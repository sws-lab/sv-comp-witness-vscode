package witnesses.data.invariant

import c.invariantAST.Expression
import witnesses.data.run.Tool
import witnesses.data.yaml.Location

data class InvariantPiece(
    val type: String,
    val location: Location,
    val value: String,
    val format: String,
    val tool: Tool,
    val normValue: String,
    val originalValue: String,
    val ast: Expression
)
