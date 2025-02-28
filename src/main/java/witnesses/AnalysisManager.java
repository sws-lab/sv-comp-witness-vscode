package witnesses;

import fmweckserver.AnalyzeMessageParams;
import fmweckserver.FmWeckClient;
import fm_weck.generated.FmWeckService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.CodeLens;
import witnesses.data.run.Tool;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class AnalysisManager {
    private final WitnessReader witnessReader;
    private final FmWeckClient fmweckclient;

    private final Logger log = LogManager.getLogger(AnalysisManager.class);

    public AnalysisManager(WitnessReader witnessReader, FmWeckClient fmweckclient) {
        this.witnessReader = witnessReader;
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
            codeLenses = witnessReader.readAndConvertWitness(witness);
        } catch (IOException e) {
            // TODO: proper error handling
            e.printStackTrace();
        }
        return codeLenses;
    }


}