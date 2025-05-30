package witnesses

import c.CInvariantAst
import c.collectConjunctAsts
import combine.ksmt.CType
import combine.ksmt.createSMTBoolExpr
import combine.ksmt.impliesSat
import combine.types.TypeEnv
import io.ksmt.KContext
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
                originalInvariantValue = invariant.value,
                ast = normalizedAst
            )
        }
        invariantComponentsByLoc.getOrPut(invariant.location) { mutableListOf() }.addAll(invariantComponents)
    }

    fun getEqualInvariantGroups(invariantComponentsByLoc: LocToInvariantComponents, typeEnv: TypeEnv) =
        computeEqualInvariantGroups(invariantComponentsByLoc, typeEnv)

    fun computeEqualInvariantGroups(invariantComponentsByLoc: LocToInvariantComponents, typeEnv: TypeEnv): List<EqualInvariantGroup> {
        val graph = buildImplicationGraph(invariantComponentsByLoc, typeEnv)
        val inspector = KosarajuStrongConnectivityInspector(graph)
        val sccList = inspector.stronglyConnectedSets()
        return sccList.map { equivalentInvariantComponents ->
            // Deduplicate per tool: pick shortest .value for each tool
            val deDuplicated = equivalentInvariantComponents
                .groupBy { it.tool }
                .mapValues { (_, group) -> group.minByOrNull { it.value.length }!! }
                .values
                .sortedBy { it.value.length }
            val representative = deDuplicated.minByOrNull { it.normValue.length }!!
            EqualInvariantGroup(
                shortestInvariantString = representative.normValue,
                location = representative.location,
                equalInvariantComponents = deDuplicated.toList()
            )
        }
    }

    private fun areEqual(a: InvariantComponent, b: InvariantComponent, locTypeEnv: Map<String, CType>): Boolean {
        if (a.normValue == b.normValue) return true
        else {
            try {
                KContext().use { ctx ->
                    val aSmtExpr = createSMTBoolExpr(a.ast, ctx, locTypeEnv)
                    val bSmtExpr = createSMTBoolExpr(b.ast, ctx, locTypeEnv)
                    return impliesSat(ctx, aSmtExpr, bSmtExpr) && impliesSat(ctx, bSmtExpr, aSmtExpr)
                }
            } catch (t: Throwable) {
                return false
            }
        }
    }

    private fun buildImplicationGraph(invariantComponentsByLoc: LocToInvariantComponents, typeEnv: TypeEnv): DefaultDirectedGraph<InvariantComponent, DefaultEdge> {
        val graph = DefaultDirectedGraph<InvariantComponent, DefaultEdge>(DefaultEdge::class.java)

        for ((loc, invariants) in invariantComponentsByLoc) {
            val locTypeEnv = typeEnv[loc.line]
            for (a in invariants) {
                graph.addVertex(a)
            }
            for (i in invariants.indices) {
                for (j in i + 1 until invariants.size) {
                    val a = invariants[i]
                    val b = invariants[j]
                    if (locTypeEnv != null && areEqual(a, b, locTypeEnv)) {
                        graph.addEdge(a, b)
                        graph.addEdge(b, a)
                    }
                }
            }
        }
        return graph
    }

}