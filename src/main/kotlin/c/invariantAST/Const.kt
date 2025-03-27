package c.invariantAST

class Const(val value: String) : Expression() {
    override val abstractNodeList: List<AbstractNode> = listOf(dataNode(value))
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
}