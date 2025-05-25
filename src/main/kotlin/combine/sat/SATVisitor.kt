package combine.sat

import c.invariantAST.*
import io.ksmt.KContext
import io.ksmt.expr.KExpr
import io.ksmt.sort.KBoolSort
import io.ksmt.sort.KIntSort
import io.ksmt.utils.mkConst

fun createSMTArithExpr(invariantAst: Expression, ctx: KContext, typeEnv: Map<String, CType>): KExpr<KIntSort> {
    return when (invariantAst) {
        is Var -> getVarSort(ctx, typeEnv, invariantAst.name)
        is Const -> ctx.mkIntNum(invariantAst.value)
        is BinaryExpression -> {
            when (invariantAst.op) {
                "+" -> ctx.mkArithAdd(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                "-" -> ctx.mkArithSub(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                "*" -> ctx.mkArithMul(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                "/" -> ctx.mkArithDiv(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                ">", "<", ">=", "<=", "!=", "==", "&&", "||" ->
                    convertBoolExprToIntExpr(createSMTBoolExpr(invariantAst, ctx, typeEnv), ctx)

                else -> error("Unsupported SMTArith expression: ${invariantAst.toValue()}")
            }
        }

        is UnaryExpression ->
            when (invariantAst.op) {
                "-" -> ctx.mkArithUnaryMinus(createSMTArithExpr(invariantAst.exp, ctx, typeEnv))
                "!" -> convertBoolExprToIntExpr(createSMTBoolExpr(invariantAst, ctx, typeEnv), ctx)
                else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
            }

        else -> error("Unsupported SMTArith expression: ${invariantAst.toValue()}")
    }
}

fun createSMTBoolExpr(invariantAst: Expression, ctx: KContext, typeEnv: Map<String, CType>): KExpr<KBoolSort> {
    return when (invariantAst) {
        is Var -> convertIntExprToBoolExpr(getVarSort(ctx, typeEnv, invariantAst.name), ctx)
        is Const -> convertIntExprToBoolExpr(ctx.mkIntNum(invariantAst.value), ctx)
        is BinaryExpression -> {
            when (invariantAst.op) {
                ">" -> ctx.mkArithGt(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                "<" -> ctx.mkArithLt(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                ">=" -> ctx.mkArithGe(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                "<=" -> ctx.mkArithLe(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                "==" -> ctx.mkEq(
                    createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                    createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                )

                "!=" -> ctx.mkNot(
                    ctx.mkEq(
                        createSMTArithExpr(invariantAst.left, ctx, typeEnv),
                        createSMTArithExpr(invariantAst.right, ctx, typeEnv)
                    )
                )

                "&&" -> ctx.mkAnd(
                    createSMTBoolExpr(invariantAst.left, ctx, typeEnv),
                    createSMTBoolExpr(invariantAst.right, ctx, typeEnv)
                )

                "||" -> ctx.mkOr(
                    createSMTBoolExpr(invariantAst.left, ctx, typeEnv),
                    createSMTBoolExpr(invariantAst.right, ctx, typeEnv)
                )

                "+", "-", "*", "/" -> convertIntExprToBoolExpr(createSMTArithExpr(invariantAst, ctx, typeEnv), ctx)

                else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
            }
        }

        is UnaryExpression ->
            when (invariantAst.op) {
                "!" -> ctx.mkNot(createSMTBoolExpr(invariantAst.exp, ctx, typeEnv))
                "-" -> convertIntExprToBoolExpr(createSMTArithExpr(invariantAst, ctx, typeEnv), ctx)
                else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
            }

        else -> error("Unsupported SMTBool expression: ${invariantAst.toValue()}")
    }
}

enum class CType {
    INT, CHAR, DOUBLE;

    companion object {
        fun fromSimpleType(type: String): CType? = when (type) {
            "INT" -> INT
            "CHAR" -> CHAR
            "DOUBLE" -> DOUBLE
            else -> null
        }
    }

}

fun getVarSort(ctx: KContext, typeEnv: Map<String, CType>, name: String): KExpr<KIntSort> {
    return when (typeEnv[name]) {
        CType.INT, CType.CHAR -> ctx.mkIntSort().mkConst(name) // treat CHAR as int
        //CType.DOUBLE -> ctx.mkRealSort().mkConst(name)
        else -> error("Unsupported variable type for '$name'")
    }
}

fun convertIntExprToBoolExpr(intSort: KExpr<KIntSort>, ctx: KContext) =
    ctx.mkDistinct(listOf(intSort, ctx.mkIntNum(0)))

fun convertBoolExprToIntExpr(expr: KExpr<KBoolSort>, ctx: KContext) =
    ctx.mkIte(expr, ctx.mkIntNum("1"), ctx.mkIntNum("0"))
