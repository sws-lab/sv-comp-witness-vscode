package witnesses.data.yaml

import c.VariableMapping

data class Invariant(
    val type: String,
    val location: Location,
    val value: String,
    val format: String,
    var decomposedConjunctionMap: VariableMapping?
)
