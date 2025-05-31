package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Tool(
    val name: String,
    val version: String? = null
)
