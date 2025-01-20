package lsp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SVLanguageServer implements LanguageServer, WorkspaceService, TextDocumentService {

    private SVLanguageClient client;
    private final ExecutorService lspThreadPool = Executors.newCachedThreadPool();

    private Map<URI, List<InlayHint>> inlayHints = new HashMap<>();
    private Map<URI, List<CodeLens>> codeLenses = new HashMap<>();

    private static final Logger log = LogManager.getLogger(SVLanguageServer.class);

    public SVLanguageServer() {
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        ServerCapabilities serverCapabilities = new ServerCapabilities();
        serverCapabilities.setTypeDefinitionProvider(false);
        serverCapabilities.setImplementationProvider(false);
        serverCapabilities.setWorkspaceSymbolProvider(false);
        serverCapabilities.setDocumentFormattingProvider(false);
        serverCapabilities.setDocumentRangeFormattingProvider(false);
        serverCapabilities.setDocumentHighlightProvider(false);
        serverCapabilities.setColorProvider(false);
        serverCapabilities.setDocumentSymbolProvider(false);
        serverCapabilities.setDefinitionProvider(false);
        serverCapabilities.setDefinitionProvider(false);
        serverCapabilities.setReferencesProvider(false);
        serverCapabilities.setHoverProvider(false);

        serverCapabilities.setInlayHintProvider(true);

        CodeLensOptions cl = new CodeLensOptions();
        cl.setResolveProvider(false);
        serverCapabilities.setCodeLensProvider(cl);

        return CompletableFuture.completedFuture(new InitializeResult(serverCapabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        lspThreadPool.shutdownNow();
        try {
            lspThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // The Socket will be closed by the client, and so remaining threads will die and the JVM will terminate
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Launch on stream.
     *
     * @param input  the in
     * @param output the out
     */
    public void launchOnStream(InputStream input, OutputStream output) {
        Launcher<SVLanguageClient> launcher = new Launcher.Builder<SVLanguageClient>()
                .setLocalService(this)
                .setRemoteInterface(SVLanguageClient.class)
                .setInput(input)
                .setOutput(output)
                .setExecutorService(lspThreadPool)
                .create();
        this.client = launcher.getRemoteProxy();
        launcher.startListening();
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {

    }

    @Override
    public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {

    }

    @Override
    public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {

    }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {

    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        String uri = params.getTextDocument().getUri();
                        URI decodedUri = new URI(URLDecoder.decode(uri, StandardCharsets.UTF_8));
                        if (this.codeLenses.containsKey(decodedUri)) {
                            log.info("codelens");
                            log.info(codeLenses.get(decodedUri));
                            return codeLenses.get(decodedUri);
                        } else return new ArrayList<>();
                    } catch (URISyntaxException e) {
                        // TODO: proper error handling
                        e.printStackTrace();
                    }
                    return new ArrayList<>();
                });
    }

    public void addCodeLens(URI uri, CodeLens codeLens) {
        List<CodeLens> codeLenses = this.codeLenses.computeIfAbsent(uri, k -> new ArrayList<>());
        codeLenses.add(codeLens);
    }

    @Override
    public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        String uri = params.getTextDocument().getUri();
                        URI decodedUri = new URI(URLDecoder.decode(uri, StandardCharsets.UTF_8));
                        if (this.inlayHints.containsKey(decodedUri)) {
                            log.info("inlayhint");
                            return inlayHints.get(decodedUri);
                        } else return new ArrayList<>();
                    } catch (URISyntaxException e) {
                        // TODO: proper error handling
                        e.printStackTrace();
                    }
                    return new ArrayList<>();
                });
    }

    public void addInlayHint(URI uri, InlayHint inlayHint) {
        List<InlayHint> inlayHints = this.inlayHints.computeIfAbsent(uri, k -> new ArrayList<>());
        inlayHints.add(inlayHint);
    }
}
