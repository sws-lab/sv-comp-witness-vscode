package witnesses

import combine.ksmt.CType
import combine.types.TypeEnv
import combine.types.VariableType
import combine.types.VariableTypeHandler.extractTypeEnvByLocation
import combine.types.VariableTypeHandler.getVariableTypesForProgram
import combine.types.VariableTypeHandler.serializeTypesForProgram
import combine.types.VariableTypeMap
import kotlin.test.Test
import kotlin.test.assertEquals

object InvariantVariableTypeTest {

    @Test
    fun test() {
        println(getVariableTypesForProgram("./examples/safe-program-example.c", "variable_type.json"))
    }

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

    @Test
    fun test_reading_typeEnv() {
        val typesJsonString =
            this::class.java.classLoader.getResource("exampleVariableTypeMaps/variableTypeMap1.json")
                ?.readText() ?: ""
        val variableTypeMap = serializeTypesForProgram(typesJsonString)
        val expected: TypeEnv = mapOf(
            32 to mapOf("i" to CType.INT, "n" to CType.CHAR, "s" to CType.INT, "l" to CType.LONG, "c" to CType.CHAR),
            10 to mapOf("n" to CType.CHAR),
            16 to mapOf("n" to CType.CHAR, "c" to CType.CHAR)
        )
        assertEquals(expected, extractTypeEnvByLocation(variableTypeMap))
    }
}