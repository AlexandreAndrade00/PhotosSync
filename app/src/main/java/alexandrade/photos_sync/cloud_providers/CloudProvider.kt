package alexandrade.photos_sync.cloud_providers

import alexandrade.photos_sync.database.entities.Image
import android.content.Context
import kotlinx.coroutines.Deferred
import java.util.UUID

interface CloudProvider {
    suspend fun getRemoteImagesIds(): List<UUID>?

    suspend fun uploadImages(images: List<Image>, context: Context): List<Deferred<UUID?>>

    suspend fun downloadImages(imagesIds: List<UUID>, context: Context): List<Deferred<Image?>>

    fun cancel()
}