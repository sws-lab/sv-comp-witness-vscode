package c.invariantAST

abstract class Node : AbstractNode() {
    companion object {

        fun variable(name: String) = Var(name)

        fun constant(value: String, suffix: String?) = Const(value, suffix)

        fun binary(left: Expression, op: String, right: Expression, str: String) =
            BinaryExpression(left, op, right, str)

        fun unary(op: UnaryOp, exp: Expression, str: String) =
            UnaryExpression(op, exp, str)

        fun ternary(fst: Expression, snd: Expression, thrd: Expression, str: String) =
            TernaryExpression(fst, snd, thrd, str)

    }

    abstract fun <T> accept(visitor: InvariantAstVisitor<T>): T
}
