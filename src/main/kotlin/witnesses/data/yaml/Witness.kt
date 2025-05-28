package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Witness(
    val entry_type: String,
    val metadata: MetaData,
    val content: List<ContentElement>
)
