package com.example.smartbikeapplication.ui.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@SuppressLint("MissingPermission")
@Composable
fun MapContent(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Configuration.getInstance().userAgentValue = context.packageName

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    controller.setZoom(15.0)

                    val locationClient =
                        LocationServices.getFusedLocationProviderClient(context)

                    locationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val point = GeoPoint(it.latitude, it.longitude)
                            controller.setCenter(point)

                            val marker = Marker(this)
                            marker.position = point
                            marker.title = "You are here"
                            overlays.add(marker)
                        }
                    }
                }
            }
        )

        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("Back to sensors")
        }
    }
}
