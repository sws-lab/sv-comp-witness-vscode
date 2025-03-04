package witnesses

import fmweckserver.AnalyzeMessageParams
import fmweckserver.FmWeckClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.lsp4j.CodeLens
import witnesses.data.run.Tool
import java.io.IOException

class AnalysisManager(private val fmWeckClient: FmWeckClient) {

    private val log: Logger = LogManager.getLogger(AnalysisManager::class.java)

    fun analyze(message: AnalyzeMessageParams, tool: Tool): List<CodeLens> =
        try {
            log.info("Starting analysis for tool: " + tool.name)
            // TODO: wrap into futures
            val runId = fmWeckClient.startRun(message, tool)
            Thread.sleep(5000) // Optional: wait a bit before querying results
            val witness = fmWeckClient.waitOnRun(runId)
            readAndConvertWitnesses(witness)
        } catch (e: Exception) {
            e.printStackTrace()
            TODO("proper error handling")
        }


    fun readAndConvertWitnesses(witness: String?): List<CodeLens> =
        try {
            WitnessReader.readAndConvertWitness(witness)
        } catch (e: IOException) {
            e.printStackTrace()
            TODO("proper error handling")
        }
}