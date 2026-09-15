package com.example.todobase.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.example.todobase.R
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.todobase.data.models.User
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.data.services.MapSettingHelper
import com.example.todobase.data.services.MapStyleOptions
import com.example.todobase.ui.theme.TodoBaseTheme

fun base64ToBitmap(base64String: String): ImageBitmap? {
    return try{
        val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bmp.asImageBitmap()
    } catch (e: Exception){
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modif: Modifier = Modifier) {
    val authRepo = AuthRepo()
    var user by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }
    
    var selectedStyle by remember { mutableStateOf(MapSettingHelper.getSelectedOption(context)) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if(uri != null){
            isUploading = true

            val base64String = authRepo.uriToBase64(context, uri)
            if(base64String != null){
                authRepo.updateUserProfile(base64String) { success, msg ->
                    isUploading = false
                    if(success){
                        user = user?.copy(profilePictureUrl = base64String)
                        Toast.makeText(context, "Foto berhasil diubah", Toast.LENGTH_SHORT).show()
                    } else{
                        Toast.makeText(context, msg ?: "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        selectedStyle = MapSettingHelper.getSelectedOption(context)
        authRepo.getUserProfile { usr, _ ->
            user = usr
        }
    }
    
    Column(
        modifier = modif
            .fillMaxSize()
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
           modifier = modif.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){ 
            Box(
                modifier = modif
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ){
                val userBitmap = remember(user?.profilePictureUrl){
                    user?.profilePictureUrl?.let { base64ToBitmap(it) }
                }
                
                if(userBitmap != null){
                    Image(
                        bitmap = userBitmap,
                        contentDescription = "Profile Picture",
                        modifier = modif.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else{
                    Image(
                        painter = painterResource(id = R.drawable.nopp),
                        contentDescription = "Profile Picture",
                        modifier = modif.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                if(isUploading){
                    CircularProgressIndicator()
                }
               
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user?.username ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = user?.email ?: "", color = MaterialTheme.colorScheme.outline)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Pilih Tampilan Peta", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row (
            modifier = modif.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            MapStyleOptions.entries.forEach { opt ->
                val isSelected = selectedStyle == opt

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .clickable {
                            selectedStyle = opt
                            MapSettingHelper.saveMapStyle(context, opt)
                        },
                    shape = MaterialTheme.shapes.medium,
                    border = if(isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(
                        containerColor = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(if(isSelected) 6.dp else 2.dp)
                ) { 
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        AsyncImage(
                            model = opt.previewImageUrl,
                            contentDescription = opt.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.08f)
                                        )
                                    )
                                )
                        )

                        Text(
                            text = opt.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(22.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Terpilih",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                    }
                }                
            }
        }
    }
}
