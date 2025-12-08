package com.example.mobile_dev_project

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobile_dev_project.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read the data passed from list screen
        val id = intent.getLongExtra("id", -1L)
        val name = intent.getStringExtra("name") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        val phone = intent.getStringExtra("phone") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val tags = intent.getStringExtra("tags") ?: ""
        val rating = intent.getFloatExtra("rating", 0f)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DetailsScreen(
                        restaurantId = id,
                        name = name,
                        address = address,
                        phone = phone,
                        description = description,
                        tags = tags,
                        rating = rating
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsScreen(
    restaurantId: Long,
    name: String,
    address: String,
    phone: String,
    description: String,
    tags: String,
    rating: Float
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance(context)
    val restaurantDao = db.restaurantDao()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        if (address.isNotBlank()) {
            Text("Address: $address")
            Spacer(Modifier.height(8.dp))
        }

        if (phone.isNotBlank()) {
            Text("Phone: $phone")
            Spacer(Modifier.height(8.dp))
        }

        if (tags.isNotBlank()) {
            Text("Tags: $tags")
            Spacer(Modifier.height(8.dp))
        }

        Text("Rating: ${rating.toInt()} stars")
        Spacer(Modifier.height(16.dp))

        if (description.isNotBlank()) {
            Text("Description:")
            Text(description)
        }

        Spacer(Modifier.height(30.dp))

        // Get Directions button
        Button(
            onClick = {
                if (address.isBlank()) {
                    Toast.makeText(context, "No address available", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val uri = "google.navigation:q=${Uri.encode(address)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                    setPackage("com.google.android.apps.maps")
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Fallback to browser-based directions if Maps app isn't installed
                    val browser = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://www.google.com/maps/dir/?api=1&destination=${
                                Uri.encode(address)
                            }"
                        )
                    )
                    context.startActivity(browser)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Directions")
        }

        Spacer(Modifier.height(12.dp))

        // Share button
        Button(
            onClick = {
                val shareText = """
                    Restaurant: $name
                    Address: $address
                    Phone: $phone
                    Tags: $tags
                    Rating: ${rating.toInt()} / 5
                    
                    $description
                """.trimIndent()

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Restaurant: $name")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }

                val chooser = Intent.createChooser(intent, "Share restaurant info")
                try {
                    context.startActivity(chooser)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "No app available to share this information",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share")
        }

        Spacer(Modifier.height(12.dp))

        // Edit button (NEW ADDED)
        Button(
            onClick = {
                if (restaurantId <= 0L) {
                    Toast.makeText(
                        context,
                        "Unable to edit this restaurant",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val intent = Intent(context, EditRestaurantActivity::class.java).apply {
                    putExtra("id", restaurantId)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit")
        }

        Spacer(Modifier.height(12.dp))

        // Delete button (NEW ADDED)
        Button(
            onClick = {
                if (restaurantId <= 0L) {
                    Toast.makeText(
                        context,
                        "Unable to delete this restaurant",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                scope.launch {
                    withContext(Dispatchers.IO) {
                        val restaurant = restaurantDao.getById(restaurantId)
                        if (restaurant != null) {
                            restaurantDao.delete(restaurant)
                        }
                    }
                    Toast.makeText(context, "Restaurant removed", Toast.LENGTH_SHORT).show()
                    (context as? Activity)?.finish()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete")
        }
    }
}