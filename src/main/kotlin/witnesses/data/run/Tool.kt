package witnesses.data.run

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class Tool(
    val name: String,
    val version: String?
)