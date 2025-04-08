package c.invariantAST

abstract class InvariantAstVisitor<T> {

    abstract fun visit(variable: Var): T
    abstract fun visit(constant: Const): T
    abstract fun visit(binop: BinaryExpression): T
    abstract fun visit(unop: UnaryExpression): T
    abstract fun visit(ternop: TernaryExpression): T
    abstract fun visit(postfix: PostfixExpression): T

    fun visit(node: Node): T {
        return node.accept(this)
    }
}
