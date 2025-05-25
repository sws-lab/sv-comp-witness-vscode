package combine.sat

import c.invariantAST.*
import io.ksmt.KContext
import io.ksmt.expr.KExpr
import io.ksmt.sort.KArithSort
import io.ksmt.sort.KBoolSort
import io.ksmt.sort.KSort
import io.ksmt.utils.mkConst

abstract class TypedInvariantAstVisitor<T> {
    abstract fun visit(variable: Var): T
    abstract fun visit(constant: Const): T
    abstract fun visit(binop: BinaryExpression): T
    abstract fun visit(unop: UnaryExpression): T
    abstract fun visit(ternop: TernaryExpression): T

    fun visit(expr: Expression): T = when (expr) {
        is Var -> visit(expr)
        is Const -> visit(expr)
        is BinaryExpression -> visit(expr)
        is UnaryExpression -> visit(expr)
        is TernaryExpression -> visit(expr)
        is Type -> error("Unsupported node type: Type")
    }
}

fun createSAT(invariantAst: Expression, ctx: KContext, typeEnv: Map<String, CType>): KExpr<out KSort> {
    return object : TypedInvariantAstVisitor<KExpr<out KSort>>() {
        override fun visit(variable: Var) = getVarSort(ctx, typeEnv, variable.name)

        override fun visit(constant: Const) = ctx.mkIntNum(constant.value)

        override fun visit(binop: BinaryExpression): KExpr<out KSort> {
            val left = visit(binop.left)
            val right = visit(binop.right)

            return when (binop.op) {
                ">" -> ctx.mkArithGt(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                "<" -> ctx.mkArithLt(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                ">=" -> ctx.mkArithGe(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                "<=" -> ctx.mkArithLe(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                "==" -> ctx.mkEq(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                "!=" -> ctx.mkNot(ctx.mkEq(left as KExpr<KArithSort>, right as KExpr<KArithSort>))
                "&&" -> ctx.mkAnd(left as KExpr<KBoolSort>, right as KExpr<KBoolSort>)
                "||" -> ctx.mkOr(left as KExpr<KBoolSort>, right as KExpr<KBoolSort>)
                "+" -> ctx.mkArithAdd(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                "-" -> ctx.mkArithSub(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                "*" -> ctx.mkArithMul(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                "/" -> ctx.mkArithDiv(left as KExpr<KArithSort>, right as KExpr<KArithSort>)
                else -> error("Unsupported binary operator: ${binop.op}")
            }
        }

        override fun visit(unop: UnaryExpression): KExpr<*> {
            val expr = visit(unop.exp)
            return when (unop.op) {
                "!" -> ctx.mkNot(expr as KExpr<KBoolSort>)
                "-" -> ctx.mkArithUnaryMinus(expr as KExpr<KArithSort>)
                else -> error("Unsupported unary operator: ${unop.op}")
            }
        }

        override fun visit(ternop: TernaryExpression): KExpr<KSort> = error("Ternary expressions not supported")

    }.visit(invariantAst)
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

fun getVarSort(ctx: KContext, typeEnv: Map<String, CType>, name: String): KExpr<KSort> {
    return when (typeEnv[name]) {
        CType.INT, CType.CHAR -> ctx.mkIntSort().mkConst(name) // treat CHAR as int
        else -> error("Unsupported variable type for '$name'")
    }
}

