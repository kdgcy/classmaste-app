package com.kd.classmate.appsetting

// Enum representing the available font sizes
enum class FontSize {
    SMALL, MEDIUM, LARGE, HUGE;

    // Helper property to get the readable display name
    val displayName: String
        get() = when (this) {
            SMALL -> "Small"
            MEDIUM -> "Medium"
            LARGE -> "Large"
            HUGE -> "Huge"
        }

    // Scale factor applied to the entire theme's text size
    val scaleFactor: Float
        get() = when (this) {
            SMALL -> 0.9f
            MEDIUM -> 1.0f // Default scale
            LARGE -> 1.2f
            HUGE -> 1.4f
        }
}