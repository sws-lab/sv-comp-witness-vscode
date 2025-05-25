package witnesses

import combine.ksmt.CType
import combine.types.TypeEnv
import combine.types.VariableType
import combine.types.VariableTypeHandler.extractTypeEnvByLocation
import combine.types.VariableTypeMap
import kotlin.test.Test
import kotlin.test.assertEquals

object InvariantVariableTypeTest {

    @Test
    fun test_typeEnv() {
        val variableTypeMap: VariableTypeMap = mapOf(
            "someFile" to mapOf(
                42 to mapOf(
                    0 to listOf(VariableType("x", "INT")),
                    1 to listOf(VariableType("y", "CHAR"))
                ),
                43 to mapOf(
                    0 to listOf(VariableType("x", "DOUBLE"))
                )
            )
        )
        val expected: TypeEnv = mapOf(
            42 to mapOf("x" to CType.INT, "y" to CType.CHAR),
            43 to mapOf("x" to CType.DOUBLE)
        )
        assertEquals(expected, extractTypeEnvByLocation(variableTypeMap))
    }
}