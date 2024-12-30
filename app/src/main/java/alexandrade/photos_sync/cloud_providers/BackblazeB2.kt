package alexandrade.photos_sync.cloud_providers

import alexandrade.photos_sync.database.entities.CloudProviders
import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.database.entities.Remote
import com.backblaze.b2.client.B2StorageClient
import com.backblaze.b2.client.B2StorageClientFactory
import com.backblaze.b2.client.contentSources.B2FileContentSource
import com.backblaze.b2.client.structures.B2ListFileNamesRequest
import com.backblaze.b2.client.structures.B2UploadFileRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class BackblazeB2(val appKeyId: String, val appKey: String, val bucketId: String) : CloudProvider {
    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    private fun getClient(): B2StorageClient {
        return B2StorageClientFactory.createDefaultFactory()
            .create(appKeyId, appKey, "android/photos_sync");
    }

    private fun <T> runInClient(block: (client: B2StorageClient) -> T): T {
        val client = getClient()

        try {
            return block(client)
        } finally {
            client.close()
        }
    }

    override suspend fun getRemoteImagesIds(): List<UUID> {
        val files = scope.async {
            return@async runInClient { client ->
                return@runInClient client.fileNames(
                    B2ListFileNamesRequest.builder(bucketId).build()
                )
            }
        }.await()

        if (files == null) {
            throw Exception("Bucket with id $bucketId doesn't exist or exist problems retrieving the files")
        }

        val imagesIds = mutableListOf<UUID>()

        files.forEach { file ->
            if (file == null) {
                throw Exception("File in iterable doesn't exist")
            }

            if (file.fileName == null) {
                throw Exception("File doesn't has a name")
            }

            val fileName = file.fileName

            imagesIds.add(UUID.fromString(fileName))
        }

        return imagesIds
    }

    override fun uploadImages(images: List<Image>) {
        images.forEach { image ->
            val req: B2UploadFileRequest = B2UploadFileRequest.builder(
                bucketId, image.uuid.toString(), "image",
                B2FileContentSource.build(File(image.localPath!!.path!!))
            ).build()


            scope.launch {
                runInClient { client -> client.uploadSmallFile(req) }
            }
        }
    }

    override fun createBucket(): Remote {
        val bucket = runInClient { client -> client.createBucket("photos_sync", "allPrivate") }

        return Remote("photos_sync", CloudProviders.BACKBLAZE, appKeyId, appKey, bucket.bucketId!!)
    }

    fun stopCoroutine() {
        job.cancel()
    }

}