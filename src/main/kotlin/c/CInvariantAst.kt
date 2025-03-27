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

    override fun visitUnary(ctx: InvariantCParser.UnaryContext) =
        UnaryExpression(ctx.op.text, visit(ctx.castExpression()))

    override fun visitUnarybase(ctx: InvariantCParser.UnarybaseContext) =
        visit(ctx.primaryExpression())

    override fun visitCast(ctx: InvariantCParser.CastContext) =
        UnaryExpression(ctx.typeName().text, visit(ctx.castExpression()))

    override fun visitCastbase(ctx: InvariantCParser.CastbaseContext) =
        visit(ctx.unaryExpression())

    override fun visitMultiplicativeExpression(ctx: InvariantCParser.MultiplicativeExpressionContext) =
        visitBinary(ctx.castExpression(), ctx.op)

    override fun visitAdditiveExpression(ctx: InvariantCParser.AdditiveExpressionContext) =
        visitBinary(ctx.multiplicativeExpression(), ctx.op)

    override fun visitRelationalExpression(ctx: InvariantCParser.RelationalExpressionContext) =
        visitBinary(ctx.additiveExpression(), ctx.op)

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

    override fun visitParens(ctx: InvariantCParser.ParensContext): Expression =
        visit(ctx.expression())

    override fun visitSpecifierQualifierList(ctx: InvariantCParser.SpecifierQualifierListContext) =
        visit(ctx.specifierQualifierList())

    override fun visitTypeName(ctx: InvariantCParser.TypeNameContext) =
        visitSpecifierQualifierList(ctx.specifierQualifierList())

    private fun visitBinary(ctx: List<ParserRuleContext>, ops: List<Token>): Expression {
        var node = visit(ctx.first())
        for ((op, exp) in ops.zip(ctx.drop(1))) {
            node = BinaryExpression(node, op.text, visit(exp))
        }
        return node
    }
}