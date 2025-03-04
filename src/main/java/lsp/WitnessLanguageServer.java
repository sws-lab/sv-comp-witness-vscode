package lsp;

import file.WitnessTextDocumentService;
import fmweckserver.AnalyzeMessageParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import witnesses.AnalysisManager;
import witnesses.ToolLoader;
import witnesses.data.run.Tool;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WitnessLanguageServer implements LanguageServer, WorkspaceService {

    private WitnessLanguageClient client;
    private final TextDocumentService textDocumentService;
    private final ExecutorService lspThreadPool = Executors.newCachedThreadPool();


    private final AnalysisManager analysisManager;
    private final Map<URI, List<CodeLens>> codeLenses = new HashMap<>();
    private final List<Tool> tools = ToolLoader.getTools();

    private static final Logger log = LogManager.getLogger(WitnessLanguageServer.class);

    public WitnessLanguageServer(AnalysisManager analysisManager) {
        this.textDocumentService = new WitnessTextDocumentService(codeLenses);
        this.analysisManager = analysisManager;
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
        serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        //serverCapabilities.setInlayHintProvider(true);

        CodeLensOptions cl = new CodeLensOptions(false);
        serverCapabilities.setCodeLensProvider(cl);

        List<String> commands = new ArrayList<>();
        commands.add("custom/handleWebviewMessage");
        serverCapabilities.setExecuteCommandProvider(new ExecuteCommandOptions(commands));


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
        return this.textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTrace(SetTraceParams params) {
        // TODO: noop?
    }

    /**
     * Launch on stream.
     *
     * @param input  the in
     * @param output the out
     */
    public void launchOnStream(InputStream input, OutputStream output) {
        Launcher<WitnessLanguageClient> launcher = new Launcher.Builder<WitnessLanguageClient>()
                .setLocalService(this)
                .setRemoteInterface(WitnessLanguageClient.class)
                .setInput(input)
                .setOutput(output)
                .setExecutorService(lspThreadPool)
                .create();
        this.client = launcher.getRemoteProxy();
        launcher.startListening();
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // TODO
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO
    }

    @JsonNotification(value="custom/handleWebviewMessage", useSegment = false)
    public void handleWebviewMessage(AnalyzeMessageParams message) {
        for (Tool tool : tools) {
            URI fileUri = URI.create(message.fileUri());
            codeLenses.put(fileUri, analysisManager.analyze(message, tool));
            client.refreshCodeLenses();
        }
    }

}
