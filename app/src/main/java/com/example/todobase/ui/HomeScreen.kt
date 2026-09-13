package com.example.todobase.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todobase.data.models.FavoritePlace
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.data.repo.FavoriteRepo
import com.example.todobase.ui.theme.TodoBaseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modif: Modifier = Modifier, onPlaceClick: (place: FavoritePlace) -> Unit) {
    val authRepo = AuthRepo()
    val favoriteRepo = remember { FavoriteRepo() }
    
    var favPlaces by remember { mutableStateOf<List<FavoritePlace>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var username by remember { mutableStateOf("Memuat...")  }

    fun loadFavorites(){
        favoriteRepo.getFavoritePlaces { places, _ ->
            if(places != null){
                favPlaces = places
            }
            isLoading = false
        }
    }
    
    LaunchedEffect(Unit) {
        authRepo.getUserProfile { user, _ ->
            username = user?.username ?: "Gagal memuat profil"
        }
        loadFavorites()
    }

    Column(
        modifier = modif
            .fillMaxSize()
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Welcome, $username", fontSize = 24.sp, modifier = modif.padding(bottom = 16.dp))

        Spacer(modifier = modif.height(20.dp))

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
                LazyColumn(modifier = modif.fillMaxWidth()) {
                    items(favPlaces.size){ i ->
                        val place = favPlaces[i]

                        CardFavorite(
                            place = place,
                            onClick = { onPlaceClick(place) },
                            onDeleteClick = {
                                favoriteRepo.deleteFavoritePlace(place.uid) { success, _ ->
                                    if (success) {
                                        favPlaces = favPlaces.filter { it.uid != place.uid }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
       
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
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