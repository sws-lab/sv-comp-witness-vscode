package ksmt

import c.CInvariantAst
import combine.ksmt.CType
import combine.ksmt.createSMTBoolExpr
import combine.ksmt.impliesSat
import io.ksmt.KContext
import kotlin.test.Test
import kotlin.test.assertFalse

object SmtTest {

    private fun invariantsAreEqual(inv1: String, inv2: String, typeEnv: Map<String, CType>): Boolean {
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst(inv1), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst(inv2), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            return implies1 && implies2
        }
    }

    @Test
    fun test_sat_implication() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x > 1"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("x >= 0"), ctx, typeEnv)
            val implies = impliesSat(ctx, formula1, formula2)
            assert(implies)
        }
    }

    @Test
    fun test_unsat_implication() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x > 0"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("x >= 2"), ctx, typeEnv)
            val implies = impliesSat(ctx, formula1, formula2)
            assertFalse(implies)
        }
    }

    @Test
    fun test_comparison() {
        val typeEnv = mapOf("x" to CType.INT)
        assert(invariantsAreEqual("x > 0", "x >= 1", typeEnv))
        assertFalse(invariantsAreEqual("x > 2", "x >= 0", typeEnv))
    }

    @Test
    fun test_arithmetic_and_bool_combinations() {
        val typeEnv = mapOf("x" to CType.INT, "y" to CType.INT)
        assert(invariantsAreEqual("x + y > x - 0", "0 < y", typeEnv))
        assert(invariantsAreEqual("!(x > 5 || y <= 3)", "(! (x > 5)) && (! (y <= 3))", typeEnv))
        assert(invariantsAreEqual("x && y", "y && x", typeEnv))
        assertFalse(invariantsAreEqual("x + y && y + x", "y && x", typeEnv))
    }

    @Test
    fun test_tool_examples() {
        assert(invariantsAreEqual("m > 0 || i >= m && m > 0", "m >= 1", mapOf("i" to CType.INT, "m" to CType.INT)))
    }

    @Test
    fun create_smt_expressions() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT, "y" to CType.INT, "z" to CType.INT)
        KContext().use { ctx ->
            createSMTBoolExpr(CInvariantAst.createAst("x + (y && z)"), ctx, typeEnv)
            createSMTBoolExpr(CInvariantAst.createAst("x + !y"), ctx, typeEnv)
        }
    }


}