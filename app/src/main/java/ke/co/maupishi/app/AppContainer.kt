package ke.co.maupishi.app

import android.content.Context
import androidx.room.Room
import ke.co.maupishi.app.content.AssetContentSource
import ke.co.maupishi.app.data.ContentSeeder
import ke.co.maupishi.app.data.MaupishiDatabase
import ke.co.maupishi.app.data.RecipeRepository
import ke.co.maupishi.app.data.RoomRecipeRepository

/**
 * Builds the app's long-lived objects once and hands them out: manual
 * dependency injection. [MaupishiApplication] holds the single instance.
 * Everything is created on first use, so starting the app stays cheap.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    // No destructive-migration fallback: it would delete favorites on a schema change.
    val database: MaupishiDatabase by lazy {
        Room.databaseBuilder(appContext, MaupishiDatabase::class.java, DATABASE_NAME).build()
    }

    val recipeRepository: RecipeRepository by lazy { RoomRecipeRepository(database.recipeDao()) }

    val contentSeeder: ContentSeeder by lazy {
        ContentSeeder(database, AssetContentSource(appContext.assets))
    }

    private companion object {
        const val DATABASE_NAME = "maupishi.db"
    }
}
