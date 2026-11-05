package ke.co.maupishi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A recipe the user marked as a favorite.
 *
 * There is deliberately no foreign key to `recipes`: a content update that
 * replaces or removes a recipe must never delete a favorite. A favorite whose
 * recipe is gone is simply not shown.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val recipeId: String,
    /** When it was added, in epoch milliseconds. */
    val addedAt: Long,
)

/** A small key-value row about the stored content, such as the last seeded content version. */
@Entity(tableName = "content_meta")
data class ContentMetaEntity(
    @PrimaryKey val name: String,
    val value: String,
)
