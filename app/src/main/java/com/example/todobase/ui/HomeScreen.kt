package com.example.todobase.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.todobase.data.models.FavoritePlace
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.data.repo.FavoriteRepo

@Composable
fun HomeScreen(modif: Modifier = Modifier, onPlaceClick: (place: FavoritePlace) -> Unit) {
    val favoriteRepo = remember { FavoriteRepo() }
    val authRepo = remember { AuthRepo() }
    
    var favPlaces by remember { mutableStateOf<List<FavoritePlace>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var greetingName by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var uidMaps by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun loadFavorites() {
        favoriteRepo.getFavoritePlaces { places, _ ->
            if (places != null) {
                favPlaces = places
            }
            isLoading = false
        }
    }
    
    LaunchedEffect(Unit) {
        authRepo.getUserProfile { user, _ ->
            if (user != null) {
                greetingName = if (user.nama.isNotBlank()) user.nama else user.username
            }
        }
        loadFavorites()
    }

    Column(
        modifier = modif
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (greetingName.isNotBlank()) "Selamat Datang, $greetingName! 👋" else "Selamat Datang! 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jelajahi dan kelola tempat-tempat favorit pilihanmu di peta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Daftar Tempat Favorit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            favPlaces.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada tempat favorit yang disimpan.",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } 
            else -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(favPlaces.size) { i ->
                        val place = favPlaces[i]

                        CardFavorite(
                            place = place,
                            onClick = { onPlaceClick(place) },
                            onDeleteClick = {
                                confirmDelete = true
                                uidMaps = place.uid
                            }
                        )
                    }
                }
            }
        }
    }

    if(confirmDelete){
        AlertDialog(
            onDismissRequest = {
                confirmDelete = false
            },
            title = {
                Text("Yakin ingin menghapus tempat ini?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        favoriteRepo.deleteFavoritePlace(uidMaps) { success, msg ->
                            if (success) {
                                favPlaces = favPlaces.filter { it.uid != uidMaps }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                            }
                        }
                        confirmDelete = false
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun CardFavorite(
    place: FavoritePlace,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Favorit",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (place.description.isNotEmpty()) {
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${place.latitude}, ${place.longitude})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
