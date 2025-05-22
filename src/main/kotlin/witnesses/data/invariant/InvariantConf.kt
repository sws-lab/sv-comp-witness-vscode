package witnesses.data.invariant

import witnesses.data.yaml.Invariant

data class InvariantConf(
    val invariant: Invariant,
    val allInvariants: List<Invariant>
) {
    override fun toString(): String {
        return allInvariants.joinToString(", ") { it.tool?.name + ": " + it.value }
    }
}
