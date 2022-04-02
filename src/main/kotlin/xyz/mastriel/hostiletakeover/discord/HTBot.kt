package xyz.mastriel.hostiletakeover.discord

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import dev.minn.jda.ktx.injectKTX
import dev.minn.jda.ktx.listener
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import xyz.mastriel.hostiletakeover.ConsoleLine
import xyz.mastriel.hostiletakeover.UserSettings
import xyz.mastriel.hostiletakeover.consoleLines
import xyz.mastriel.hostiletakeover.discord.commands.HTCommand
import java.awt.event.KeyEvent
import kotlin.math.floor


lateinit var Discord : JDA
    private set

val discordInitialized : Boolean = ::Discord.isInitialized

var status = mutableStateOf(JDA.Status.SHUTDOWN)

object HTBot {

    var username : String = ""

    suspend fun start() : Boolean {
        try {
            if (UserSettings().username.value == "") {
                log("Username must not be blank!", ConsoleLine.Severity.ERROR)
                return false
            }
            if (::Discord.isInitialized) {
                log("Disabling previous bot instance...", ConsoleLine.Severity.WARN)
                Discord.shutdown()
            }
            val token = UserSettings().token.value
            this.username = UserSettings().username.value
            val thread = Thread {
                log("Logging into bot...")
                Discord = JDABuilder.createDefault(token)
                    .injectKTX()
                    .build()
                    .awaitReady()
                log("Bot enabled as $username!")

                Discord.listener<SlashCommandInteractionEvent> { HTCommand.onSlashCommandInteraction(it) }
                repeatingTask(50L) { status.value = Discord.status }

                Discord.presence.activity = Activity.of(Activity.ActivityType.WATCHING, "$username.")
                HTCommand.registerAllSubcommands()
            }
            return withContext(Dispatchers.IO) {
                thread.start()
                thread.join()
                return@withContext true
            }
        } catch (e: Exception) {
            log("Invalid Bot Token! (or no internet connection)", ConsoleLine.Severity.ERROR)
            e.printStackTrace()
            return false
        }
    }

    private val livingTasks = hashSetOf<Int>()
    private var lastTaskID = 0

    fun repeatingTask(updateInterval: Long, task: suspend () -> Unit) : Int {
        lastTaskID++
        val taskID = lastTaskID
        livingTasks += taskID
        CoroutineScope(Dispatchers.IO).launch {
            while (livingTasks.contains(taskID)) {
                delay(updateInterval)
                task()
            }
        }
        return taskID
    }

    fun log(text: String, severity: ConsoleLine.Severity=ConsoleLine.Severity.LOG) =
        consoleLines.add(ConsoleLine(text, severity))

    fun warn(text: String) =
        consoleLines.add(ConsoleLine(text, ConsoleLine.Severity.WARN))

    fun error(text: String) =
        consoleLines.add(ConsoleLine(text, ConsoleLine.Severity.ERROR))


    fun log(text: String, color: Color) =
        consoleLines.add(ConsoleLine(text, color))
}


fun getTimeRepresentation(long: Long) : String {
    val second = floor(long / 1000.0) % 60
    val minute = floor(long / 60000.0)
    return if (minute.toInt() != 0)
        "${minute.toInt()}m ${second.toInt()}s"
    else
        "${second.toInt()}s"

}

fun stringToKey(s: String) : Int {
    return when(s.lowercase()) {
        "a" -> KeyEvent.VK_A
        "b" -> KeyEvent.VK_B
        "c" -> KeyEvent.VK_C
        "d" -> KeyEvent.VK_D
        "e" -> KeyEvent.VK_E
        "f" -> KeyEvent.VK_F
        "g" -> KeyEvent.VK_G
        "h" -> KeyEvent.VK_H
        "i" -> KeyEvent.VK_I
        "j" -> KeyEvent.VK_J
        "k" -> KeyEvent.VK_K
        "l" -> KeyEvent.VK_L
        "m" -> KeyEvent.VK_M
        "n" -> KeyEvent.VK_N
        "o" -> KeyEvent.VK_O
        "p" -> KeyEvent.VK_P
        "q" -> KeyEvent.VK_Q
        "r" -> KeyEvent.VK_R
        "s" -> KeyEvent.VK_S
        "t" -> KeyEvent.VK_T
        "u" -> KeyEvent.VK_U
        "v" -> KeyEvent.VK_V
        "w" -> KeyEvent.VK_W
        "x" -> KeyEvent.VK_X
        "y" -> KeyEvent.VK_Y
        "z" -> KeyEvent.VK_Z

        "0" -> KeyEvent.VK_0
        "1" -> KeyEvent.VK_1
        "2" -> KeyEvent.VK_2
        "3" -> KeyEvent.VK_3
        "4" -> KeyEvent.VK_4
        "5" -> KeyEvent.VK_5
        "6" -> KeyEvent.VK_6
        "7" -> KeyEvent.VK_7
        "8" -> KeyEvent.VK_8
        "9" -> KeyEvent.VK_9

        "ctrl" -> KeyEvent.VK_CONTROL
        "shift" -> KeyEvent.VK_SHIFT
        "tab" -> KeyEvent.VK_TAB
        "|" -> KeyEvent.VK_ENTER
        "enter" -> KeyEvent.VK_ENTER
        "alt" -> KeyEvent.VK_ALT
        "caps" -> KeyEvent.VK_CAPS_LOCK
        "space" -> KeyEvent.VK_SPACE
        " " -> KeyEvent.VK_SPACE
        "esc" -> KeyEvent.VK_ESCAPE
        "backspace" -> KeyEvent.VK_BACK_SPACE
        "-" -> KeyEvent.VK_MINUS
        "=" -> KeyEvent.VK_EQUALS
        "up" -> KeyEvent.VK_UP
        "down" -> KeyEvent.VK_DOWN
        "left" -> KeyEvent.VK_LEFT
        "right" -> KeyEvent.VK_RIGHT
        "win" -> if (System.getProperty("os.name").lowercase().contains("windows"))
            KeyEvent.VK_WINDOWS else KeyEvent.VK_META


        "f1" -> KeyEvent.VK_F1
        "f2" -> KeyEvent.VK_F2
        "f3" -> KeyEvent.VK_F3
        "f4" -> KeyEvent.VK_F4
        "f5" -> KeyEvent.VK_F5
        "f6" -> KeyEvent.VK_F6
        "f7" -> KeyEvent.VK_F7
        "f8" -> KeyEvent.VK_F8
        "f9" -> KeyEvent.VK_F9
        "f10" -> KeyEvent.VK_F10
        "f11" -> KeyEvent.VK_F11
        "f12" -> KeyEvent.VK_F12

        "." -> KeyEvent.VK_PERIOD
        "/" -> KeyEvent.VK_SLASH
        "'" -> KeyEvent.VK_QUOTE
        ";" -> KeyEvent.VK_SEMICOLON
        "[" -> KeyEvent.VK_OPEN_BRACKET
        "]" -> KeyEvent.VK_CLOSE_BRACKET
        "<" -> KeyEvent.VK_LESS
        ">" -> KeyEvent.VK_GREATER
        ":" -> KeyEvent.VK_COLON
        "\"" -> KeyEvent.VK_QUOTEDBL
        "!" -> KeyEvent.VK_EXCLAMATION_MARK
        "@" -> KeyEvent.VK_AT






        else -> throw IllegalStateException("Invalid key called. ($s)")


        // .?/';\[\]|`~<>:"!@#$%^&*()-= ]+
    }
}
