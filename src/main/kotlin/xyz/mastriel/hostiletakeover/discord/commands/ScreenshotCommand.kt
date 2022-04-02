package xyz.mastriel.hostiletakeover.discord.commands

import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.Subcommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.mastriel.hostiletakeover.ConsoleLine
import xyz.mastriel.hostiletakeover.UserSettings
import xyz.mastriel.hostiletakeover.discord.HTBot
import xyz.mastriel.hostiletakeover.serializers.MutableBoolean
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


object ScreenshotCommand : HTCommand() {
    override val command: SubcommandData = Subcommand("screenshot", "Take a Screenshot")

    override val requiredConsent: MutableBoolean
        get() = UserSettings().allowScreenshots

    override val cooldown: Long
        get() = UserSettings().screenshotCooldown.value

    override suspend fun execute(e: SlashCommandInteractionEvent) {
        val bufferedImage = captureMainMonitor()

        val tempFile = File("./screenshot.png")

        withContext(Dispatchers.IO) {
            ImageIO.write(bufferedImage, "png", tempFile)
        }

        val edit = e.deferReply()
        edit.queue()

        e.hook.editOriginal(tempFile.readBytes(), "image.png").await()

        HTBot.log("${e.user.asTag} took a screenshot!", ConsoleLine.Severity.WARN)

        File("./screenshot.png").delete()
    }

    private fun captureMainMonitor() : BufferedImage {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val robot = Robot()
        return robot.createScreenCapture(ge.maximumWindowBounds)
    }
}