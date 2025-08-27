package c.invariantAST

import kotlinx.serialization.Serializable

@Serializable
data class Var(val name: String) : Expression() {
    override val abstractNodeList: List<AbstractNode> = listOf(dataNode(name))
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
    override fun normalize() = this
    override fun toCode() = name
    override fun toValue() = name
    override fun nodeOpStr() = "var"
}