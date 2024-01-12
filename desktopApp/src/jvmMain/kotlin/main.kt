import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.techbeloved.bundledasset.Greeting

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                GreetingView(Greeting().greet())
            }
        }
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}
