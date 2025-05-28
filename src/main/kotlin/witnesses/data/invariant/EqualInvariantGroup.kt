package witnesses.data.invariant

import witnesses.data.yaml.Location

data class EqualInvariantGroup(
    val shortestInvariantString: String,
    val location: Location,
    val allInvariants: List<InvariantPiece>
) {
    override fun toString(): String {
        return allInvariants.joinToString(", ") { it.tool.name + ": " + it.value }
    }
}
