package alexandrade.photos_sync.utils

import java.util.Base64

fun encodeToBase64(input: String): String {
    val bytes = input.toByteArray()
    val encodedBytes = Base64.getEncoder().encode(bytes)
    return String(encodedBytes)
}