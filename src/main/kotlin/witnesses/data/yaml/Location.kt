package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val file_name: String,
    val file_hash: String? = null,
    val line: Int,
    val column: Int? = null,
    val function: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Location
        if (line != other.line) return false
        if (column != other.column) return false
        return true
    }

    override fun hashCode(): Int {
        var result = line
        result = 31 * result + (column ?: 0)
        return result
    }
}
