package c.invariantAST

abstract class Node : AbstractNode() {
    companion object {

        fun variable(name: String) = Var(name)

        fun constant(value: String) = Const(value)

        fun binary(left: Expression, op: String, right: Expression, str: String) =
            BinaryExpression(left, op, right, str)

        fun unary(op: String, exp: Expression, str: String) =
            UnaryExpression(op, exp, str)
    }

    abstract fun <T> accept(visitor: InvariantAstVisitor<T>): T
}
