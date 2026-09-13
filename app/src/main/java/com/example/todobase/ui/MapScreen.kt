package com.example.todobase.ui

import android.Manifest
import com.example.todobase.BuildConfig
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.todobase.data.models.FavoritePlace
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.data.repo.FavoriteRepo
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.PropertyFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(modif: Modifier = Modifier,  targetLat: Double? = null, targetLng: Double? = null) {
    val favoriteRepo = FavoriteRepo()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    
    var markers by remember { mutableStateOf<Map<String, Marker>>(emptyMap()) }
    var favPlaces by remember { mutableStateOf<List<FavoritePlace>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<FavoritePlace?>(null) }
    
    var selectedPoint by remember { mutableStateOf<LatLng?>(null) }
    var placeName by remember { mutableStateOf("") }
    var placeDescription by remember { mutableStateOf("") }
    
    var mapLibreMap by remember { 
        mutableStateOf<MapLibreMap?>(null) 
    }
    var hasLocationPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { p->
        hasLocationPermission = p[Manifest.permission.ACCESS_FINE_LOCATION]  == true || p[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if(hasLocationPermission){
            mapLibreMap?.style?.let { s->
                mapLibreMap?.let { m->
                    enableLocationComponent(context, m, s)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission){
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    
    LaunchedEffect(mapLibreMap, targetLat, targetLng){
        if(mapLibreMap != null && targetLat != null && targetLng != null){
            mapLibreMap?.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(targetLat, targetLng))
                        .zoom(17.0)
                        .tilt(0.0)
                        .build()
                ),
                1000
            )
        }
    }
    LaunchedEffect(mapLibreMap){
        if(mapLibreMap != null){
            favoriteRepo.getFavoritePlaces { places, msg ->
                if(places != null){
                    favPlaces = places
                    places.forEach { place ->
                        val marker = mapLibreMap?.addMarker(
                            MarkerOptions()
                                .position(LatLng(place.latitude, place.longitude))
                                .title(place.name)
                                .snippet(place.description)
                        )
                        if(marker != null){
                             markers = markers + (place.uid to marker)
                        }
                    }
                    
               } else{
                    Toast.makeText(
                        context,
                        msg ?: "Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } 
    }
    
    val mapView = remember { 
        val options = MapLibreMapOptions
            .createFromAttributes(context)
            .textureMode(true)

        MapView(context, options)
    }
    
    val apiKey = BuildConfig.API_KEY
    val styleUrl = "https://api.maptiler.com/maps/streets-v4/style.json?key=$apiKey"

    DisposableEffect(Unit) {
        mapView.onStart()
        mapView.onResume()
        
        onDispose { 
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    var is3D by remember { mutableStateOf(false) }

    Box(modifier = modif.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        mapLibreMap = map

                        map.uiSettings.isTiltGesturesEnabled = true
                        map.uiSettings.isRotateGesturesEnabled = true
                        map.uiSettings.isZoomGesturesEnabled = true
                        map.uiSettings.isScrollGesturesEnabled = true

                        if (targetLat != null && targetLng != null) {
                            map.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(targetLat, targetLng))
                                .zoom(17.0)
                                .tilt(if (is3D) 60.0 else 0.0)
                                .build()
                        } else {
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                            val initialLocation = try {
                                if (hasLocationPermission && locationManager != null) {
                                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                } else null
                            } catch (e: SecurityException) {
                                null
                            }

                            if (initialLocation != null) {
                                map.cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(initialLocation.latitude, initialLocation.longitude))
                                    .zoom(17.0)
                                    .tilt(if (is3D) 60.0 else 0.0)
                                    .bearing(0.0)
                                    .build()
                            }
                        }

                        val buildingLayer = FillExtrusionLayer("3d-buildings", "openmaptiles").apply {
                            sourceLayer = "building"
                            setProperties(
                                PropertyFactory.fillExtrusionHeight(Expression.get("render_height")),
                                PropertyFactory.fillExtrusionBase(Expression.get("render_min_height")),
                                PropertyFactory.fillExtrusionColor("#d1d5db"),
                                PropertyFactory.fillExtrusionOpacity(0.85f)
                            )
                        }

                        map.setStyle(styleUrl) { style ->
                            if (hasLocationPermission) {
                                enableLocationComponent(context, map, style)
                            }
                            try {
                                style.addLayer(buildingLayer)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        map.addOnMapClickListener { point ->
                            selectedPoint = point
                            selectedPlace = null
                            placeName = ""
                            placeDescription = ""
                            showDialog = true
                            true
                        }

                        map.setOnMarkerClickListener { marker ->
                            val place = favPlaces.find {
                                markers[it.uid] == marker
                            }

                            if (place != null) {
                                selectedPlace = place
                                placeName = place.name
                                placeDescription = place.description
                                selectedPoint = LatLng(place.latitude, place.longitude)

                                showDialog = true
                            }
                            true
                        }
                    }
                }
            }
        )

        FloatingActionButton(
            onClick = {
                val next3D = !is3D
                is3D = next3D
                val targetTilt = if (next3D) 70.0 else 0.0
                mapLibreMap?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .tilt(targetTilt)
                            .build()
                    ),
                    800
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(if (is3D) "2D" else "3D", fontWeight = FontWeight.Bold)
        }
    }
    
    if(showDialog){
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(if(selectedPlace != null) "Edit Place" else "Add Place")
            },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = placeName,
                        onValueChange = {
                            placeName = it
                        },
                        label = {
                            Text("Place Name")
                        }
                    )

                    OutlinedTextField(
                        value = placeDescription,
                        onValueChange = {
                            placeDescription = it
                        },
                        label = {
                            Text("Description")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if(selectedPlace == null){
                            val point = selectedPoint ?: return@TextButton

                            favoriteRepo.addFavoritePlace(
                                FavoritePlace(
                                    name = placeName,
                                    description = placeDescription,
                                    latitude = point.latitude,
                                    longitude = point.longitude
                                ),
                                onComplete = { success, message, savedPlace ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success && savedPlace != null) {
                                        val marker = mapLibreMap?.addMarker(
                                            MarkerOptions()
                                                .position(
                                                    LatLng(
                                                        savedPlace.latitude,
                                                        savedPlace.longitude
                                                    )
                                                )
                                                .title(savedPlace.name)
                                                .snippet(savedPlace.description)
                                        )
                                        if (marker != null) {
                                            markers = markers + (savedPlace.uid to marker)
                                        }
                                    }

                                    showDialog = false
                                }
                            )   
                        } else{
                            favoriteRepo.updateFavoritePlace(
                                selectedPlace!!.copy(
                                    name = placeName,
                                    description = placeDescription
                                ),
                                onComplete = { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                
                                    showDialog = false
                                }
                            )
                        }
                    }
                ) { 
                    Text("Save")
                }
                
                if (selectedPlace != null) {
                    TextButton(
                        onClick = {
                            favoriteRepo.deleteFavoritePlace(
                                selectedPlace!!.uid,
                                onComplete = { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        markers[selectedPlace!!.uid]?.remove()
                                        markers = markers - selectedPlace!!.uid
                                        favPlaces = favPlaces.filter {
                                            it.uid != selectedPlace!!.uid
                                        }

                                        selectedPlace = null
                                        placeName = ""
                                        placeDescription = ""
                                    }
                                    showDialog = false
                                }
                            )
                        }
                    ) {
                        Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@SuppressWarnings("Missing Permission")
private fun enableLocationComponent(
    context: Context,
    map: MapLibreMap,
    style: Style
) {
    val locationComponent = map.locationComponent
    
    locationComponent.activateLocationComponent(
        LocationComponentActivationOptions.builder(
            context,
            style
        ).useDefaultLocationEngine(true).build()
    )
    
    locationComponent.isLocationComponentEnabled = true
    locationComponent.cameraMode = CameraMode.TRACKING
    locationComponent.renderMode = RenderMode.COMPASS
    
    locationComponent.lastKnownLocation?.let { loc ->
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(loc.latitude, loc.longitude))
                    .zoom(17.0)
                    .tilt(0.0)
                    .build()
            ),
            1000
        )
    }
}