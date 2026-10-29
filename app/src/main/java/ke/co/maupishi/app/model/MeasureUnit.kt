package ke.co.maupishi.app.model

/** How a unit is used in the kitchen. Local units have no fixed metric size. */
enum class UnitGroup { METRIC, KITCHEN, LOCAL, COUNTING }

/**
 * Units an ingredient quantity can use. Named `MeasureUnit` to avoid a clash
 * with `kotlin.Unit`. The constant names are stored codes (see
 * [RecipeSource] for the rule).
 */
enum class MeasureUnit(val group: UnitGroup) {
    G(UnitGroup.METRIC),
    KG(UnitGroup.METRIC),
    ML(UnitGroup.METRIC),
    L(UnitGroup.METRIC),

    TSP(UnitGroup.KITCHEN),
    TBSP(UnitGroup.KITCHEN),
    CUP(UnitGroup.KITCHEN),

    KIKOMBE(UnitGroup.LOCAL),
    HANDFUL(UnitGroup.LOCAL),
    PINCH(UnitGroup.LOCAL),
    TIN(UnitGroup.LOCAL),
    DEBE(UnitGroup.LOCAL),

    PIECE(UnitGroup.COUNTING),
    BUNCH(UnitGroup.COUNTING),
    CLOVE(UnitGroup.COUNTING),
    ;

    val isLocal: Boolean get() = group == UnitGroup.LOCAL
}
