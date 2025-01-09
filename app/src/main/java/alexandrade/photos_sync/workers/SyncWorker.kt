package alexandrade.photos_sync.workers

import alexandrade.photos_sync.MainActivity
import alexandrade.photos_sync.cloud_providers.CloudProvider
import alexandrade.photos_sync.database.entities.Remote
import alexandrade.photos_sync.database.entities.SyncStatus
import alexandrade.photos_sync.repository.MediaRepository
import alexandrade.photos_sync.repository.RemotesRepository
import alexandrade.photos_sync.R
import alexandrade.photos_sync.database.entities.SyncHistory
import alexandrade.photos_sync.database.entities.SyncType
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.Date

class SyncWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val notificationManager =
        applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val notificationChannelId = "image_sync_channel"
    private val notificationId = 1

    init {
        createNotificationChannel()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                performSynchronization()
            }

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

            if (remoteImagesIds == null) return

            val localImages = imagesRepository.getImagesByStatus(SyncStatus.LOCAL)

            val remoteAndLocalImagesIds =
                imagesRepository.getImagesByStatus(SyncStatus.BOTH).map { image -> image.uuid }

            val imagesToDownload = remoteImagesIds.minus(remoteAndLocalImagesIds)

            val totalSyncFiles: Int = imagesToDownload.size + localImages.size

            val uploadedImagesIds = cloudProvider.uploadImages(localImages, appContext)

            val downloadedImages = cloudProvider.downloadImages(imagesToDownload, appContext)

            val uploadJobs: MutableList<Job> = mutableListOf()
            val downloadJobs: MutableList<Job> = mutableListOf()

            var syncedFiles: Int = 0

            fun updateNotification() {
                syncedFiles++

                if (syncedFiles % 10 == 0) {

                    val messase = "A sincronizar ficheiro $syncedFiles de $totalSyncFiles"
                    val progress = (syncedFiles.toFloat() / totalSyncFiles) * 100

                    showNotification(progress.toInt(), messase)
                }
            }

            supervisorScope {
                uploadedImagesIds.forEachIndexed { i, imageFuture ->
                    uploadJobs.add(launch(Dispatchers.IO) {
                        val image = imageFuture.await()

                        if (image == null) return@launch

                        imagesRepository.updateImageStatus(image, SyncStatus.BOTH)

                        updateNotification()
                    })
                }

                downloadedImages.forEachIndexed { i, imageFuture ->
                    downloadJobs.add(launch(Dispatchers.IO) {
                        val image = imageFuture.await()

                        if (image == null) return@launch

                        imagesRepository.addImage(image)

                        updateNotification()
                    })
                }
            }

            uploadJobs.joinAll()
            downloadJobs.joinAll()

            notificationManager.cancel(notificationId)

            imagesRepository.insertSyncHystory(
                SyncHistory(
                    date = Date(),
                    syncType = SyncType.REMOTE
                )
            )
        } finally {
            cloudProvider.cancel()
        }
    }

    private fun showNotification(progress: Int, message: String) {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("SyncWorker", "No permissions for post notification")

            return
        }

        val notification = createNotification(progress, message)

        notificationManager.notify(notificationId, notification.build())
    }

    private fun createNotification(progress: Int, message: String): NotificationCompat.Builder {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(appContext, notificationChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .setContentTitle("A sincronizar imagens...")
            .setContentText(message)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
    }

    private fun createNotificationChannel() {
        val name = "Images synchronization progress"
        val descriptionText = "Channel for images synchronization progress"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(notificationChannelId, name, importance).apply {
            description = descriptionText
        }

        notificationManager.createNotificationChannel(channel)
    }
}