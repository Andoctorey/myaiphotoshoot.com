package ai.create.photo.platform

import android.os.Build

class AndroidPlatform : Platform {
    override val platform = Platforms.ANDROID
    override val name = "Android ${Build.VERSION.SDK_INT}"
}

actual fun platform(): Platform = AndroidPlatform()