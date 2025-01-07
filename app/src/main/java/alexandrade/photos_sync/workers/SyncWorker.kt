package alexandrade.photos_sync.workers

import alexandrade.photos_sync.cloud_providers.CloudProvider
import alexandrade.photos_sync.database.entities.Remote
import alexandrade.photos_sync.database.entities.SyncStatus
import alexandrade.photos_sync.repository.MediaRepository
import alexandrade.photos_sync.repository.RemotesRepository
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class SyncWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            performSynchronization()

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun performSynchronization() {
        val remotesRepository = RemotesRepository(appContext)
        val imagesRepository = MediaRepository(appContext)

        val remote: Remote? = remotesRepository.getPrincipalRemote()

        if (remote == null) return

        val cloudProvider: CloudProvider = remote.getStrategy()

        try {
            val remoteImagesIds = cloudProvider.getRemoteImagesIds()

            val localImages = imagesRepository.getImagesByStatus(SyncStatus.LOCAL)

            val remoteAndLocalImagesIds =
                imagesRepository.getImagesByStatus(SyncStatus.BOTH).map { image -> image.uuid }

            val imagesToDownload = remoteImagesIds.minus(remoteAndLocalImagesIds)

            val uploadedImagesIds = cloudProvider.uploadImages(localImages, appContext)

            val downloadedImages = cloudProvider.downloadImages(imagesToDownload, appContext)

            val uploadJobs: MutableList<Job> = mutableListOf()
            val downloadJobs: MutableList<Job> = mutableListOf()

            supervisorScope {
                uploadedImagesIds.forEach { imageFuture ->
                    uploadJobs.add(launch(Dispatchers.IO) {
                        val image = imageFuture.await()

                        imagesRepository.updateImageStatus(image, SyncStatus.BOTH)
                    })
                }

                downloadedImages.forEach { imageFuture ->
                    downloadJobs.add(launch(Dispatchers.IO) {
                        val image = imageFuture.await()

                        imagesRepository.addImage(image)
                    })
                }
            }

            uploadJobs.joinAll()
            downloadJobs.joinAll()
        } finally {
            cloudProvider.cancel()
        }
    }
}

//class ProgressUpdaterImpl(private val context: Context, workerParams: WorkerParameters) : ProgressUpdater {
//    private val notificationManager = NotificationManagerCompat.from(context)
//    private val notificationChannelId = "image_sync_channel"
//    private val notificationId = 1
//
//    init {
//        createNotificationChannel()
//    }
//
//    override fun updateProgress(context: Context, id: UUID, data: Data): ListenableFuture<Void> {
//        val future = SettableFuture.create<Void>()
//        val latch = CountDownLatch(1)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val progress = data.getInt("progress", 0)
//            val message = data.getString("message") ?: ""
//            val notification: NotificationCompat.Builder = createNotification(progress, message)
//            notificationManager.notify(notificationId, notification.build())
//            latch.countDown()
//        }
//
//        CoroutineScope(Dispatchers.Default).launch {
//            latch.await(10, TimeUnit.SECONDS)
//            future.set(null)
//        }
//
//        return future
//    }
//
//    private fun createNotification(progress: Int, message: String): NotificationCompat.Builder {
//        val intent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        return NotificationCompat.Builder(context, notificationChannelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
//            .setContentTitle("Image Sync")
//            .setContentText(message)
//            .setProgress(100, progress, false)
//            .setOngoing(true)
//            .setContentIntent(pendingIntent)
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "Image Sync Channel"
//            val descriptionText = "Channel for image sync progress"
//            val importance = NotificationManager.IMPORTANCE_LOW
//            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
//                description = descriptionText
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//}