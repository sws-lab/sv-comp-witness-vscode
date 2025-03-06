package witnesses

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import witnesses.data.yaml.Witness
import java.io.IOException
import java.nio.file.Paths

object WitnessReader {
    private val log: Logger = LogManager.getLogger(WitnessReader::class.java)

    private fun convertWitnessToCodeLenses(witnesses: List<Witness>): List<CodeLens> {
        return witnesses.flatMap { it.content }.map { content ->
            if (content.invariant != null) {
                val invariant = content.invariant
                val location = invariant.location
                // Position is zero-based as opposed to witnesses, where min value is 1
                val zeroPos = Position(location.line - 1, location.column?.minus(1) ?: 0)
                val range = Range(zeroPos, zeroPos)
                val command = Command(invariant.value, "")
                CodeLens(range, command, null)
            } else TODO("unimplemented")
        }
    }

    fun filterWitnesses(witnesses: MutableList<Witness>): MutableList<Witness?> {
        val filteredWitnesses: MutableList<Witness?> = ArrayList<Witness?>()
        val uniqueInvariants: MutableSet<String?> = HashSet<String?>()
        for (witness in witnesses) {
            val filteredContent = witness.content
                .filter { content ->
                    val invariant = content.invariant
                    val location = invariant!!.location
                    val fileName = Paths.get(location.file_name).fileName.toString()
                    val key = "$fileName:${location.line}:${location.column}:${invariant.value}"
                    invariant.value != "1" && uniqueInvariants.add(key)
                }
            // Only add witness if it has remaining content
            if (!filteredContent.isEmpty()) {
                filteredWitnesses.add(
                    Witness(
                        entry_type = witness.entry_type,
                        metadata = witness.metadata,
                        content = filteredContent
                    )
                )
            }
        }
        return filteredWitnesses
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readAndConvertWitness(witnessStrings: List<String>): List<CodeLens> {
        log.info("Read witnesses and convert them to code lenses")
        val objectMapper = ObjectMapper(YAMLFactory())
        val witnesses = witnessStrings.flatMap { witness ->
            objectMapper.readValue<List<Witness>>(
                witness, objectMapper.typeFactory.constructCollectionType(
                    List::class.java, Witness::class.java
                )
            )
        }
        return convertWitnessToCodeLenses(witnesses)
    }
}