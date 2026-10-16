package com.example.gamecollection

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamecollection.ui.theme.GameCollectionTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameCollectionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameCollectionScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GameCollectionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var games by remember { mutableStateOf(listOf<Game>()) }
    var nextId by remember { mutableIntStateOf(1) }

    var title by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var platformError by remember { mutableStateOf<String?>(null) }
    var ratingError by remember { mutableStateOf<String?>(null) }

    var search by remember { mutableStateOf("") }
    var gameToEdit by remember { mutableStateOf<Game?>(null) }
    var gameToDelete by remember { mutableStateOf<Game?>(null) }

    val filteredGames = games.filter {
        it.title.contains(search, ignoreCase = true) || it.platform.contains(search, ignoreCase = true)
    }

    fun addGame() {
        titleError = validateTitle(title)
        platformError = validatePlatform(platform)
        ratingError = validateRating(rating)
        if (titleError != null || platformError != null || ratingError != null) {
            toast(context, "Minden adatot helyesen adj meg!")
        } else {
            games = games + Game(nextId, title.trim(), platform.trim(), rating.trim().toInt())
            nextId++
            title = ""
            platform = ""
            rating = ""
            toast(context, "Játék sikeresen hozzáadva!")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Játékgyűjtemény",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Kedvenc játékaid egy helyen",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text("Játék címe") },
            isError = titleError != null,
            supportingText = titleError?.let { message -> { Text(message) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = platform,
            onValueChange = {
                platform = it
                platformError = null
            },
            label = { Text("Platform") },
            isError = platformError != null,
            supportingText = platformError?.let { message -> { Text(message) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = rating,
            onValueChange = {
                rating = it
                ratingError = null
            },
            label = { Text("Értékelés") },
            isError = ratingError != null,
            supportingText = ratingError?.let { message -> { Text(message) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { addGame() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Játék hozzáadása")
        }

        Text(
            text = "Játékok száma: ${games.size}",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Keresés") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (search.isNotBlank()) {
            Text(
                text = "Találatok: ${filteredGames.size}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        when {
            games.isEmpty() -> Text("Még nincs játék a gyűjteményben.")
            filteredGames.isEmpty() -> Text("Nincs a keresésnek megfelelő játék.")
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGames) { game ->
                    GameCard(
                        game = game,
                        onEdit = { gameToEdit = game },
                        onDelete = { gameToDelete = game }
                    )
                }
            }
        }
    }

    gameToEdit?.let { game ->
        EditGameDialog(
            game = game,
            onDismiss = { gameToEdit = null },
            onSave = { newTitle, newPlatform, newRating ->
                games = games.map {
                    if (it.id == game.id) it.copy(title = newTitle, platform = newPlatform, rating = newRating) else it
                }
                gameToEdit = null
            }
        )
    }

    gameToDelete?.let { game ->
        DeleteGameDialog(
            game = game,
            onDismiss = { gameToDelete = null },
            onConfirm = {
                games = games.filterNot { it.id == game.id }
                gameToDelete = null
            }
        )
    }
}

@Composable
fun GameCard(
    game: Game,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameAvatar(title = game.title)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = game.platform, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Értékelés: ${game.rating} / 10",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "Szerkesztés"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Törlés"
                )
            }
        }
    }
}

@Composable
fun GameAvatar(title: String, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFFEF5350),
        Color(0xFF7E57C2),
        Color(0xFF42A5F5),
        Color(0xFF26A69A),
        Color(0xFFFFA726),
        Color(0xFF66BB6A)
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colors[abs(title.hashCode() % colors.size)]),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.trim().take(2).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun EditGameDialog(
    game: Game,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    val context = LocalContext.current

    var title by remember(game.id) { mutableStateOf(game.title) }
    var platform by remember(game.id) { mutableStateOf(game.platform) }
    var rating by remember(game.id) { mutableStateOf(game.rating.toString()) }
    var titleError by remember(game.id) { mutableStateOf<String?>(null) }
    var platformError by remember(game.id) { mutableStateOf<String?>(null) }
    var ratingError by remember(game.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Játék szerkesztése") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text("Játék címe") },
                    isError = titleError != null,
                    supportingText = titleError?.let { message -> { Text(message) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = platform,
                    onValueChange = {
                        platform = it
                        platformError = null
                    },
                    label = { Text("Platform") },
                    isError = platformError != null,
                    supportingText = platformError?.let { message -> { Text(message) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rating,
                    onValueChange = {
                        rating = it
                        ratingError = null
                    },
                    label = { Text("Értékelés") },
                    isError = ratingError != null,
                    supportingText = ratingError?.let { message -> { Text(message) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    titleError = validateTitle(title)
                    platformError = validatePlatform(platform)
                    ratingError = validateRating(rating)
                    if (titleError != null || platformError != null || ratingError != null) {
                        toast(context, "Minden adatot helyesen adj meg!")
                    } else {
                        onSave(title.trim(), platform.trim(), rating.trim().toInt())
                    }
                }
            ) {
                Text("Mentés")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse")
            }
        }
    )
}

@Composable
fun DeleteGameDialog(
    game: Game,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Játék törlése") },
        text = { Text("Biztosan törölni szeretnéd a ${game.title} játékot?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Törlés")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse")
            }
        }
    )
}

private fun validateTitle(title: String): String? =
    if (title.isBlank()) "A cím megadása kötelező!" else null

private fun validatePlatform(platform: String): String? =
    if (platform.isBlank()) "A platform megadása kötelező!" else null

private fun validateRating(rating: String): String? =
    if (rating.trim().toIntOrNull() !in 1..10) "Az értékelés 1 és 10 között lehet!" else null

private fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
fun GameCollectionPreview() {
    GameCollectionTheme {
        GameCollectionScreen()
    }
}