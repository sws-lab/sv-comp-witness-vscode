import lsp.SVLanguageServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import witnesses.ProcessWitnesses;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        SVLanguageServer languageServer = createLanguageServer();
        log.info("Language server launched.");

        ProcessWitnesses processWitnesses = new ProcessWitnesses();
        try {
            // TODO: hardcoded values
            processWitnesses.readAndConvertWitnesses(languageServer, "./examples/gob-n.c40.yml");
        } catch (IOException | URISyntaxException e) {
            // TODO: proper error handling
            e.printStackTrace();
        }
    }

    /**
     * Method for creating and launching language server.
     */

    private static SVLanguageServer createLanguageServer() {
        SVLanguageServer svLanguageServer = new SVLanguageServer();
        svLanguageServer.launchOnStream(System.in, System.out);

        return svLanguageServer;
    }

}