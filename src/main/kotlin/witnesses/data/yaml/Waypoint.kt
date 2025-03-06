package witnesses.data.yaml

@JvmRecord
data class Waypoint(
    val type: String,
    val action: String,
    val location: Location,
    val constraint: Constraint?
)
