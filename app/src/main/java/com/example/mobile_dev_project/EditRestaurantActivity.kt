package com.example.mobile_dev_project

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobile_dev_project.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditRestaurantActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val restaurantId = intent.getLongExtra("id", -1L)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditRestaurantScreen(restaurantId = restaurantId)
                }
            }
        }
    }
}

@Composable
fun EditRestaurantScreen(restaurantId: Long) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val restaurantDao = remember { db.restaurantDao() }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) }

    // Load existing restaurant data
    LaunchedEffect(restaurantId) {
        if (restaurantId <= 0L) {
            Toast.makeText(context, "Invalid restaurant", Toast.LENGTH_SHORT).show()
            (context as? Activity)?.finish()
            return@LaunchedEffect
        }

        val restaurant = withContext(Dispatchers.IO) {
            restaurantDao.getById(restaurantId)
        }

        if (restaurant == null) {
            Toast.makeText(context, "Restaurant not found", Toast.LENGTH_SHORT).show()
            (context as? Activity)?.finish()
        } else {
            name = restaurant.name
            address = restaurant.address
            phone = restaurant.phone
            description = restaurant.description
            tags = restaurant.tags
            rating = restaurant.rating
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading restaurant...")
        }
        return
    }

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
            text = "Edit Restaurant",
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
            steps = 4, // 0–5 stars
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    withContext(Dispatchers.IO) {
                        val restaurant = restaurantDao.getById(restaurantId)
                        if (restaurant != null) {
                            restaurantDao.update(
                                restaurant.copy(
                                    name = name.trim(),
                                    address = address.trim(),
                                    phone = phone.trim(),
                                    description = description.trim(),
                                    tags = tags.trim(),
                                    rating = rating
                                )
                            )
                        }
                    }
                    Toast.makeText(context, "Restaurant updated", Toast.LENGTH_SHORT).show()
                    (context as? Activity)?.finish()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}