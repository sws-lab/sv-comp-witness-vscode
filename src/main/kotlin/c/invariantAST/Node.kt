package c.invariantAST

open class Node : AbstractNode() {
    companion object {

        fun variable(name: String) = Var(name)

        fun constant(value: String) = Const(value)

        fun binary(left: Expression, op: String, right: Expression) = BinaryExpression(left, op, right)
    }
}
