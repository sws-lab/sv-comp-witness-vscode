package file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WitnessTextDocumentService implements TextDocumentService {

    private final Map<URI, List<CodeLens>> codeLenses;
    private final Set<URI> changedFiles = new HashSet<>();

    private static final Logger log = LogManager.getLogger(WitnessTextDocumentService.class);

    public WitnessTextDocumentService(Map<URI, List<CodeLens>> codeLenses) {
        this.codeLenses = codeLenses;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        // TODO
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        URI fileUri = URI.create(params.getTextDocument().getUri());
        changedFiles.add(fileUri);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        URI fileUri = URI.create(params.getTextDocument().getUri());
        if (changedFiles.contains(fileUri)) {
            changedFiles.remove(fileUri);
            codeLenses.remove(fileUri);
        }
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return CompletableFuture.supplyAsync(
                () -> {
                    URI fileUri = URI.create(params.getTextDocument().getUri());
                    if (codeLenses.containsKey(fileUri)) {
                        return codeLenses.get(fileUri);
                    } else return new ArrayList<>();
                });
    }
}
