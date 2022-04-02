package xyz.mastriel.hostiletakeover.discord.commands

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.Subcommand
import dev.minn.jda.ktx.interactions.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.mastriel.hostiletakeover.ConsoleLine
import xyz.mastriel.hostiletakeover.UserSettings
import xyz.mastriel.hostiletakeover.discord.HTBot
import xyz.mastriel.hostiletakeover.serializers.MutableBoolean
import java.io.IOException

object TerminalCommand : HTCommand() {
    override val command: SubcommandData get() =
        Subcommand("terminal", "Run a command using ${HTBot.username}'s command processor.") {
            option<String>("command", "The command to run.", true)
        }

    override val requiredConsent: MutableBoolean
        get() = UserSettings().allowTerminal

    override val cooldown: Long
        get() = UserSettings().terminalCooldown.value

    override suspend fun execute(e: SlashCommandInteractionEvent) {
        val command = e.getOption("command")?.asString ?: return
        if (command.contains("reboot")) {
            e.replyEmbeds(Embed {
                color = 0xf55142
                title = "Error while running `$command`!"
                description = "This command is potentially harmful to ${HTBot.username}'s device!"
            }).setEphemeral(true).queue()

            return
        }
        HTBot.log("${e.user.asTag} ran a native OS command! ($command)", ConsoleLine.Severity.WARN)
        println(0)

        val value = withTimeoutOrNull(500L) {
            withContext(Dispatchers.IO) {
                try {
                    Runtime.getRuntime().exec(command)
                } catch (e: IOException) {
                    return@withContext null
                }
            }
        }
        if (value == null) {
            e.replyEmbeds(Embed {
                color = errorColor
                title = "Error while running `$command`!"
                description = "The command processor did not respond!"
            }).setEphemeral(true).queue()
            return
        }

        e.replyEmbeds(Embed {
            color = successColor
            title = "Command `$command` ran successfully!"
            description = "The command `$command` ran successfully on ${HTBot.username}'s machine!"
        }).queue()

    }
}