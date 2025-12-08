package com.example.mobile_dev_project

import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class MapsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Read address passed from MainActivity / Details
        val address = intent.getStringExtra("address") ?: ""

        setContent {
            MapScreen(address = address)
        }
    }
}

@Composable
fun MapScreen(address: String) {

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    var location by remember { mutableStateOf<LatLng?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Normalize & debug the incoming address
    val cleanAddress = remember(address) {
        address.trim().replace("\n", " ").replace(Regex("\\s+"), " ")
    }

    // Convert address → LatLng (with a simple fallback)
    LaunchedEffect(cleanAddress) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                // Try original address
                var results = geocoder.getFromLocationName(cleanAddress, 1)

                // Fallback: append city if nothing found (edit if you prefer a different default)
                if (results.isNullOrEmpty()) {
                    results = geocoder.getFromLocationName("$cleanAddress, Toronto, ON", 1)
                }

                if (!results.isNullOrEmpty()) {
                    location = LatLng(results[0].latitude, results[0].longitude)
                } else {
                    error = "Address not found: \"$cleanAddress\""
                }
            } catch (e: Exception) {
                error = "Geocoding error: ${e.message}"
            }
        }
    }

    // Move camera when location is available
    LaunchedEffect(location) {
        location?.let {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        location?.let {
            Marker(
                state = MarkerState(position = it),
                title = cleanAddress
            )
        }
    }

    // Show errors (if any)
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
}