package c.invariantAST

class Const(val value: String) : Statement() {
    override val abstractNodeList: List<AbstractNode> = listOf(dataNode(value))
}