package lattice

import c.CInvariantAst
import combine.sat.CType
import combine.sat.createSAT
import combine.sat.impliesSat
import io.ksmt.KContext
import io.ksmt.expr.KExpr
import io.ksmt.sort.KBoolSort
import kotlin.test.Test
import kotlin.test.assertFalse

object SatTest {

    @Test
    fun test_sat_comparison() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSAT(CInvariantAst.createAst("x > 1"), ctx, typeEnv) as KExpr<KBoolSort>
            val formula2 = createSAT(CInvariantAst.createAst("x >= 0"), ctx, typeEnv) as KExpr<KBoolSort>
            val implies = impliesSat(ctx, formula1, formula2)
            assert(implies)
        }
    }

    @Test
    fun test_sat_equality() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSAT(CInvariantAst.createAst("x > 0"), ctx, typeEnv) as KExpr<KBoolSort>
            val formula2 = createSAT(CInvariantAst.createAst("x >= 1"), ctx, typeEnv) as KExpr<KBoolSort>
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assert(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_inequality() {
        val typeEnv: Map<String, CType> = mapOf("x" to CType.INT)
        KContext().use { ctx ->
            val formula1 = createSAT(CInvariantAst.createAst("x > 2"), ctx, typeEnv) as KExpr<KBoolSort>
            val formula2 = createSAT(CInvariantAst.createAst("x >= 0"), ctx, typeEnv) as KExpr<KBoolSort>
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assertFalse(implies1 && implies2)
        }
    }

    @Test
    fun test_sat_equality_cpa_gob() {
        val typeEnv: Map<String, CType> = mapOf("i" to CType.INT, "m" to CType.INT)
        KContext().use { ctx ->
            val formula1 =
                createSAT(CInvariantAst.createAst("m > 0 || i >= m && m > 0"), ctx, typeEnv) as KExpr<KBoolSort>
            val formula2 = createSAT(CInvariantAst.createAst("m >= 1"), ctx, typeEnv) as KExpr<KBoolSort>
            val implies1 = impliesSat(ctx, formula1, formula2)
            val implies2 = impliesSat(ctx, formula2, formula1)
            assert(implies1 && implies2)
        }
    }

}