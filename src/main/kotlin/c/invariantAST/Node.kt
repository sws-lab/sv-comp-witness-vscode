package c.invariantAST

abstract class Node : AbstractNode() {
    companion object {

        fun variable(name: String) = Var(name)

        fun constant(value: String) = Const(value)

        fun binary(left: Expression, op: String, right: Expression) = BinaryExpression(left, op, right)

        fun unary(op: String, exp: Expression) = UnaryExpression(op, exp)
    }

    abstract fun <T> accept(visitor: InvariantAstVisitor<T>): T
}
