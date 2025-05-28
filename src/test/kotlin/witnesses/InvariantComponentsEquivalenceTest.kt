package witnesses

import c.invariantAST.BinaryExpression
import c.invariantAST.Const
import c.invariantAST.Expression
import c.invariantAST.Var
import combine.ksmt.CType
import combine.types.TypeEnv
import witnesses.WitnessComparison.computeEqualInvariantGroups
import witnesses.data.invariant.InvariantComponent
import witnesses.data.yaml.Tool
import witnesses.data.yaml.Location
import kotlin.test.Test
import kotlin.test.assertEquals

object InvariantComponentsEquivalenceTest {

    private val loc = Location("somefile.c", null, 10)
    private val tool1 = Tool("ToolA", null)
    private val tool2 = Tool("ToolB", null)
    private val tool3 = Tool("ToolC", null)
    private val tool4 = Tool("ToolD", null)

    private fun invariant(value: String, normvalue: String, tool: Tool, ast: Expression) = InvariantComponent(
        type = "loop_invariant",
        location = loc,
        value = value,
        format = "c_expression",
        tool = tool,
        normValue = normvalue,
        originalInvariantValue = value,
        ast = ast
    )

    private val `data==2 ast` = BinaryExpression(Var("data"), "==", Const("0"), "data == 2")
    private val `i==2 ast` = BinaryExpression(Var("i"), "==", Const("0"), "i == 2")

    private val inv1 = invariant("data == 2", "data == 2", tool1, `data==2 ast`)
    private val inv2 = invariant("data == 2", "data == 2", tool2, `data==2 ast`)
    private val inv3 = invariant("2 == data", "data == 2", tool3, `data==2 ast`)
    private val inv4 = invariant("i == 0", "i == 0", tool4, `i==2 ast`)
    private val inv44 = invariant("i == 0", "i == 0", tool4, `i==2 ast`)

    private val typeEnv: TypeEnv = mapOf(10 to mapOf("data" to CType.INT, "i" to CType.INT),)

    @Test
    fun testEquivalentInvariantGroups() {
        val invariantComponentsByLoc: LocToInvariantComponents = mutableMapOf(
            loc to mutableListOf(inv1, inv2, inv3, inv4)
        )
        val equivalenceClasses = computeEqualInvariantGroups(invariantComponentsByLoc, typeEnv)

        assertEquals(2, equivalenceClasses.size)

        val largestGroup = equivalenceClasses.maxByOrNull { it.equalInvariantComponents.size }!!
        val singletonGroup = equivalenceClasses.first { it.equalInvariantComponents.size == 1 }

        // Largest group should include data == 2, 2 == data, and data == 2
        assertEquals(3, largestGroup.equalInvariantComponents.size)
        assert(largestGroup.equalInvariantComponents.any { it.value.contains("data == 2") })
        assert(largestGroup.equalInvariantComponents.any { it.value.contains("2 == data") })

        // Singleton group should be inv4 (i == 0)
        assertEquals(1, singletonGroup.equalInvariantComponents.size)
        assert(singletonGroup.equalInvariantComponents.single().value.contains("i == 0"))
    }

    @Test
    fun testEquivalentInvariantGroupsDismissDuplicates() {
        val invariantComponentsByLoc: LocToInvariantComponents = mutableMapOf(
            loc to mutableListOf(inv1, inv2, inv4, inv44)
        )
        val equivalenceClasses = computeEqualInvariantGroups(invariantComponentsByLoc, typeEnv)

        assertEquals(2, equivalenceClasses.size)

        val largestGroup = equivalenceClasses.maxByOrNull { it.equalInvariantComponents.size }!!
        val singletonGroup = equivalenceClasses.first { it.equalInvariantComponents.size == 1 }

        // Largest group should include data == 2, 2 == data, and data == 2
        assertEquals(2, largestGroup.equalInvariantComponents.size)
        assert(largestGroup.equalInvariantComponents.any { it.value.contains("data == 2") })

        // Singleton group should be inv4 (i == 0)
        assertEquals(1, singletonGroup.equalInvariantComponents.size)
        assert(singletonGroup.equalInvariantComponents.single().value.contains("i == 0"))
    }

}