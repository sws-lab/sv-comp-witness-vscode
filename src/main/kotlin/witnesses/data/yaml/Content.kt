package witnesses.data.yaml

@JvmRecord
data class Content(
    val invariant: Invariant?,
    val segment: List<Segment>?
)