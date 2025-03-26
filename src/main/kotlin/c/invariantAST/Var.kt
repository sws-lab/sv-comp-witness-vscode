package c.invariantAST

class Var(val name: String) : Expression() {
    override val abstractNodeList: List<AbstractNode> = listOf(dataNode(name))
}