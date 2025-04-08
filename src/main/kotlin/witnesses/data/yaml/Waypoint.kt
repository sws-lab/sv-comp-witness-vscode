package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Waypoint(
    val type: String,
    val action: String,
    val location: Location,
    val constraint: Constraint? = null
)
