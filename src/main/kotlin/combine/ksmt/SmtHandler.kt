package combine.ksmt

import c.invariantAST.*
import io.ksmt.KContext
import io.ksmt.expr.KExpr
import io.ksmt.solver.KSolverStatus
import io.ksmt.solver.z3.KZ3Solver
import io.ksmt.sort.KBoolSort
import io.ksmt.sort.KIntSort
import io.ksmt.utils.mkConst

fun impliesSat(ctx: KContext, formula1: KExpr<KBoolSort>, formula2: KExpr<KBoolSort>): Boolean {
    val negated = ctx.mkNot(ctx.mkImplies(formula1, formula2))
    return KZ3Solver(ctx).use { solver ->
        solver.assert(negated)
        when (solver.check()) {
            KSolverStatus.UNSAT -> true
            else -> false
        }
    }
}

fun createSMTArithExpr(invariantAst: Expression, ctx: KContext, locTypeEnv: Map<String, CType>): KExpr<KIntSort> {
    return when (invariantAst) {
        is Var -> getVarSort(ctx, locTypeEnv, invariantAst.name)
        is Const -> ctx.mkIntNum(invariantAst.value)
        is BinaryExpression -> {
            when (invariantAst.op.name) {
                "+" -> ctx.mkArithAdd(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "-" -> ctx.mkArithSub(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "*" -> ctx.mkArithMul(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "/" -> ctx.mkArithDiv(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                ">", "<", ">=", "<=", "!=", "==", "&&", "||" ->
                    convertBoolExprToIntExpr(createSMTBoolExpr(invariantAst, ctx, locTypeEnv), ctx)

                else -> error("Unsupported SMTArith expression: ${invariantAst.toValue()}")
            }
        }

        is UnaryExpression ->
            when (val op = invariantAst.op) {
                is CastOp -> getVarSort(ctx, locTypeEnv, invariantAst.exp.toValue())
                is UnaryOp -> {
                    when (op.name) {
                        "-" -> ctx.mkArithUnaryMinus(createSMTArithExpr(invariantAst.exp, ctx, locTypeEnv))
                        "!" -> convertBoolExprToIntExpr(createSMTBoolExpr(invariantAst, ctx, locTypeEnv), ctx)
                        else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
                    }
                }
                is BinaryOp -> TODO()
            }

        else -> error("Unsupported SMTArith expression: ${invariantAst.toValue()}")
    }
}

fun createSMTBoolExpr(invariantAst: Expression, ctx: KContext, locTypeEnv: Map<String, CType>): KExpr<KBoolSort> {
    return when (invariantAst) {
        is Var -> convertIntExprToBoolExpr(getVarSort(ctx, locTypeEnv, invariantAst.name), ctx)
        is Const -> convertIntExprToBoolExpr(ctx.mkIntNum(invariantAst.value), ctx)
        is BinaryExpression -> {
            when (invariantAst.op.name) {
                ">" -> ctx.mkArithGt(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "<" -> ctx.mkArithLt(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                ">=" -> ctx.mkArithGe(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "<=" -> ctx.mkArithLe(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "==" -> ctx.mkEq(
                    createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "!=" -> ctx.mkNot(
                    ctx.mkEq(
                        createSMTArithExpr(invariantAst.left, ctx, locTypeEnv),
                        createSMTArithExpr(invariantAst.right, ctx, locTypeEnv)
                    )
                )

                "&&" -> ctx.mkAnd(
                    createSMTBoolExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTBoolExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "||" -> ctx.mkOr(
                    createSMTBoolExpr(invariantAst.left, ctx, locTypeEnv),
                    createSMTBoolExpr(invariantAst.right, ctx, locTypeEnv)
                )

                "+", "-", "*", "/" -> convertIntExprToBoolExpr(createSMTArithExpr(invariantAst, ctx, locTypeEnv), ctx)

                else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
            }
        }

        is UnaryExpression ->
            when (val op = invariantAst.op) {
                is CastOp -> convertIntExprToBoolExpr(getVarSort(ctx, locTypeEnv, invariantAst.exp.toValue()), ctx)
                is UnaryOp -> {
                    when (op.name) {
                        "!" -> ctx.mkNot(createSMTBoolExpr(invariantAst.exp, ctx, locTypeEnv))
                        "-" -> convertIntExprToBoolExpr(createSMTArithExpr(invariantAst, ctx, locTypeEnv), ctx)
                        else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
                    }
                }
                is BinaryOp -> TODO()
            }

        else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
    }
}

enum class CType {
    INT, CHAR, DOUBLE, LONG;

    companion object {
        fun fromSimpleType(type: String): CType? =
            when (type.lowercase()) {
                "int", "unsigned int" -> INT
                "long", "unsigned long", "long long", "unsigned long long" -> LONG
                "char", "unsigned char" -> CHAR
                "double" -> DOUBLE
                else -> null
            }
    }
}

fun getVarSort(ctx: KContext, typeEnv: Map<String, CType>, name: String): KExpr<KIntSort> {
    return when (typeEnv[name]) {
        CType.INT, CType.LONG, CType.CHAR -> ctx.mkIntSort().mkConst(name) // treat CHAR as int
        //CType.DOUBLE -> ctx.mkRealSort().mkConst(name)
        else -> error("Unsupported variable type for '$name: ${typeEnv[name]}'")
    }
}

fun convertIntExprToBoolExpr(intSort: KExpr<KIntSort>, ctx: KContext) =
    ctx.mkDistinct(listOf(intSort, ctx.mkIntNum(0)))

fun convertBoolExprToIntExpr(expr: KExpr<KBoolSort>, ctx: KContext) =
    ctx.mkIte(expr, ctx.mkIntNum("1"), ctx.mkIntNum("0"))
