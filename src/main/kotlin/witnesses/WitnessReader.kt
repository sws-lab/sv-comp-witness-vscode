package witnesses

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Location
import witnesses.data.yaml.Waypoint
import witnesses.data.yaml.Witness

object WitnessReader {
    private val log: Logger = LogManager.getLogger(WitnessReader::class.java)

    private fun rangeFromLocation(location: Location): Range {
        // Position is zero-based as opposed to witnesses, where min value is 1
        val zeroPos = Position(location.line - 1, location.column?.minus(1) ?: 0)
        return Range(zeroPos, zeroPos)
    }

    private fun convertCorrectnessWitness(invariant: Invariant): CodeLens {
        val range = rangeFromLocation(invariant.location)
        val command = Command(invariant.value, "")
        return CodeLens(range, command, null)
    }

    private fun convertViolationWitness(waypoint: Waypoint): CodeLens {
        val range = rangeFromLocation(waypoint.location)
        val type = waypoint.type
        var title = type
        if (type != "target" && type != "function_enter" && waypoint.constraint != null)
            title += ": " + waypoint.constraint.value
        val command = Command(title, "")
        return CodeLens(range, command, null)
    }

    private fun convertWitnessToCodeLenses(witnesses: List<Witness>): List<CodeLens> {
        return witnesses.flatMap { it.content }.flatMap { contentElement ->
            if (contentElement.invariant != null)
                listOf(convertCorrectnessWitness(contentElement.invariant))
            else
                contentElement.segment!!.map { segment ->
                    convertViolationWitness(segment.waypoint)
                }
        }
    }

    fun readWitnessFromYaml(witnessStrings: List<String>): List<Witness> {
        val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val typeRef = object : TypeReference<List<Witness>>() {}
        return witnessStrings.flatMap { witness ->
            objectMapper.readValue(witness, typeRef)
        }
    }

    fun readAndConvertWitness(witnessStrings: List<String>): List<CodeLens> {
        log.info("Read witnesses and convert them to code lenses")
        val witnesses = readWitnessFromYaml(witnessStrings)
        return convertWitnessToCodeLenses(witnesses)
    }
}