package ai.create.photo.platform


enum class Platforms {
    ANDROID, IOS, DESKTOP, WEB_DESKTOP, WEB_MOBILE
}

interface Platform {
    val platform: Platforms
    val name: String
}

expect fun platform(): Platform

