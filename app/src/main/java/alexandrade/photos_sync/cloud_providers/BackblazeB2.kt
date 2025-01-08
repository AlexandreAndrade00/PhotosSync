package alexandrade.photos_sync.cloud_providers

import alexandrade.photos_sync.database.entities.CloudProviders
import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.database.entities.Remote
import alexandrade.photos_sync.database.entities.SyncStatus
import alexandrade.photos_sync.utils.calculateSHA1
import alexandrade.photos_sync.utils.getImageBytes
import alexandrade.photos_sync.utils.encodeToBase64
import alexandrade.photos_sync.utils.saveImageToMediaStore
import alexandrade.photos_sync.utils.sha1ToHex
import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.util.UUID

private class B2Client(
    val authorizationToken: String,
    val apiUrl: String,
    val accountId: String,
    val client: HttpClient
)


class BackblazeB2(val remote: Remote) :
    CloudProvider {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun getRemoteImagesIds(): List<UUID> {
        val url: String = "/b2api/v3/b2_list_file_names"

        val response = scope.async(Dispatchers.IO) {
            val b2Client = getClient(scope, remote.apiKeyId, remote.apiKey).await()

            return@async b2Client.client.post(b2Client.apiUrl + url) {
                headers {
                    append(HttpHeaders.Authorization, b2Client.authorizationToken)
                }
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("bucketId", remote.bucketId)
                    }
                )
            }
        }.await()

        val body: JsonElement = response.body()

        val files = body.jsonObject.getValue("files").jsonArray

        val imagesIds = mutableListOf<UUID>()

        files.forEach { file ->
            val fileName: String = file.jsonObject.getValue("fileName").jsonPrimitive.content

            imagesIds.add(UUID.fromString(fileName))
        }

        return imagesIds
    }

    override suspend fun uploadImages(images: List<Image>, context: Context): List<Deferred<UUID>> {
        val uploadUrlUrl = "/b2api/v3/b2_get_upload_url"

        val jobs: MutableList<Deferred<UUID>> = mutableListOf()

        images.forEach { image ->
            jobs.add(scope.async(Dispatchers.IO) {
                val b2Client = getClient(scope, remote.apiKeyId, remote.apiKey).await()

                val response = scope.async(Dispatchers.IO) {
                    val b2Client = getClient(scope, remote.apiKeyId, remote.apiKey).await()

                    return@async b2Client.client.get(b2Client.apiUrl + uploadUrlUrl) {
                        headers {
                            append(HttpHeaders.Authorization, b2Client.authorizationToken)
                        }
                        url {
                            parameters.append("bucketId", remote.bucketId)
                        }
                    }
                }.await()

                val body: JsonElement = response.body()

                val uploadUrl: String = body.jsonObject.getValue("uploadUrl").jsonPrimitive.content
                val authorizationToken: String =
                    body.jsonObject.getValue("authorizationToken").jsonPrimitive.content

                val imageBytes = getImageBytes(image.localPath!!, context)
                val imageSHA1 = calculateSHA1(imageBytes)

                val uploadResponse = b2Client.client.post(uploadUrl) {
                    headers {
                        append(HttpHeaders.Authorization, authorizationToken)
                        append(HttpHeaders.ContentType, image.contentType)
                        append("X-Bz-File-Name", image.uuid.toString())
                        append(HttpHeaders.ContentLength, imageBytes.size.toString())
                        append("X-Bz-Content-Sha1", sha1ToHex(imageSHA1!!))
                    }
                    setBody(imageBytes)
                }

                if (uploadResponse.status != HttpStatusCode.OK) {
                    throw Error("Image upload failed")
                }

                return@async image.uuid
            })
        }

        return jobs
    }

    override suspend fun downloadImages(
        imagesIds: List<UUID>,
        context: Context
    ): List<Deferred<Image>> {
        val downloadUrl = "/file"

        val images: MutableList<Deferred<Image>> = mutableListOf()

        imagesIds.forEach { imageId ->
            images.add(scope.async(Dispatchers.IO) {
                val b2Client = getClient(scope, remote.apiKeyId, remote.apiKey).await()

                val downloadResponse = b2Client.client.get(b2Client.apiUrl + downloadUrl) {
                    url {
                        appendPathSegments(remote.name, imageId.toString())
                    }
                    headers {
                        append(HttpHeaders.Authorization, b2Client.authorizationToken)
                    }
                }

                val imageBytes: ByteArray = downloadResponse.body()

                val contentType: String = downloadResponse.headers[HttpHeaders.ContentType]!!

                val uri = saveImageToMediaStore(
                    context,
                    imageBytes,
                    imageId.toString(),
                    contentType
                )

                return@async Image(imageId, SyncStatus.BOTH, uri, imageId.toString(), contentType)
            })
        }

        return images
    }

    companion object {
        private const val BASE_URL = "https://api.backblazeb2.com"

        suspend fun createBucket(apiKeyId: String, apiKey: String, name: String): Remote {
            val scope = CoroutineScope(Dispatchers.IO + Job())

            val url: String = "/b2api/v3/b2_create_bucket"

            try {
                val bucketId: String = scope.async {
                    val b2Client = getClient(scope, apiKeyId, apiKey).await()

                    try {
                        val response = b2Client.client.post(b2Client.apiUrl + url) {
                            headers {
                                append(HttpHeaders.Authorization, b2Client.authorizationToken)
                            }
                            contentType(ContentType.Application.Json)
                            setBody(
                                buildJsonObject {
                                    put("accountId", b2Client.accountId)
                                    put("bucketName", name)
                                    put("bucketType", "allPrivate")
                                    putJsonObject("defaultServerSideEncryption") {
                                        put("mode", "SSE-B2")
                                        put("algorithm", "AES256")
                                    }
                                }
                            )
                        }

                        val body: JsonElement = response.body()

                        return@async body.jsonObject.getValue("bucketId").jsonPrimitive.content
                    } catch (e: ClientRequestException) {
                        val url = "/b2api/v3/b2_list_buckets"

                        val response = b2Client.client.post(b2Client.apiUrl + url) {
                            headers {
                                append(HttpHeaders.Authorization, b2Client.authorizationToken)
                            }
                            contentType(ContentType.Application.Json)
                            setBody(
                                buildJsonObject {
                                    put("accountId", b2Client.accountId)
                                }
                            )
                        }

                        val body: JsonElement = response.body()

                        val bucketJson =
                            body.jsonObject.getValue("buckets").jsonArray.first { elem ->
                                elem.jsonObject.getValue("bucketName").jsonPrimitive.content == name
                            }

                        return@async bucketJson.jsonObject.getValue("bucketId").jsonPrimitive.content
                    }
                }.await()

                return Remote(name, CloudProviders.BACKBLAZE, apiKeyId, apiKey, bucketId, false)
            } finally {
                scope.cancel()
            }
        }

        private fun getClient(
            scope: CoroutineScope,
            apiKeyId: String,
            apiKey: String
        ): Deferred<B2Client> {
            val url = "/b2api/v3/b2_authorize_account"

            val client = HttpClient(CIO) {
                expectSuccess = true
                install(HttpTimeout)
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
            }

            return scope.async(Dispatchers.IO) {
                val response: HttpResponse = client.get(BASE_URL + url) {
                    headers {
                        append(
                            HttpHeaders.Authorization,
                            "Basic" + encodeToBase64("$apiKeyId:$apiKey")
                        )
                    }
                }

                val jsonElement: JsonElement = response.body()

                val authorizationToken =
                    jsonElement.jsonObject.getValue("authorizationToken").jsonPrimitive.content

                val accountId =
                    jsonElement.jsonObject.getValue("accountId").jsonPrimitive.content

                val apiUrl =
                    jsonElement
                        .jsonObject.getValue("apiInfo")
                        .jsonObject.getValue("storageApi")
                        .jsonObject.getValue("apiUrl")
                        .jsonPrimitive.content

                return@async B2Client(authorizationToken, apiUrl, accountId, client)
            }
        }
    }

    override fun cancel() {
        scope.cancel()
    }
}