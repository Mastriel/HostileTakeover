package xyz.mastriel.hostiletakeover

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.mastriel.hostiletakeover.serializers.MutableBoolean
import xyz.mastriel.hostiletakeover.serializers.MutableLong
import xyz.mastriel.hostiletakeover.serializers.MutableString
import java.io.File

@Serializable
open class UserSettings private constructor() {
    val allowScreenshots: MutableBoolean = mutableStateOf(false)
    val screenshotCooldown: MutableLong = mutableStateOf(15000L)

    val allowCamera: MutableBoolean = mutableStateOf(false)
    val allowScripting: MutableBoolean = mutableStateOf(false)

    val allowTerminal: MutableBoolean = mutableStateOf(false)
    val terminalCooldown: MutableLong = mutableStateOf(60000L)

    val allowTyping: MutableBoolean = mutableStateOf(false)
    val allowInputs: MutableBoolean = mutableStateOf(false)
    val username: MutableString = mutableStateOf("Default")
    val token: MutableString = mutableStateOf("")

    fun save() {
        val data = json.encodeToString(this)
        file.writeText(data)
    }

    companion object {
        private val file = File(System.getProperty("user.home"), "hostile-takeover-config.json")
        private val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        private val data : UserSettings by lazy {
            file.parentFile.mkdirs()
            file.createNewFile()
            try {
                val decode = json.decodeFromString<UserSettings>(file.readText())
                decode.username.value = decode.username.value.take(20)
                decode
            } catch (e: SerializationException) {
                UserSettings()
            }
        }
        operator fun invoke() = data

    }
}