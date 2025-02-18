package file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import witnesses.AnalysisManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WitnessTextDocumentService implements TextDocumentService {

    private final AnalysisManager analysisManager;

    private final Map<URI, List<CodeLens>> codeLenses = new HashMap<>();

    private static final Logger log = LogManager.getLogger(WitnessTextDocumentService.class);

    public WitnessTextDocumentService(AnalysisManager analysisManager) {
        this.analysisManager = analysisManager;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem doc = params.getTextDocument();
        URI fileUri = URI.create(doc.getUri());
        log.info("File opened: " + fileUri);
        codeLenses.put(fileUri, analysisManager.analyze());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        // TODO
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        TextDocumentIdentifier doc = params.getTextDocument();
        URI fileUri = URI.create(doc.getUri());
        log.info("File saved: " + fileUri);
        codeLenses.put(fileUri, analysisManager.analyze());
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        String uri = params.getTextDocument().getUri();
                        URI decodedUri = new URI(URLDecoder.decode(uri, StandardCharsets.UTF_8));
                        if (codeLenses.containsKey(decodedUri)) {
                            return codeLenses.get(decodedUri);
                        } else return new ArrayList<>();
                    } catch (URISyntaxException e) {
                        // TODO: proper error handling
                        e.printStackTrace();
                    }
                    return new ArrayList<>();
                });
    }
}
