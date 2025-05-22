package witnesses.data.invariant

import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Location

data class EqualInvariantGroup(
    val shortestInvariantString: String,
    val location: Location,
    val allInvariants: List<Invariant>
) {
    override fun toString(): String {
        return allInvariants.joinToString(", ") { it.tool?.name + ": " + it.value }
    }
}
