package witnesses.data.yaml

@JvmRecord
data class Invariant(
        val type: String,

        val location: Location,

        val value: String,

        val format: String,
)
