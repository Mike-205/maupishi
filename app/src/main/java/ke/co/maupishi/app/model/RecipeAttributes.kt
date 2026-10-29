package ke.co.maupishi.app.model

// The constant names below are the codes saved in recipe files and, from M1b,
// in the database. Renaming one breaks stored data: add a new constant instead.
// `RecipeAttributesTest` pins the names.

/** Where a recipe came from. Content updates only touch [BUNDLED]. */
enum class RecipeSource { BUNDLED, USER, COMMUNITY }

/** The part of Kenya a dish comes from. [NATIONWIDE] is for dishes eaten everywhere. */
enum class Region {
    COAST,
    CENTRAL,
    EASTERN,
    WESTERN,
    NYANZA,
    RIFT_VALLEY,
    NORTH_EASTERN,
    NAIROBI,
    NATIONWIDE,
}

enum class Category { BREAKFAST, MAIN, SIDE, SNACK, DRINK, DESSERT, SOUP_STEW }

enum class Occasion {
    CHRISTMAS,
    EID,
    WEDDING,
    FUNERAL,
    EVERYDAY,
    BUDGET_MEAL,
    STUDENT_FRIENDLY,
    SCHOOL_HOLIDAY,
    SPECIAL_GUEST,
}

enum class Allergen {
    PEANUTS,
    TREE_NUTS,
    DAIRY,
    EGGS,
    GLUTEN,
    FISH,
    SHELLFISH,
    SOY,
    SESAME,
}
