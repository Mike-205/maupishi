package ke.co.maupishi.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Favorites, newest first. Never touched by content updates. */
@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Upsert
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE recipeId = :recipeId")
    suspend fun remove(recipeId: String)
}

@Dao
interface ContentMetaDao {

    @Query("SELECT value FROM content_meta WHERE name = :name")
    suspend fun get(name: String): String?

    @Upsert
    suspend fun put(entry: ContentMetaEntity)
}
