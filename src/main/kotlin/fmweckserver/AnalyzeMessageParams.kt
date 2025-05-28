package fmweckserver

@JvmRecord
data class AnalyzeMessageParams(
    val command: String,
    val dataModel: String,
    val property: String,
    val tools: List<String>,
    val fileUri: String,
    val fileRelativePath: String
)

data class Tool(
    val name: String,
    val version: String?
)