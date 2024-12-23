package alexandrade.photos_sync.cloud_providers

import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.database.entities.Remote
import java.util.UUID

interface CloudProvider {
    suspend fun getRemoteImagesIds() : List<UUID>

    fun uploadImages(images: List<Image>)

    fun createBucket(): Remote
}