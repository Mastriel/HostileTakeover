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

    private fun repeatingTask(updateInterval: Long, task: () -> Unit) : Int {
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