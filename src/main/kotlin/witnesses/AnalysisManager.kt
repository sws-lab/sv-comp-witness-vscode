package witnesses

import combine.types.VariableTypeHandler.extractTypeEnvByLocation
import combine.types.VariableTypeHandler.getVariableTypesForProgram
import fmweckserver.AnalyzeMessageParams
import fmweckserver.FmWeckClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import witnesses.WitnessComparison.decomposeInvariantByConjunctions
import witnesses.WitnessComparison.getEqualInvariantGroups
import witnesses.WitnessReader.readWitnessFromYaml
import witnesses.data.invariant.EqualInvariantGroup
import witnesses.data.run.Tool
import witnesses.data.run.ToolLoader
import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Location
import witnesses.data.yaml.Waypoint
import witnesses.data.yaml.Witness

class AnalysisManager(private val fmWeckClient: FmWeckClient) {

    private val tools: List<Tool> = ToolLoader.tools

    private val log: Logger = LogManager.getLogger(AnalysisManager::class.java)

    fun analyze(message: AnalyzeMessageParams): MutableList<CodeLens> {
        val lenses = mutableListOf<CodeLens>()
        val witnesses = mutableListOf<Witness>()

        tools.forEach { tool ->
            val witnessStrings = runTool(message, tool)
            witnesses.addAll(readWitnessFromYaml(witnessStrings))
        }

        lenses.addAll(convert(witnesses, message))
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
            TODO("proper error handling")
        }
    }

    fun convert(witnesses: List<Witness>, message: AnalyzeMessageParams): List<CodeLens> {
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
        val invariantComponentsByLoc: LocToInvariantComponents = mutableMapOf()
        correctnessInvariants.forEach { (invariant, witness) ->
            decomposeInvariantByConjunctions(invariant, witness, invariantComponentsByLoc)
        }
        val typeEnv = extractTypeEnvByLocation(getVariableTypesForProgram(message.fileRelativePath, "vtypes.json"))
        val correctnessCodeLenses =
            getEqualInvariantGroups(invariantComponentsByLoc, typeEnv)
                .sortedByDescending { it.equalInvariantComponents.size }
                .map { equalInvariantGroup ->
                convertCorrectnessWitness(equalInvariantGroup)
            }
        return correctnessCodeLenses + violationCodeLenses
    }

    private fun convertCorrectnessWitness(equalInvariantGroup: EqualInvariantGroup): CodeLens {
        val range = rangeFromLocation(equalInvariantGroup.location)
        val command =
            Command(
                equalInvariantGroup.shortestInvariantString,
                "showInvariantInfo",
                listOf(equalInvariantGroup.toString())
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