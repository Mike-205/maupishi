package ke.co.maupishi.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's single database: bundled recipes, favorites and content metadata.
 * The schema is exported to `app/schemas` on every build and committed, so a
 * future version can be migrated without losing user data.
 */
@Database(
    entities = [
        RecipeEntity::class,
        RecipeCategoryEntity::class,
        RecipeOccasionEntity::class,
        RecipeAllergenEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        FavoriteEntity::class,
        ContentMetaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MaupishiDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun contentMetaDao(): ContentMetaDao
}
