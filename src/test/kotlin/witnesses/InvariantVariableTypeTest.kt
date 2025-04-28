package witnesses

import c.VariableTypeHandler.getVariableTypesForProgram
import org.junit.jupiter.api.Test

object InvariantVariableTypeTest {

    @Test
    fun test() {
        val variableTypesForProgram =
            getVariableTypesForProgram("./examples/safe-program-example.c", "variableTypes.json")
    }
}