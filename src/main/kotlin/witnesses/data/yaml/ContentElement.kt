package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class ContentElement(
    val invariant: Invariant? = null,
    val segment: List<Segment>? = null
)