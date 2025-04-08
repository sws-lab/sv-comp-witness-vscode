package c.invariantAST

data class PostfixExpression(val expression: Expression, val postfixExpression: Expression, val str: String) :Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
}