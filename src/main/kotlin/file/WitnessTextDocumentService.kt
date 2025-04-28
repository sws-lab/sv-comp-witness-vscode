package file

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import java.net.URI
import java.util.concurrent.CompletableFuture

class WitnessTextDocumentService(
    private val codeLenses: MutableMap<URI, List<CodeLens>>
) : TextDocumentService {

    private val changedFiles: MutableSet<URI?> = HashSet()

    override fun didOpen(params: DidOpenTextDocumentParams?) {
        // TODO
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        changedFiles.add(URI.create(params.textDocument.uri))
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        // TODO
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        val fileUri = URI.create(params.textDocument.uri)
        if (fileUri in changedFiles) {
            changedFiles.remove(fileUri)
            codeLenses.remove(fileUri)
        }
    }

    override fun codeLens(params: CodeLensParams): CompletableFuture<List<CodeLens>> {
        return CompletableFuture.supplyAsync {
            codeLenses[URI.create(params.textDocument.uri)] ?: ArrayList()
        }
    }

}