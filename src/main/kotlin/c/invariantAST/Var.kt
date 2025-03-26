package c.invariantAST

class Var(val name: String) : Statement() {
    override val abstractNodeList: List<AbstractNode> = listOf(dataNode(name));
}