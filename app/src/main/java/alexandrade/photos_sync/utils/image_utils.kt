package alexandrade.photos_sync.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

fun sha1ToHex(hash: ByteArray): String {
    return hash.joinToString("") { String.format("%02x", it) }
}

fun calculateSHA1(byteArray: ByteArray): ByteArray? {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    return messageDigest.digest(byteArray)
}

suspend fun getImageBytes(imagePath: Uri, context: Context): ByteArray {
    return withContext(Dispatchers.IO) {
        return@withContext context.contentResolver.openInputStream(imagePath)!!.readBytes()
    }
}