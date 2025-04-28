package witnesses

import c.CInvariantAst
import c.VariableTypeHandler.getVariableTypesForProgram
import c.collectMapping
import fmweckserver.AnalyzeMessageParams
import fmweckserver.FmWeckClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import witnesses.WitnessReader.readWitnessFromYaml
import witnesses.data.run.Tool
import witnesses.data.run.ToolLoader
import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Location
import witnesses.data.yaml.Waypoint
import witnesses.data.yaml.Witness
import java.net.URI

class AnalysisManager(private val fmWeckClient: FmWeckClient) {

    private val tools: List<Tool> = ToolLoader.tools
    private val mapping: MutableMap<Int, MutableList<Invariant>> = mutableMapOf()

    private val log: Logger = LogManager.getLogger(AnalysisManager::class.java)

    fun analyze(message: AnalyzeMessageParams): MutableList<CodeLens> {
        val lenses = mutableListOf<CodeLens>()

        tools.forEach { tool ->
            val witnessStrings = runTool(message, tool)
            val witnesses = readWitnessFromYaml(witnessStrings)
            lenses.addAll(convert(witnesses))
        }

        log.info("Invariant mapping: $mapping")

        buildProgramWithTypes(message)
        return lenses
    }

    private fun runTool(message: AnalyzeMessageParams, tool: Tool): List<String> {
        try {
            log.info("Starting analysis for tool: " + tool.name)
            // TODO: wrap into futures
            val runId = fmWeckClient.startRun(message, tool)
            Thread.sleep(5000) // Optional: wait a bit before querying results
            return fmWeckClient.waitOnRun(runId)
        } catch (e: Throwable) {
            e.printStackTrace()
            TODO("proper error handling")
        }
    }

    private fun convert(witnesses: List<Witness>): List<CodeLens> =
        witnesses
            .flatMap { it.content }
            .flatMap { contentElement ->
                if (contentElement.invariant != null) {
                    val invariant = contentElement.invariant
                    invariant.decomposedConjunctionMap =
                        collectMapping(CInvariantAst.createAst(contentElement.invariant.value))
                    // TODO: combine (currently only builds the map)
                    mapping.getOrPut(invariant.location.line) { mutableListOf() }.add(invariant)
                    listOf(convertCorrectnessWitness(invariant))
                } else
                    contentElement.segment!!.map { segment ->
                        convertViolationWitness(segment.waypoint)
                    }
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

    private fun rangeFromLocation(location: Location): Range {
        // Position is zero-based as opposed to witnesses, where min value is 1
        val zeroPos = Position(
            (location.line - 1).coerceAtLeast(0),
            (location.column?.minus(1) ?: 0).coerceAtLeast(0)
        )
        return Range(zeroPos, zeroPos)
    }

    private fun buildProgramWithTypes(message: AnalyzeMessageParams) {
        val fileName = URI.create(message.fileUri).toString() // TODO: this is in the wrong format
        val variableTypes = getVariableTypesForProgram(
            fileName,
            "variableTypes.json"
        )[fileName]
        println("VariableTypes for the file: $variableTypes")
        mapping.forEach { (line, invariant) ->

        }
    }

}