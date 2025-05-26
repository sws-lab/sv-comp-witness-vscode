package witnesses

import c.invariantAST.Const
import witnesses.WitnessComparison.computeEqualInvariantGroups
import witnesses.data.invariant.InvariantComponent
import witnesses.data.run.Tool
import witnesses.data.yaml.Location
import kotlin.test.Test
import kotlin.test.assertEquals

object InvariantComponentsEquivalenceTest {

    private val loc = Location("somefile.c", null, 10)
    private val tool1 = Tool("ToolA", null)
    private val tool2 = Tool("ToolB", null)
    private val tool3 = Tool("ToolC", null)
    private val tool4 = Tool("ToolD", null)

    private fun invariant(value: String, normvalue: String, tool: Tool = tool1) = InvariantComponent(
        type = "loop_invariant",
        location = loc,
        value = value,
        format = "c_expression",
        tool = tool,
        normValue = normvalue,
        originalValue = value,
        ast = Const("0")
    )

    private val inv1 = invariant("data == 2", "data == 2", tool1)
    private val inv2 = invariant("data == 2", "data == 2", tool2)
    private val inv3 = invariant("2 == data", "data == 2", tool3)
    private val inv4 = invariant("i == 0", "i == 0", tool4)

    private val invariantComponentsByLoc: LocToInvariantComponents = mutableMapOf(
        loc to mutableListOf(inv1, inv2, inv3, inv4)
    )

    @Test
    fun testEquivalentInvariantGroups() {
        val equivalenceClasses = computeEqualInvariantGroups(invariantComponentsByLoc)

        // Check that we get 2 SCCs
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

}