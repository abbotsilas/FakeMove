import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TabExample() {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabTitles = listOf("Tab 1", "Tab 2", "Tab 3")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(8.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> TabContent("Content for Tab 1")
            1 -> TabContent("Content for Tab 2")
            2 -> TabContent("Content for Tab 3")
        }
    }
}

@Composable
fun TabContent(content: String) {
    Text(
        text = content,
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun PreviewTabExample() {
    TabExample()
}
