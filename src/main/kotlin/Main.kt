import fmweckserver.FmWeckClient
import fmweckserver.FmWeckServer
import lsp.WitnessLanguageServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import witnesses.AnalysisManager

object Main {
    // TODO: hardcoded port
    private const val PORT = 50051

    private val log: Logger = LogManager.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val fmWeckClient = FmWeckClient("localhost", PORT)
        val fmWeckServer = FmWeckServer()
        runCatching {
            fmWeckServer.startFmWeckServer(PORT)
            val analysisManager = AnalysisManager(fmWeckClient)
            createLanguageServer(analysisManager)
        }.onFailure {
            fmWeckServer.process.destroy()
        }
    }

    private fun createLanguageServer(analysisManager: AnalysisManager) {
        val witnessLanguageServer = WitnessLanguageServer(analysisManager)
        witnessLanguageServer.launchOnStream(System.`in`, System.out)
        log.info("Language server launched.")
    }
}