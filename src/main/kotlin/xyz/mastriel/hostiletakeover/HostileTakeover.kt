package xyz.mastriel.hostiletakeover

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import xyz.mastriel.hostiletakeover.discord.Discord
import xyz.mastriel.hostiletakeover.discord.HTBot
import xyz.mastriel.hostiletakeover.discord.discordInitialized
import xyz.mastriel.hostiletakeover.discord.status
import java.lang.Exception
import kotlin.system.exitProcess

const val allowedNameCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_=+-[]';/.,,`~}{:?><\""

fun main() = application {
    Window(
        onCloseRequest = ::onExit,
        title = "Hostile Takeover",
        state = rememberWindowState(width = 350.dp, height = 600.dp),
        resizable = true,

    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black)
                    .border(BorderStroke(4.dp, Color.White))
                    .padding(
                        start = 12.dp,
                        top = 8.dp,
                        end = 12.dp,
                        bottom = 6.dp
                    )
            ) {
                MaterialTheme {
                    Column(
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = "Hostile Takeover",
                            color = Color.White,
                            fontFamily = ExtraBoldFont,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val settings = UserSettings()
                        ToggleButton("Allow Screenshots", settings.allowScreenshots)
                        ToggleButton("Allow Camera", settings.allowCamera)
                        ToggleButton("Allow Scripting", settings.allowScripting)
                        ToggleButton("Allow Typing", settings.allowTyping)
                        ToggleButton("Allow Inputs", settings.allowInputs)
                        UserInput("Username", settings.username)
                        Box(modifier = Modifier.padding(top = 15.dp))
                        UserInput("Bot Token", settings.token, maxChars = 300, hidden = true)
                        SaveChanges()
                    }
                    val discStatus by status
                    OptionText(
                        label = "Status: ${discStatus.name}",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        font = LightFont
                    )

                }
            }
        }
    }
}
@Preview
@Composable
fun ToggleButton(label: String, mutableState: MutableState<Boolean>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OptionText(label)
        Switch(
            checked = mutableState.value,
            onCheckedChange = { mutableState.value = it }
        )
    }
}

@Composable
fun OptionText(label: String,
               size: TextUnit = 16.sp,
               modifier: Modifier = Modifier,
               font: FontFamily = BoldFont,
               color: Color = Color.White,
               onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    Text(
        text = label,
        fontSize = size,
        color = color,
        textAlign = TextAlign.Center,
        fontFamily = font,
        modifier = modifier,
        onTextLayout = onTextLayout
    )
}


@Composable
fun UserInput(label: String,
              mutableState: MutableState<String>,
              maxChars: Int=20,
              hidden: Boolean=false,
              size: TextUnit = 12.sp,
              modifier: Modifier = Modifier,
              font: FontFamily = BoldFont) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.fillMaxWidth().align(Alignment.CenterVertically)
        ) {
            Text(
                text = label,
                fontSize = size,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontFamily = font,
                modifier = modifier.padding(start = 10.dp, end = 10.dp).fillMaxWidth()
            )
            TextField(
                value = mutableState.value,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.Gray
                ),
                textStyle = TextStyle(
                    fontFamily = RegularFont,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.height(55.dp).fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
                onValueChange = {
                    for (char in it) {
                        if (!allowedNameCharacters.contains(char)) return@TextField
                    }
                    if (it.length > maxChars) return@TextField
                    mutableState.value = it
                }
            )
        }
    }
}
@Composable
fun SaveChanges() = Column(modifier = Modifier.fillMaxWidth()) {
    val color = remember { mutableStateOf(Color.White) }
    val enabled = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    Button(
        colors = ButtonDefaults.buttonColors(
            backgroundColor = color.value
        ),
        onClick = {
            scope.launch {
                if (!enabled.value) return@launch
                enabled.value = false
                val started = HTBot.start()
                delay(250L)
                if (started) {
                    color.value = Color.Green
                    delay(500L)
                    color.value = Color.White
                } else {
                    color.value = Color.Red
                    delay(500L)
                    color.value = Color.White
                }
                enabled.value = true
            }
        },
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp),
        content = {
            OptionText("Start Bot", color = Color.Black)
        }
    )
}

val BoldFont = FontFamily(Font("font/JetBrainsMono-Bold.ttf", FontWeight.Bold))
val RegularFont = FontFamily(Font("font/JetBrainsMono-Regular.ttf", FontWeight.Bold))
val LightFont = FontFamily(Font("font/JetBrainsMono-Light.ttf", FontWeight.Bold))
val ExtraBoldFont = FontFamily(Font("font/JetBrainsMono-ExtraBold.ttf", FontWeight.ExtraBold))

fun onExit() {
    UserSettings().save()
    exitProcess(0)
}