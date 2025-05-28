package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Segment(
    val waypoint: Waypoint
)
