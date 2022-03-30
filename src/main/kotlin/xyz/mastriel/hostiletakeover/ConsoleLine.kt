package xyz.mastriel.hostiletakeover

import androidx.compose.ui.graphics.Color

data class ConsoleLine(val text: String, val color: Color) {

    constructor(text: String, severity: Severity) : this(text, severity.color)

    enum class Severity(val color: Color) {
        LOG(Color.White),
        WARN(Color.Yellow),
        ERROR(Color.Red)
    }
}
