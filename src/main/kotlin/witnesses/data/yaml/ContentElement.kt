package witnesses.data.yaml

@JvmRecord
data class ContentElement(
    val invariant: Invariant?,
    val segment: List<Segment>?
)