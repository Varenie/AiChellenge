package ru.varenie.aichellenge

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.varenie.aichellenge.data.dto.GeminiContent
import ru.varenie.aichellenge.data.dto.GeminiPart
import ru.varenie.aichellenge.data.dto.GeminiRequest
import ru.varenie.aichellenge.data.remote.NetworkModule
import ru.varenie.aichellenge.presentation.chat_screen.ChatScreen
import ru.varenie.aichellenge.presentation.chat_screen.ChatViewModel
import ru.varenie.aichellenge.ui.theme.AiChellengeTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiChellengeTheme {
                val viewModel: ChatViewModel = hiltViewModel()
                ChatScreen(viewModel)
            }
        }

    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AiChellengeTheme {
        Greeting("Android")
    }
}