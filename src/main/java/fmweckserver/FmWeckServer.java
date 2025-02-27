package fmweckserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FmWeckServer {

    private final Logger log = LogManager.getLogger(FmWeckServer.class);

    // TODO: implement proper shut-down

    public void startFmWeckServer(int PORT) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "fm-weck",
                "server",
                "--port",
                Integer.toString(PORT),
                "--listen",
                "localhost");

        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            log.info("fm-weck server started on port " + PORT);
            // TODO: redirect server output to IDE output-log
        } catch (IOException e) {
            // TODO: proper error handling
            log.error("Failed to start fm-weck server", e);
            throw new RuntimeException(e);
        }
    }

}
