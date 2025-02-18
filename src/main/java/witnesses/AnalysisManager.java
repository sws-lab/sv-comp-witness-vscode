package witnesses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.CodeLens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalysisManager {
    private final WitnessReader witnessReader;

    private final Logger log = LogManager.getLogger(AnalysisManager.class);

    public AnalysisManager(WitnessReader witnessReader) {
        this.witnessReader = witnessReader;
    }

    public List<CodeLens> analyze() {
        log.info("Starting analysis");
        return readAndConvertWitnesses();
    }

    public List<CodeLens> readAndConvertWitnesses() {
        List<CodeLens> codeLenses = new ArrayList<>();
        try {
            // TODO: hardcoded value
            codeLenses = witnessReader.readAndConvertWitnesses("./examples/standard_strcpy_original-2");
        } catch (IOException e) {
            // TODO: proper error handling
            e.printStackTrace();
        }
        return codeLenses;
    }


}