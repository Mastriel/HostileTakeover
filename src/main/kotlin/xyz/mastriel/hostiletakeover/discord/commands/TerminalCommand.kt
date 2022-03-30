package xyz.mastriel.hostiletakeover.discord.commands

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.Subcommand
import dev.minn.jda.ktx.interactions.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.mastriel.hostiletakeover.ConsoleLine
import xyz.mastriel.hostiletakeover.UserSettings
import xyz.mastriel.hostiletakeover.discord.HTBot
import xyz.mastriel.hostiletakeover.serializers.MutableBoolean

object TerminalCommand : HTCommand() {
    override val command: SubcommandData get() =
        Subcommand("terminal", "Run a command using ${HTBot.username}'s command processor.") {
            option<String>("command", "The command to run.", true)
        }

    override val requiredConsent: MutableBoolean
        get() = UserSettings().allowTerminal

    override suspend fun execute(e: SlashCommandInteractionEvent) {
        val command = e.getOption("command")?.asString ?: return
        HTBot.log("${e.user.asTag} ran a native OS command! ($command)", ConsoleLine.Severity.WARN)

        withContext(Dispatchers.IO) {
            Runtime.getRuntime().exec(command)
        }

        e.replyEmbeds(Embed {
            color = 0x85ff78
            title = "Command `$command` ran successfully!"
            description = "The command `$command` ran successfully on ${HTBot.username}'s machine!"
        }).queue()

    }
}