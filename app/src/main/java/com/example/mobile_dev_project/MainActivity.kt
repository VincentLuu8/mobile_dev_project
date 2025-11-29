package com.example.mobile_dev_project

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.mobile_dev_project.data.AppDatabase
import com.example.mobile_dev_project.data.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    RestaurantFormScreen()
                }
            }
        }
    }
}

@Composable
fun RestaurantFormScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val restaurantDao = remember { db.restaurantDao() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Add Restaurant",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma separated: vegan, thai)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Rating: ${rating.toInt()} stars")
        Slider(
            value = rating,
            onValueChange = { rating = it },
            valueRange = 0f..5f,
            steps = 4, // 0,1,2,3,4,5
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val restaurant = Restaurant(
                    name = name.trim(),
                    address = address.trim(),
                    phone = phone.trim(),
                    description = description.trim(),
                    tags = tags.trim(),
                    rating = rating
                )

                scope.launch {
                    withContext(Dispatchers.IO) {
                        restaurantDao.insert(restaurant)
                    }
                    Toast.makeText(context, "Restaurant saved", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Restaurant")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                if (address.isBlank()) {
                    Toast.makeText(context, "Enter an address first", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                val uri = "geo:0,0?q=${Uri.encode(address)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                    setPackage("com.google.android.apps.maps")
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("View on Map")
        }

        TextButton(
            onClick = {
                if (address.isBlank()) {
                    Toast.makeText(context, "Enter an address first", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                val uri = "google.navigation:q=${Uri.encode(address)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                    setPackage("com.google.android.apps.maps")
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Get Directions")
        }

        TextButton(
            onClick = {
                val intent = Intent(context, RestaurantListActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("View Saved Restaurants")
        }
    }
}

@Composable
fun DetailsScreen(
    name: String,
    address: String,
    phone: String,
    description: String,
    tags: String,
    rating: Float
) {
    val context = LocalContext.current

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

        Button(
            onClick = {
                val uri = "google.navigation:q=${Uri.encode(address)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                    setPackage("com.google.android.apps.maps")
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val browser = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://www.google.com/maps/dir/?api=1&destination=${
                                Uri.encode(
                                    address
                                )
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

        Button(
            onClick = {
                val shareText = """
                    Restaurant: $name
                    Address: $address
                    Phone: $phone
                    Tags: $tags
                    
                    $description
                """.trimIndent()

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Restaurant: $name")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }

                context.startActivity(Intent.createChooser(intent, "Share via"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share")
        }
    }
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text("About This App", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Text("Team Members:", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Text("• Khaila Franco - Student ID")
        Text("• Member 2 - Student ID")
        Text("• Member 3 - Student ID")

        Spacer(Modifier.height(24.dp))

        Text("Personal Restaurant Guide app for COMP3074.")
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(1500) // 1.5 seconds
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = "App Logo",
            tint = Color.White,
            modifier = Modifier.size(130.dp)
        )
    }
}
