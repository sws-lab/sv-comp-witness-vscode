package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val file_name: String,
    val file_hash: String? = null,
    val line: Int,
    val column: Int? = null,
    val function: String? = null
)
