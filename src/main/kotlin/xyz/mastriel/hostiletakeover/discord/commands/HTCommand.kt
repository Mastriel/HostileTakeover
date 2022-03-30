package xyz.mastriel.hostiletakeover.discord.commands

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.Command
import dev.minn.jda.ktx.interactions.updateCommands
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.mastriel.hostiletakeover.discord.Discord
import xyz.mastriel.hostiletakeover.discord.HTBot
import xyz.mastriel.hostiletakeover.discord.getTimeRepresentation
import xyz.mastriel.hostiletakeover.serializers.MutableBoolean
import java.time.Instant
import kotlin.math.floor

sealed class HTCommand {
    abstract val command : SubcommandData

    abstract suspend fun execute(e: SlashCommandInteractionEvent)

    open val requiredConsent : MutableBoolean? = null

    val cooldowns : HashMap<User, Instant> = hashMapOf()

    val fullCommandName get() = "/control-${HTBot.username.lowercase()} ${command.name}"

    companion object {

        fun registerAllSubcommands() {
            val username = HTBot.username
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
            ScreenshotCommand,
            TerminalCommand
        )

        suspend fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            val username = HTBot.username
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
                        HTBot.warn("${event.user.asTag} tried to run ${subcommand.fullCommandName}, but couldn't because it's disabled!")
                        return
                    }
                    val expiry = subcommand.cooldowns[event.user] ?: Instant.now().minusMillis(1000)
                    if (expiry.isAfter(Instant.now())) {
                        val difference = expiry.epochSecond - Instant.now().toEpochMilli()
                        val time = getTimeRepresentation(difference)
                        event.replyEmbeds(
                            Embed {
                                title = "Command on Cooldown"
                                color = 0xf55142
                                description = "You cannot use this command for another `$time`."
                            }
                        ).setEphemeral(true).queue()
                        HTBot.warn("${event.user.asTag} tried to run ${subcommand.fullCommandName}, but couldn't because they were on cooldown!")
                        return

                    }
                    subcommand.execute(event)
                }
            }
        }
    }
}