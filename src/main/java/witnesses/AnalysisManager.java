package witnesses;

import fm_weck.generated.FmWeckService;
import fmweckserver.AnalyzeMessageParams;
import fmweckserver.FmWeckClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.CodeLens;
import witnesses.data.run.Tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalysisManager {
    private final FmWeckClient fmweckclient;

    private final Logger log = LogManager.getLogger(AnalysisManager.class);

    public AnalysisManager(FmWeckClient fmweckclient) {
        this.fmweckclient = fmweckclient;
    }

    public List<CodeLens> analyze(AnalyzeMessageParams message, Tool tool) {
        log.info("Starting analysis for tool: " + tool.name());
        try {
            // TODO: wrap into futures
            FmWeckService.RunID runId = fmweckclient.startRun(message, tool);
            Thread.sleep(5000); // Optional: wait a bit before querying results
            String witness = fmweckclient.waitOnRun(runId);
            return readAndConvertWitnesses(witness);
        } catch (Exception e) {
            // TODO: proper error handling
            e.printStackTrace();
        } finally {
            //fmweckclient.shutdown();
        }
        return new ArrayList<>();
    }

    public List<CodeLens> readAndConvertWitnesses(String witness) {
        List<CodeLens> codeLenses = new ArrayList<>();
        try {
            codeLenses = WitnessReader.readAndConvertWitness(witness);
        } catch (IOException e) {
            // TODO: proper error handling
            e.printStackTrace();
        }
        return codeLenses;
    }


}