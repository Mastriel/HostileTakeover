package xyz.mastriel.hostiletakeover

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.mouse.mouseScrollFilter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.mastriel.hostiletakeover.discord.HTBot
import xyz.mastriel.hostiletakeover.discord.getTimeRepresentation
import xyz.mastriel.hostiletakeover.discord.status
import xyz.mastriel.hostiletakeover.serializers.MutableLong
import java.awt.Dimension
import kotlin.system.exitProcess

const val allowedNameCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_=+-[]';/.,,`~}{:?><\""

fun main() = application {
    Window(
        onCloseRequest = ::onExit,
        title = "Hostile Takeover",
        state = rememberWindowState(width = 650.dp, height = 900.dp),
        resizable = true,
    ) {
        this.window.minimumSize = Dimension(550, 630)
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
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        )
                        val settings = UserSettings()
                        ToggleButton(
                            label = "Allow Screenshots",
                            mutableState = settings.allowScreenshots,
                            cooldownState = settings.screenshotCooldown,
                            enabled = true
                        )
                        ToggleButton(
                            label = "Allow Terminal",
                            mutableState = settings.allowTerminal,
                            cooldownState = settings.terminalCooldown,
                            enabled = true
                        )
                        ToggleButton(
                            label = "Allow Camera",
                            mutableState = settings.allowCamera,
                            cooldownState = null,
                            enabled = false
                        )
                        ToggleButton(
                            label = "Allow Scripting",
                            mutableState = settings.allowScripting,
                            cooldownState = null,
                            enabled = false
                        )
                        ToggleButton(
                            label = "Allow Typing",
                            mutableState = settings.allowTyping,
                            cooldownState = null,
                            enabled = false
                        )
                        ToggleButton(
                            label = "Allow Inputs",
                            mutableState = settings.allowInputs,
                            cooldownState = null,
                            enabled = false
                        )
                        UserInput("Username", settings.username)
                        Box(modifier = Modifier.padding(top = 15.dp))
                        UserInput("Bot Token", settings.token, maxChars = 300, hidden = true)
                        StartBotButton()
                        Console()
                    }
                    val discStatus by status
                    Row(
                        modifier = Modifier.background(Color.Black)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                    ) {
                        OptionText(
                            label = "Status: ${discStatus.name}",
                            font = LightFont
                        )
                    }
                }
            }
        }
    }
}
@Preview
@Composable
fun ToggleButton(label: String, mutableState: MutableState<Boolean>, cooldownState: MutableState<Long>? =null, enabled: Boolean=true) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OptionText(
            label,
            color = if (enabled) Color.White else Color.Red,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(200.dp)
        )
        Row(
            Modifier.width(600.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (cooldownState != null) {
                CooldownSlider(cooldownState)
            }
            Switch(
                checked = mutableState.value,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = Color.LightGray,
                    disabledUncheckedTrackColor = Color.DarkGray,
                ),
                onCheckedChange = { if (enabled) mutableState.value = it }
            )
        }
    }
}
@Composable
fun CooldownSlider(cooldownState: MutableLong) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 10.dp)
    ) {
        val cooldownTime = getTimeRepresentation(cooldownState.value)
        OptionText(
            label = "Cooldown: $cooldownTime",
            size = 12.sp,
            modifier = Modifier.absoluteOffset(y = (-3).dp)
        )
        Slider(
            value = cooldownState.value.toFloat(),
            valueRange = 0.0F..600_000.0F,
            modifier = Modifier.fillMaxWidth(0.6F).height(7.dp).absoluteOffset(y = 3.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.LightGray,
                inactiveTrackColor = Color.Gray
            ),
            onValueChange = {
                cooldownState.value = it.toLong()
            }
        )
    }
}

@Composable
fun OptionText(
    label: String,
    size: TextUnit = 16.sp,
    modifier: Modifier = Modifier,
    font: FontFamily = BoldFont,
    color: Color = Color.White,
    textAlign: TextAlign = TextAlign.Center,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    Text(
        text = label,
        fontSize = size,
        color = color,
        textAlign = textAlign,
        fontFamily = font,
        modifier = modifier,
        onTextLayout = onTextLayout
    )
}


@Composable
fun UserInput(
    label: String,
    mutableState: MutableState<String>,
    maxChars: Int=20,
    hidden: Boolean=false,
    size: TextUnit = 12.sp,
    modifier: Modifier = Modifier,
    font: FontFamily = BoldFont
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.fillMaxWidth().align(Alignment.CenterVertically)
        ) {
            MiniText(label, size, font, modifier)
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
fun MiniText(label: String, size: TextUnit=12.sp, font: FontFamily=BoldFont, modifier: Modifier=Modifier) {
    Text(
        text = label,
        fontSize = size,
        color = Color.White,
        textAlign = TextAlign.Center,
        fontFamily = font,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun StartBotButton() = Column(modifier = Modifier.fillMaxWidth()) {
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

val consoleLines = mutableStateListOf<ConsoleLine>()



@Composable
fun Console() {
    MiniText("Logger", modifier = Modifier.offset(y = 6.dp))
    val scroll = rememberScrollState(Int.MAX_VALUE)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 10.dp,
                bottom = 25.dp,
                start = 10.dp,
                end = 10.dp
            )
            .border(BorderStroke(2.dp, Color.White))
            .verticalScroll(scroll, false),
        contentAlignment = Alignment.TopStart
    ) {
        rememberCoroutineScope().launch {
            scroll.scrollTo(Int.MAX_VALUE)
        }
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                for (line in consoleLines) {
                    ConsoleText(line.text, line.color)
                }
            }
        }
    }
}

@Composable
fun ConsoleText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = color,
        textAlign = TextAlign.Left,
        fontFamily = BoldFont,
        modifier = Modifier.padding(start = 6.dp, top = 3.dp, bottom = 1.dp)
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