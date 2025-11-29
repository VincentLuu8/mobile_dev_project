package com.example.mobile_dev_project

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobile_dev_project.data.AppDatabase
import com.example.mobile_dev_project.data.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RestaurantListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RestaurantListScreen()
                }
            }
        }
    }
}

@Composable
fun RestaurantListScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val restaurantDao = remember { db.restaurantDao() }

    var restaurants by remember { mutableStateOf(emptyList<Restaurant>()) }
    var searchText by remember { mutableStateOf("") }

    // load data once when the screen opens
    LaunchedEffect(Unit) {
        val data = withContext(Dispatchers.IO) {
            restaurantDao.getAll()
        }
        restaurants = data
    }

    // simple search on name + tags
    val filtered = remember(restaurants, searchText) {
        val q = searchText.trim().lowercase()
        if (q.isEmpty()) {
            restaurants
        } else {
            restaurants.filter { r ->
                r.name.lowercase().contains(q) ||
                        r.tags.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Saved Restaurants",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search by name or tag") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text(
                text = "Nothing matches your search.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { restaurant ->
                    RestaurantItemRow(
                        restaurant = restaurant,
                        onEmail = { shareByEmail(context, it) },
                        onFacebook = { shareOnFacebook(context, it) },
                        onTwitter = { shareOnTwitter(context, it) },
                        onDetails = { openDetails(context, it) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun RestaurantItemRow(
    restaurant: Restaurant,
    onEmail: (Restaurant) -> Unit,
    onFacebook: (Restaurant) -> Unit,
    onTwitter: (Restaurant) -> Unit,
    onDetails: (Restaurant) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.titleMedium
        )

        if (restaurant.tags.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tags: ${restaurant.tags}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (restaurant.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = restaurant.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (restaurant.address.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Address: ${restaurant.address}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { onDetails(restaurant) }) {
                Text("Details")
            }

            Button(onClick = { onEmail(restaurant) }) {
                Text("Email")
            }

            TextButton(onClick = { onFacebook(restaurant) }) {
                Text("Facebook")
            }

            TextButton(onClick = { onTwitter(restaurant) }) {
                Text("Twitter")
            }
        }
    }
}

// ===== sharing helpers =====

fun shareByEmail(context: Context, restaurant: Restaurant) {
    val subject = "Check out this restaurant: ${restaurant.name}"

    val body = buildString {
        appendLine("Restaurant: ${restaurant.name}")
        if (restaurant.address.isNotBlank()) appendLine("Address: ${restaurant.address}")
        if (restaurant.phone.isNotBlank()) appendLine("Phone: ${restaurant.phone}")
        if (restaurant.tags.isNotBlank()) appendLine("Tags: ${restaurant.tags}")
        appendLine()
        if (restaurant.description.isNotBlank()) appendLine(restaurant.description)
    }

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

fun shareOnFacebook(context: Context, restaurant: Restaurant) {
    val text = "I found this place: ${restaurant.name}"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        `package` = "com.facebook.katana"
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val url = "https://www.facebook.com/sharer/sharer.php?u=" +
                Uri.encode("https://maps.google.com/?q=${Uri.encode(restaurant.name)}")
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }
}

fun shareOnTwitter(context: Context, restaurant: Restaurant) {
    val text = "I found this restaurant: ${restaurant.name}"
    val url = "https://twitter.com/intent/tweet?text=" + Uri.encode(text)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun openDetails(context: Context, restaurant: Restaurant) {
    val intent = Intent(context, DetailsActivity::class.java).apply {
        putExtra("name", restaurant.name)
        putExtra("address", restaurant.address)
        putExtra("phone", restaurant.phone)
        putExtra("description", restaurant.description)
        putExtra("tags", restaurant.tags)
        putExtra("rating", restaurant.rating)
    }
    context.startActivity(intent)
}

