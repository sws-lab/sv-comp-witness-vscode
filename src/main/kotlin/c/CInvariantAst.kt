package c

import InvariantCBaseVisitor
import InvariantCLexer
import InvariantCParser
import c.invariantAST.*
import org.antlr.v4.runtime.*

object CInvariantAst {
    fun createAst(program: String): Node {
        val lexer = InvariantCLexer(CharStreams.fromString(program)).apply {
            removeErrorListeners()
            //addErrorListener(ExceptionErrorListener())
        }

        val parser = InvariantCParser(CommonTokenStream(lexer)).apply {
            removeErrorListeners()
            errorHandler = BailErrorStrategy()
        }

        val tree = parser.invariant()
        println(tree.toStringTree(parser))
        return StatementVisitor().visit(tree)
    }

}

private class StatementVisitor : InvariantCBaseVisitor<Expression>() {
    override fun visitInvariant(ctx: InvariantCParser.InvariantContext) =
        visitExpression(ctx.expression())

    override fun visitExpression(ctx: InvariantCParser.ExpressionContext) =
        visitLogicalOrExpression(ctx.logicalOrExpression())

    override fun visitRelationalExpression(ctx: InvariantCParser.RelationalExpressionContext) =
        visitBinary(ctx.primaryExpression(), ctx.op)

    override fun visitEqualityExpression(ctx: InvariantCParser.EqualityExpressionContext) =
        visitBinary(ctx.relationalExpression(), ctx.op)

    override fun visitLogicalAndExpression(ctx: InvariantCParser.LogicalAndExpressionContext) =
        visitBinary(ctx.equalityExpression(), ctx.op)

    override fun visitLogicalOrExpression(ctx: InvariantCParser.LogicalOrExpressionContext) =
        visitBinary(ctx.logicalAndExpression(), ctx.op)

    override fun visitIdent(ctx: InvariantCParser.IdentContext) =
        Var(ctx.Identifier().text)

    override fun visitCons(ctx: InvariantCParser.ConsContext) =
        Const(ctx.Constant().text)


    private fun visitBinary(ctx: List<ParserRuleContext>, ops: List<Token>) : Expression {
        var node = visit(ctx.first())
        for ((op, exp) in ops.zip(ctx.drop(1))) {
            node = BinaryExpression(node, op.text, visit(exp))
        }
        return node
    }
}