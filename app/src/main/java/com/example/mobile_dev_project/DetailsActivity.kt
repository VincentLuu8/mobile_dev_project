package com.example.mobile_dev_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
class DetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read data passed from list screen
        val name = intent.getStringExtra("name") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        val phone = intent.getStringExtra("phone") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val tags = intent.getStringExtra("tags") ?: ""
        val rating = intent.getFloatExtra("rating", 0f)

        setContent {
            MaterialTheme {
                DetailsScreen(
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
