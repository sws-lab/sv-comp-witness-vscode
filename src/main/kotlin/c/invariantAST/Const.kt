package c.invariantAST

data class Const(val value: String) : Expression() {
    override val abstractNodeList: List<AbstractNode> = listOf(dataNode(value))
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
    override fun normalize() = this
    override fun toCode() = value
    override fun toValue() = value
}