import lsp.SVLanguageServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import witnesses.WitnessHandler;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        SVLanguageServer languageServer = createLanguageServer();
        log.info("Language server launched.");

        WitnessHandler witnessHandler = new WitnessHandler();
        try {
            // TODO: hardcoded values
            witnessHandler.readAndConvertWitnesses(languageServer, "./examples/gob-n.c40.yml");
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