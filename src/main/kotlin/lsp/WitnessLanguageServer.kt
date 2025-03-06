package lsp

import file.WitnessTextDocumentService
import fmweckserver.AnalyzeMessageParams
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import witnesses.AnalysisManager
import witnesses.data.run.ToolLoader
import witnesses.data.run.Tool
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

typealias WitnessLanguageClient = LanguageClient

class WitnessLanguageServer(private val analysisManager: AnalysisManager) : LanguageServer, WorkspaceService {
    private var client: WitnessLanguageClient? = null
    private val lspThreadPool: ExecutorService = Executors.newCachedThreadPool()
    private val codeLenses: MutableMap<URI?, List<CodeLens>> = HashMap<URI?, List<CodeLens>>()
    private val textDocumentService: TextDocumentService = WitnessTextDocumentService(codeLenses)
    private val tools: List<Tool> = ToolLoader.tools

    override fun initialize(initializeParams: InitializeParams?): CompletableFuture<InitializeResult?> {
        val serverCapabilities = ServerCapabilities().apply {
            setTypeDefinitionProvider(false)
            setImplementationProvider(false)
            setWorkspaceSymbolProvider(false)
            setDocumentFormattingProvider(false)
            setDocumentRangeFormattingProvider(false)
            setDocumentHighlightProvider(false)
            setColorProvider(false)
            setDocumentSymbolProvider(false)
            setDefinitionProvider(false)
            setDefinitionProvider(false)
            setReferencesProvider(false)
            setHoverProvider(false)
            setTextDocumentSync(TextDocumentSyncKind.Full)
            codeLensProvider = CodeLensOptions(false)
            executeCommandProvider = ExecuteCommandOptions(mutableListOf("custom/handleWebviewMessage"))
        }

        return CompletableFuture.completedFuture<InitializeResult?>(InitializeResult(serverCapabilities))
    }

    // TODO: stop fm-weck server process when language server shuts down, as vscode will then restart extension and another fm-weck server will be started
    override fun shutdown(): CompletableFuture<Any?> {
        return CompletableFuture.completedFuture<Any?>(null)
    }

    override fun exit() {
        lspThreadPool.shutdownNow()
        try {
            lspThreadPool.awaitTermination(1, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        // The Socket will be closed by the client, and so remaining threads will die and the JVM will terminate
    }

    override fun getTextDocumentService(): TextDocumentService {
        return this.textDocumentService
    }

    override fun getWorkspaceService(): WorkspaceService? {
        // TODO Auto-generated method stub
        return null
    }

    override fun setTrace(params: SetTraceParams?) {
        // TODO: noop?
    }

    /**
     * Launch on stream.
     *
     * @param input  the in
     * @param output the out
     */
    fun launchOnStream(input: InputStream?, output: OutputStream?) {
        val launcher: Launcher<WitnessLanguageClient> = Launcher.Builder<WitnessLanguageClient>()
            .setLocalService(this)
            .setRemoteInterface(WitnessLanguageClient::class.java)
            .setInput(input)
            .setOutput(output)
            .setExecutorService(lspThreadPool)
            .create()
        this.client = launcher.getRemoteProxy()
        launcher.startListening()
    }

    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        // TODO
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        // TODO
    }

    /**
     * When this function is called, the client is not null
     * due to [launchOnStream] being called first (see vscode/src/extension.ts)
     */

    @JsonNotification(value = "custom/handleWebviewMessage", useSegment = false)
    fun handleWebviewMessage(message: AnalyzeMessageParams) {
        for (tool in tools) {
            val fileUri = URI.create(message.fileUri)
            codeLenses.put(fileUri, analysisManager.analyze(message, tool))
            client!!.refreshCodeLenses()
        }
    }

}