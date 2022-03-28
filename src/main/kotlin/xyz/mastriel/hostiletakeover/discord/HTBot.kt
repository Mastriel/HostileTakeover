package xyz.mastriel.hostiletakeover.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import xyz.mastriel.hostiletakeover.UserSettings
import java.lang.Exception


lateinit var Discord : JDA
    private set

object HTBot {

    fun start() : Boolean {
        try {
            if (::Discord.isInitialized) {
                Discord.shutdown()
            }
            val token = UserSettings().token.value
            val username = UserSettings().username.value
            Discord = JDABuilder.createDefault(token).build()

            Discord.presence.activity = Activity.of(Activity.ActivityType.WATCHING, "$username.")

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}