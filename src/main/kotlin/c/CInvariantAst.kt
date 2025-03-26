package c

import InvariantCBaseVisitor
import InvariantCLexer
import InvariantCParser
import c.invariantAST.*
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

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
        //println(tree.toStringTree(parser))
        return StatementVisitor().visit(tree)
    }

}

private class StatementVisitor : InvariantCBaseVisitor<Expression>() {
    override fun visitInvariant(ctx: InvariantCParser.InvariantContext): Expression =
        visitExpression(ctx.expression())

    override fun visitExpression(ctx: InvariantCParser.ExpressionContext): Expression =
        visitRelationalExpression(ctx.relationalExpression())

    override fun visitRelationalExpression(ctx: InvariantCParser.RelationalExpressionContext): Expression {
        var node = visit(ctx.primaryExpression().first())
        for (expressionContext in ctx.primaryExpression()) {
            node = BinaryExpression(node, ctx.op.text, visit(expressionContext))
        }
        return node
    }

    override fun visitIdent(ctx: InvariantCParser.IdentContext) =
        Var(ctx.Identifier().text)

    override fun visitCons(ctx: InvariantCParser.ConsContext) =
        Const(ctx.Constant().text)

}