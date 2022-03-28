package xyz.mastriel.hostiletakeover.discord.commands

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.Command
import dev.minn.jda.ktx.interactions.updateCommands
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.mastriel.hostiletakeover.UserSettings
import xyz.mastriel.hostiletakeover.discord.Discord
import xyz.mastriel.hostiletakeover.serializers.MutableBoolean
import kotlin.reflect.full.createInstance

sealed class HTCommand {
    abstract val command : SubcommandData

    abstract suspend fun execute(e: SlashCommandInteractionEvent)

    open val requiredConsent : MutableBoolean? = null

    companion object {
        fun registerAllSubcommands() {
            val username = UserSettings().username.value
            Discord.guilds
                .filterNotNull()
                .forEach { guild ->

                    val command = Command("control-${username.lowercase()}", "Control $username's computer.") {
                        val subcommands = subcommandInstances
                            .filter { it.requiredConsent?.value != false }
                            .map {
                                println("Registering ${it::class.simpleName} for guild ${guild.name}...")
                                it.command
                            }
                        addSubcommands(*subcommands.toTypedArray())
                    }
                    guild.updateCommands {
                        addCommands(command)
                    }.queue()
                }
        }

        val subcommandInstances: List<HTCommand> = listOf(
            ScreenshotCommand
        )

        suspend fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            val username = UserSettings().username.value
            if (event.name != "control-${username.lowercase()}") return
            for (subcommand in subcommandInstances) {
                if (event.subcommandName == subcommand.command.name) {
                    if (subcommand.requiredConsent?.value == false) {
                        event.replyEmbeds(
                            Embed {
                                title = "Feature Disabled"
                                color = 0xf55142
                                description = "$username has disabled this feature, meaning it cannot be used. Try to convince them to enable it!"
                            }
                        ).setEphemeral(true).queue()
                        return
                    }
                    subcommand.execute(event)
                }
            }
        }
    }
}