package witnesses

import c.CInvariantAst
import c.collectConjunctAsts
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import witnesses.data.invariant.EqualInvariantGroup
import witnesses.data.invariant.InvariantComponent
import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Location
import witnesses.data.yaml.Witness

typealias LocToInvariantComponents = MutableMap<Location, MutableList<InvariantComponent>> // TODO: make map immutable

object WitnessComparison {

    fun decomposeInvariantByConjunctions(invariant: Invariant, witness: Witness, invariantComponentsByLoc: LocToInvariantComponents) {
        val ast = CInvariantAst.createAst(invariant.value)
        val conjunctAsts = collectConjunctAsts(ast)
        val invariantComponents = conjunctAsts.map { conjunctNode ->
            val normalizedAst = conjunctNode.normalize()
            InvariantComponent(
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
        invariantComponentsByLoc.getOrPut(invariant.location) { mutableListOf() }.addAll(invariantComponents)
    }

    fun getEqualInvariantGroups(invariantComponentsByLoc: LocToInvariantComponents) =
        computeEqualInvariantGroups(invariantComponentsByLoc)

    fun computeEqualInvariantGroups(invariantComponentsByLoc: LocToInvariantComponents): List<EqualInvariantGroup> {
        val graph = buildImplicationGraph(invariantComponentsByLoc)
        val inspector = KosarajuStrongConnectivityInspector(graph)
        val sccList = inspector.stronglyConnectedSets()
        return sccList.map { component ->
            val representative = component.minByOrNull { it.normValue.length }!!
            EqualInvariantGroup(
                shortestInvariantString = representative.normValue,
                location = representative.location,
                equalInvariantComponents = component.toList()
            )
        }
    }

    private fun implies(a: InvariantComponent, b: InvariantComponent): Boolean {
        // Placeholder: check if a logically implies b
        return a.normValue == b.normValue // TODO: replace with solver queries
    }

    private fun buildImplicationGraph(invariantComponentsByLoc: LocToInvariantComponents): DefaultDirectedGraph<InvariantComponent, DefaultEdge> {
        val graph = DefaultDirectedGraph<InvariantComponent, DefaultEdge>(DefaultEdge::class.java)

        for (invariants in invariantComponentsByLoc.values) {
            for (a in invariants) {
                graph.addVertex(a)
            }
            for (a in invariants) {
                for (b in invariants) {
                    if (a != b && implies(a, b) && implies(b, a)) {
                        graph.addEdge(a, b)
                        graph.addEdge(b, a)
                    }
                }
            }
        }
        return graph
    }

}