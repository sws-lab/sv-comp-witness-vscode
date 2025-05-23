package witnesses

import c.CInvariantAst
import c.collectConjunctAsts
import witnesses.data.invariant.EqualInvariantGroup
import witnesses.data.invariant.InvariantPiece
import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Witness

object WitnessComparison {

    private val invariantsByLoc: MutableMap<Int, MutableList<InvariantPiece>> = mutableMapOf()

    fun decomposeInvariantByConjunctions(invariant: Invariant, witness: Witness) {
        val ast = CInvariantAst.createAst(invariant.value)
        val conjunctAsts = collectConjunctAsts(ast)
        val invariantPieces = conjunctAsts.map { conjunctNode ->
            val normalizedAst = conjunctNode.normalize()
            InvariantPiece(
                type = invariant.type,
                location = invariant.location,
                value = conjunctNode.toValue(),
                format = invariant.format,
                tool = witness.metadata.producer,
                normValue = normalizedAst.toValue(),
                originalValue = invariant.value,
                ast = normalizedAst
            )
        }
        invariantsByLoc.getOrPut(invariant.location.line) { mutableListOf() }.addAll(invariantPieces)
    }

    fun findAgreeableTools() = mapImplicationsToConf(findImplications())

    private fun findImplications(): Map<InvariantPiece, Set<InvariantPiece>> {
        // Build mapping representing implication relationships (e.g., inv1 => inv2),
        val implicationMap: MutableMap<InvariantPiece, MutableSet<InvariantPiece>> = mutableMapOf()
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

    private fun implies(a: InvariantPiece, b: InvariantPiece): Boolean {
        // Placeholder: check if a logically implies b
        return a.normValue == b.normValue // TODO: replace with solver queries
    }

    private fun mapImplicationsToConf(implMap: Map<InvariantPiece, Set<InvariantPiece>>): Map<EqualInvariantGroup, Set<EqualInvariantGroup>> {
        val valueToConf = implMap.keys
            .groupBy { it.normValue }
            .mapValues { (_, invList) ->
                val first = invList.first()
                EqualInvariantGroup(first.normValue, first.location, invList)
            }

        // Helper to get InvariantConf from Invariant
        fun toConf(inv: InvariantPiece) = valueToConf[inv.normValue]!!

        // Fold original implication map from Invariant           -> Set<Invariant>
        //                               into EqualInvariantGroup -> Set<EqualInvariantGroup>
        return implMap.entries
            .fold(mutableMapOf<EqualInvariantGroup, MutableSet<EqualInvariantGroup>>()) { acc, (key, implied) ->
                val keyConf = toConf(key)
                val impliedInvariants = implied.map { toConf(it) }.toSet()
                acc.getOrPut(keyConf) { mutableSetOf() }.addAll(impliedInvariants)
                acc
            }.mapValues { it.value.toSet() }
    }

}