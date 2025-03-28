package witnesses.data.yaml

data class Waypoint(
    val type: String,
    val action: String,
    val location: Location,
    val constraint: Constraint?
)
