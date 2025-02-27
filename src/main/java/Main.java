import fmweckserver.FmWeckClient;
import fmweckserver.FmWeckServer;
import lsp.WitnessLanguageServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import witnesses.AnalysisManager;
import witnesses.WitnessReader;

public class Main {

    // TODO: hardcoded port
    private static final int PORT = 50051;

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        FmWeckClient fmweckclient = new FmWeckClient("localhost", PORT);
        FmWeckServer fmWeckServer = new FmWeckServer();
        fmWeckServer.startFmWeckServer(PORT);
        WitnessReader witnessReader = new WitnessReader();
        AnalysisManager analysisManager = new AnalysisManager(witnessReader, fmweckclient);
        createLanguageServer(analysisManager);
    }

    private static void createLanguageServer(AnalysisManager analysisManager) {
        WitnessLanguageServer witnessLanguageServer = new WitnessLanguageServer(analysisManager);
        witnessLanguageServer.launchOnStream(System.in, System.out);
        log.info("Language server launched.");
    }

}