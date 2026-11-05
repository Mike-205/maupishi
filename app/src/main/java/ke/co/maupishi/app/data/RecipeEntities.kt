package ke.co.maupishi.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * The stored shape of a recipe: one class per table, as described in
 * docs/DATA_MODEL.md. Enum values are saved as their constant names, so
 * renaming a constant breaks stored data. The Room schema is exported to
 * app/schemas and committed, and any change to these classes needs a new
 * schema version and a migration.
 */

/** One row per recipe. Texts that exist in two languages are stored as `...En` and `...Sw`. */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val source: String,
    val verified: Boolean,
    val nameEn: String,
    val nameSw: String?,
    val descriptionEn: String,
    val descriptionSw: String?,
    val region: String,
    val community: String?,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val servings: Int,
    val costMinKes: Int?,
    val costMaxKes: Int?,
    val costBasisEn: String?,
    val costBasisSw: String?,
    val safetyNoteEn: String?,
    val safetyNoteSw: String?,
    val culturalNoteEn: String?,
    val culturalNoteSw: String?,
    val credit: String,
    val imageFile: String?,
    val imageAltEn: String?,
    val imageAltSw: String?,
)

// The child tables below are bundled content, rewritten together with their
// recipe, so a foreign key with cascade delete is safe here (unlike favorites).

@Entity(
    tableName = "recipe_categories",
    primaryKeys = ["recipeId", "category"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("category")],
)
data class RecipeCategoryEntity(val recipeId: String, val category: String)

@Entity(
    tableName = "recipe_occasions",
    primaryKeys = ["recipeId", "occasion"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("occasion")],
)
data class RecipeOccasionEntity(val recipeId: String, val occasion: String)

@Entity(
    tableName = "recipe_allergens",
    primaryKeys = ["recipeId", "allergen"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("allergen")],
)
data class RecipeAllergenEntity(val recipeId: String, val allergen: String)

/** One ingredient line. [position] is the display order within the recipe. */
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId")],
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: String,
    val position: Int,
    val groupEn: String?,
    val groupSw: String?,
    val nameEn: String,
    val nameSw: String?,
    /** Null means "to taste"; [unit] is then null too. */
    val quantity: Double?,
    val unit: String?,
    val approxMetricAmount: Double?,
    val approxMetricUnit: String?,
    val prepNoteEn: String?,
    val prepNoteSw: String?,
    val substitutionEn: String?,
    val substitutionSw: String?,
)

/** One instruction. [position] is the display order within the recipe. */
@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId")],
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: String,
    val position: Int,
    val textEn: String,
    val textSw: String?,
    val timerSeconds: Int?,
)
