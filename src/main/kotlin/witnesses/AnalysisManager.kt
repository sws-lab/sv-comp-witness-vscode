package witnesses

import fmweckserver.AnalyzeMessageParams
import fmweckserver.FmWeckClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import witnesses.WitnessReader.readWitnessFromYaml
import witnesses.data.yaml.*

class AnalysisManager(private val fmWeckClient: FmWeckClient) {

    private val log: Logger = LogManager.getLogger(AnalysisManager::class.java)

    fun analyze(message: AnalyzeMessageParams): MutableList<CodeLens> {
        val lenses = mutableListOf<CodeLens>()
        val witnesses = mutableListOf<Witness>()

        message.tools.forEach { tool ->
            val witnessStrings = runTool(message, Tool(tool, null))
            witnesses.addAll(readWitnessFromYaml(witnessStrings))
        }

        lenses.addAll(convert(witnesses))
        return lenses
    }

    private fun runTool(message: AnalyzeMessageParams, tool: Tool): List<String> {
        try {
            log.info("Starting analysis for tool " + tool.name)
            // TODO: wrap into futures
            val runId = fmWeckClient.startRun(message, tool)
            Thread.sleep(5000) // Optional: wait a bit before querying results
            return fmWeckClient.waitOnRun(runId)
        } catch (e: Throwable) {
            e.printStackTrace()
            TODO("proper error handling (runTool)")
        }
    }

    fun convert(witnesses: List<Witness>): List<CodeLens> {
        val correctnessInvariants = mutableListOf<Pair<Invariant, Witness>>()
        val violationCodeLenses = mutableListOf<CodeLens>()
        for (witness in witnesses) {
            for (content in witness.content) {
                content.invariant?.let { invariant ->
                    correctnessInvariants += invariant to witness
                }
                content.segment?.mapTo(violationCodeLenses) {
                    convertViolationWitness(it.waypoint)
                }
            }
        }
        val correctnessCodeLenses = correctnessInvariants.map { (invariant, witness) ->
            convertCorrectnessWitness(invariant, witness)
        }
        return correctnessCodeLenses + violationCodeLenses
    }

    private fun convertCorrectnessWitness(invariant: Invariant, witness: Witness): CodeLens {
        val range = rangeFromLocation(invariant.location)
        val command =
            Command(
                invariant.value,
                "showInvariantInfo",
                listOf(witness.metadata.producer.name)
            )
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

}