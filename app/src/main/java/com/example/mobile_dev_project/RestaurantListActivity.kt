package com.example.mobile_dev_project

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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

    // Load the data once when screen opens
    LaunchedEffect(Unit) {
        val data = withContext(Dispatchers.IO) {
            restaurantDao.getAll()
        }
        restaurants = data
    }

    val filtered = remember(restaurants, searchText) {
        val q = searchText.trim().lowercase()
        if (q.isEmpty()) restaurants
        else restaurants.filter {
            it.name.lowercase().contains(q) ||
                    it.tags.lowercase().contains(q)
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered) { restaurant ->
                RestaurantItemRow(
                    restaurant = restaurant,
                    onEmail = { shareByEmail(context, it) },
                    onFacebook = { shareOnFacebook(context, it) },
                    onTwitter = { shareOnTwitter(context, it) },
                    onDetails = { openDetails(context, it) },
                    onMap = { openMap(context, it) }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
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
    onDetails: (Restaurant) -> Unit,
    onMap: (Restaurant) -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Text(restaurant.name, style = MaterialTheme.typography.titleMedium)

        if (restaurant.tags.isNotBlank()) {
            Text("Tags: ${restaurant.tags}")
        }

        if (restaurant.description.isNotBlank()) {
            Text(restaurant.description)
        }

        if (restaurant.address.isNotBlank()) {
            Text("Address: ${restaurant.address}")
        }

        Spacer(Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onDetails(restaurant) }) {
                    Text("Details")
                }

                Button(onClick = { onMap(restaurant) }) {
                    Text("Map")
                }

                Button(onClick = { onEmail(restaurant) }) {
                    Text("Email")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { onFacebook(restaurant) }) {
                    Text("Facebook")
                }
                TextButton(onClick = { onTwitter(restaurant) }) {
                    Text("Twitter")
                }
            }
        }
    }
}

// ===== Helpers =====

fun openMap(context: Context, restaurant: Restaurant) {
    val intent = Intent(context, MapsActivity::class.java).apply {
        putExtra("name", restaurant.name)
        putExtra("address", restaurant.address)
    }
    context.startActivity(intent)
}

fun openDetails(context: Context, restaurant: Restaurant) {
    val intent = Intent(context, DetailsActivity::class.java).apply {
        // 👇 pass id so DetailsActivity can delete this exact row
        putExtra("id", restaurant.id)
        putExtra("name", restaurant.name)
        putExtra("address", restaurant.address)
        putExtra("phone", restaurant.phone)
        putExtra("description", restaurant.description)
        putExtra("tags", restaurant.tags)
        putExtra("rating", restaurant.rating)
    }
    context.startActivity(intent)
}

fun shareByEmail(context: Context, restaurant: Restaurant) {
    val subject = "Check out: ${restaurant.name}"

    val body = buildString {
        appendLine("Restaurant: ${restaurant.name}")
        if (restaurant.address.isNotBlank()) appendLine("Address: ${restaurant.address}")
        if (restaurant.phone.isNotBlank()) appendLine("Phone: ${restaurant.phone}")
        if (restaurant.tags.isNotBlank()) appendLine("Tags: ${restaurant.tags}")
        appendLine()
        if (restaurant.description.isNotBlank()) appendLine(restaurant.description)
    }

    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    if (emailIntent.resolveActivity(context.packageManager) != null) {
        val chooser = Intent.createChooser(emailIntent, "Send email with:")
        context.startActivity(chooser)
    } else {
        Toast.makeText(context, "No email app found on this device", Toast.LENGTH_SHORT).show()
    }
}

fun shareOnFacebook(context: Context, restaurant: Restaurant) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.facebook.com/sharer/sharer.php?u=${Uri.encode(restaurant.name)}")
    )
    context.startActivity(intent)
}

fun shareOnTwitter(context: Context, restaurant: Restaurant) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://twitter.com/intent/tweet?text=${Uri.encode(restaurant.name)}")
    )
    context.startActivity(intent)
}