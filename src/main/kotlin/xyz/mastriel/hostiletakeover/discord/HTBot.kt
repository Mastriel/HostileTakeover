package xyz.mastriel.hostiletakeover.discord

import androidx.compose.runtime.mutableStateOf
import dev.minn.jda.ktx.injectKTX
import dev.minn.jda.ktx.listener
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.DisconnectEvent
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import xyz.mastriel.hostiletakeover.UserSettings
import xyz.mastriel.hostiletakeover.discord.commands.HTCommand
import java.lang.Exception
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextUInt


lateinit var Discord : JDA
    private set

val discordInitialized : Boolean = ::Discord.isInitialized

var status = mutableStateOf(JDA.Status.SHUTDOWN)

object HTBot {

    fun start() : Boolean {
        try {
            if (::Discord.isInitialized) {
                Discord.shutdown()
            }
            val token = UserSettings().token.value
            val username = UserSettings().username.value
            Discord = JDABuilder.createDefault(token)
                .injectKTX()
                .build()
                .awaitReady()

            Discord.listener<SlashCommandInteractionEvent> { HTCommand.onSlashCommandInteraction(it) }
            repeatingTask(50L) { status.value = Discord.status }

            Discord.presence.activity = Activity.of(Activity.ActivityType.WATCHING, "$username.")
            HTCommand.registerAllSubcommands()

            return true
        } catch (e: Exception) {
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
}