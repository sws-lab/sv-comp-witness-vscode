package witnesses.data.invariant

import witnesses.data.yaml.Location

data class EqualInvariantGroup(
    val shortestInvariantString: String,
    val location: Location,
    val equalInvariantComponents: List<InvariantComponent>
) {
    override fun toString(): String {
        return equalInvariantComponents.joinToString(", ") { it.tool.name + ": " + it.value }
    }
}
