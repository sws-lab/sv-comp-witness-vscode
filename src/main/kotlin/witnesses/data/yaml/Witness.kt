package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Witness(
    val entry_type: String? = null,
    val content: List<ContentElement>
)
