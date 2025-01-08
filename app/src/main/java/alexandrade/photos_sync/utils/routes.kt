package alexandrade.photos_sync.utils

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object SettingsRoute

@Serializable
data object CloudAccountsRoute

@Serializable
data object SynchronizationRoute

@Serializable
data class ImageScreenRoute(val imageId: String)

@Serializable
data object AddCloudRoute