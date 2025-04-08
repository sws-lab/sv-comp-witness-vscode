package c

import c.invariantAST.*

typealias VariableMapping = MutableMap<Var, MutableList<String>>

fun collectVariables(invariantASt: Node): Set<Var> = object : InvariantAstVisitor<Set<Var>>() {

    override fun visit(variable: Var): Set<Var> =
        setOf(variable)

    override fun visit(constant: Const): Set<Var> =
        setOf()

    override fun visit(binop: BinaryExpression): Set<Var> {
        return visit(binop.left) + visit(binop.right)
    }

    override fun visit(unop: UnaryExpression): Set<Var> {
        return visit(unop.exp)
    }

    override fun visit(ternop: TernaryExpression): Set<Var> {
        return visit(ternop.fst) + visit(ternop.snd) + visit(ternop.thrd)
    }

    override fun visit(postfix: PostfixExpression): Set<Var> {
        return visit(postfix.expression) + visit(postfix.postfixExpression)
    }
}.visit(invariantASt)

fun collectMapping(invariantASt: Node): VariableMapping = object : InvariantAstVisitor<VariableMapping>() {

    override fun visit(variable: Var): VariableMapping =
        mutableMapOf()

    override fun visit(constant: Const): VariableMapping =
        mutableMapOf()

    override fun visit(binop: BinaryExpression): VariableMapping {
        if (binop.op == "&&") {
            return (visit(binop.left).toList() + visit(binop.right).toList())
                .groupBy { (k, _) -> k }
                .mapValues { (_, vs) ->
                    vs.map { (_, v) -> v }
                        .reduce { a, b -> (a + b).toMutableList() }
                }.toMutableMap()
        } else {
            val mapping: VariableMapping = mutableMapOf()
            val vars = collectVariables(binop.left) + collectVariables(binop.right)
            val str = binop.str
            vars.forEach { variable ->
                mapping.computeIfAbsent(variable) { mutableListOf() }.add(str)
            }
            return mapping
        }
    }

    override fun visit(unop: UnaryExpression): VariableMapping =
        mutableMapOf()

    override fun visit(ternop: TernaryExpression): VariableMapping {
        TODO("Not yet implemented")
    }

    override fun visit(postfix: PostfixExpression): VariableMapping {
        TODO("Not yet implemented")
    }
}.visit(invariantASt)
