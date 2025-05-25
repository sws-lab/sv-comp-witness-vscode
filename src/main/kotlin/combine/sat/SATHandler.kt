package combine.sat

import io.ksmt.KContext
import io.ksmt.expr.KExpr
import io.ksmt.solver.KSolverStatus
import io.ksmt.solver.z3.KZ3Solver
import io.ksmt.sort.KBoolSort

fun impliesSat(ctx: KContext, formula1: KExpr<KBoolSort>, formula2: KExpr<KBoolSort>): Boolean {
    val negated = ctx.mkNot(ctx.mkImplies(formula1, formula2))
    val solver = KZ3Solver(ctx)

    solver.assert(negated)
    return when (solver.check()) {
        KSolverStatus.UNSAT -> true
        else -> false
    }
}
