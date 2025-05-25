package lattice

import c.CInvariantAst
import combine.ksmt.CType
import combine.ksmt.createSMTBoolExpr
import combine.ksmt.impliesSat
import io.ksmt.KContext
import kotlin.test.Test
import kotlin.test.assertFalse

object SatTest {

    @Test
    fun test_sat_comparison() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x > 1"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("x >= 0"), ctx, typeEnv)
            val implies = impliesSat(ctx, formula1, formula2)
            assert(implies)
        }
    }

    @Test
    fun test_sat_equality() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x > 0"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("x >= 1"), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assert(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_inequality() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x > 2"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("x >= 0"), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assertFalse(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_equality_cpa_gob() {
        val typeEnv: Map<String, CType> = mapOf("i" to CType.INT, "m" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("m > 0 || i >= m && m > 0"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("m >= 1"), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assert(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_arithmetic() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT, "y" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x + y > x - 0"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("0 < y"), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assert(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_unary() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT, "y" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("!(x > 5 || y <= 3)"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("(! (x > 5)) && (! (y <= 3))"), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assert(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_bool_on_integers() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT, "y" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x && y"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("y && x"), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assert(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_bool_on_integers2() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT, "y" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSMTBoolExpr(CInvariantAst.createAst("x + y && y + x"), ctx, typeEnv)
            val formula2 = createSMTBoolExpr(CInvariantAst.createAst("y && x"), ctx, typeEnv)
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assertFalse(implies1 && implies2)
        }
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