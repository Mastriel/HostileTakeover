package xyz.mastriel.hostiletakeover.discord.commands

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.Subcommand
import dev.minn.jda.ktx.interactions.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.mastriel.hostiletakeover.ConsoleLine
import xyz.mastriel.hostiletakeover.UserSettings
import xyz.mastriel.hostiletakeover.discord.HTBot
import xyz.mastriel.hostiletakeover.discord.stringToKey
import xyz.mastriel.hostiletakeover.serializers.MutableBoolean
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

object PressKeyCommand : HTCommand() {
    override val command: SubcommandData get() =
        Subcommand("presskey", "Press a key on ${HTBot.username}'s machine.") {
            option<String>("key", "The key to press.", false)
            option<Long>("duration", "The duration of the keypress in milliseconds, up to 2 seconds. Default of 250ms.", false)
        }
    override val requiredConsent: MutableBoolean
        get() = UserSettings().allowScreenshots

    override val cooldown: Long
        get() = UserSettings().screenshotCooldown.value

    val keys = ("a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,1,2,3,4,5,6,7,8,9,ctrl," +
            "win,esc,shift,tab,enter,space,backspace,alt,caps,-,=,f1,f2,f3,f4,f5," +
            "f6,f7,f8,f9,f10,f11,f12,up,down,left,right").split(",")

    override suspend fun execute(e: SlashCommandInteractionEvent) {
        val key = e.getOption("key")?.asString
        val duration = e.getOption("duration")?.asLong ?: 250

        if (duration > 2000 || duration < 10) {
            e.replyEmbeds(Embed {
                title = "Invalid Keypress Duration"
                description = "The duration of a keypress must be between 10ms and 2000ms."
                color = errorColor
            }).queue()
            resetCooldown(e.user)
            return
        }

        if (key == null || !keys.contains(key.lowercase())) {
            e.replyEmbeds(Embed {
                title = "Available keys"
                description = keys.joinToString(", ") { "`$it`" }
                color = errorColor
            }).queue()
            resetCooldown(e.user)
            return
        }
        val keycode = try {
            stringToKey(key)
        } catch (ex: IllegalArgumentException) {
            e.replyEmbeds(Embed {
                title = "Invalid key"
                description = "This key is not available to press."
                color = errorColor
            }).queue()
            resetCooldown(e.user)
            return
        }

        val robot = Robot()

        robot.keyPress(keycode)
        robot.delay(duration.toInt())
        robot.keyRelease(keycode)

        e.replyEmbeds(Embed {
            title = "Key pressed!"
            description = "`$key` was pressed on ${HTBot.username}'s machine!"
            color = successColor
        }).queue()
    }
}