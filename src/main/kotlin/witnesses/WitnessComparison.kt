package witnesses

import c.CInvariantAst
import c.collectMapping
import witnesses.data.invariant.InvariantConf
import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Witness

object WitnessComparison {

    private val invariantsByLoc: MutableMap<Int, MutableList<Invariant>> = mutableMapOf()

    fun decomposeInvariantByConjunctions(invariant: Invariant, witness: Witness) {
        val ast = CInvariantAst.createAst(invariant.value).normalize()
        invariant.decomposedConjunctionMap = collectMapping(ast)
        invariant.tool = witness.metadata.producer
        invariant.normValue = ast.toCode()
        invariantsByLoc.getOrPut(invariant.location.line) { mutableListOf() }.add(invariant)
    }

    fun findAgreeableTools() = mapImplicationsToConf(findImplications())

    private fun findImplications(): Map<Invariant, Set<Invariant>> {
        // Build mapping representing implication relationships (e.g., inv1 => inv2),
        val implicationMap: MutableMap<Invariant, MutableSet<Invariant>> = mutableMapOf()
        for (invariants in invariantsByLoc.values) {
            for (a in invariants) {
                for (b in invariants) {
                    if (a != b && implies(a, b)) {
                        implicationMap.getOrPut(a) { mutableSetOf() }.add(b)
                    }
                }
                // Ensure all invariants are represented even if they imply nothing
                implicationMap.getOrPut(a) { mutableSetOf() }
            }
        }

        return implicationMap
    }

    private fun implies(a: Invariant, b: Invariant): Boolean {
        // Placeholder: check if a logically implies b
        return a.normValue == b.normValue // TODO: replace with solver queries
    }

    private fun mapImplicationsToConf(implMap: Map<Invariant, Set<Invariant>>): Map<InvariantConf, Set<InvariantConf>> {
        val valueToConf = implMap.keys
            .groupBy { it.normValue }
            .mapValues { (_, invList) ->
                InvariantConf(invList.first(), invList)
            }

        // Helper to get InvariantConf from Invariant
        fun toConf(inv: Invariant) = valueToConf[inv.normValue]!!

        // Fold original implication map from Invariant     -> Set<Invariant>
        //                               into InvariantConf -> Set<InvariantConf>
        return implMap.entries
            .fold(mutableMapOf<InvariantConf, MutableSet<InvariantConf>>()) { acc, (key, implied) ->
                val keyConf = toConf(key)
                val impliedConfs = implied.map { toConf(it) }.toSet()
                acc.getOrPut(keyConf) { mutableSetOf() }.addAll(impliedConfs)
                acc
            }.mapValues { it.value.toSet() }
    }

}