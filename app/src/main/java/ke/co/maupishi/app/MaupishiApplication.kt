package ke.co.maupishi.app

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import ke.co.maupishi.app.data.SeedResult
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Creates the [AppContainer] and loads the bundled recipes in the background
 * at every process start. A failed load never stops the app: it keeps the
 * recipes it already has and works offline as before.
 */
class MaupishiApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch { seedContent() }
    }

    private suspend fun seedContent() {
        // Debug builds sync every time, because placeholder recipes change without a version bump.
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        try {
            when (val result = container.contentSeeder.seed(forceSync = debuggable)) {
                SeedResult.UpToDate -> Log.i(TAG, "Bundled content is up to date")
                is SeedResult.Applied ->
                    Log.i(TAG, "Loaded ${result.recipeCount} bundled recipes, removed ${result.removedCount}")
                is SeedResult.Failed ->
                    Log.e(TAG, "Loading bundled content failed, nothing changed:\n" + result.errors.joinToString("\n"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Loading bundled content failed", e)
        }
    }

    private companion object {
        const val TAG = "Maupishi"
    }
}
