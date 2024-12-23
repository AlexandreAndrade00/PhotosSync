package alexandrade.photos_sync.database.type_converters

import android.net.Uri
import androidx.room.TypeConverter

class UriTypeConverter {
    @TypeConverter
    fun fromString(value: String?): Uri? = value?.let { Uri.parse(it) }

    @TypeConverter
    fun toString(uri: Uri?): String? = uri?.toString()
}