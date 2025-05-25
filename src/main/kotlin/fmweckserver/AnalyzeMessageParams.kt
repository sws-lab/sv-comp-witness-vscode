package fmweckserver

@JvmRecord
data class AnalyzeMessageParams(
    val command: String,
    val dataModel: String,
    val property: String,
    val fileUri: String,
    val fileRelativePath: String
)