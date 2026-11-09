package com.example.todobase.ui

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.todobase.R
import com.example.todobase.data.models.User
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.data.services.MapSettingHelper

@Composable
fun CardScreen (modifier:Modifier = Modifier, ){
    val authRepo = remember { AuthRepo() }
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        authRepo.getUserProfile { usr, _ ->
            user = usr
        }
    }

    val color = listOf(
        Color.Red,
        Color.Red
    )
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = modifier.border(
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(
                        2.dp,
                        brush = Brush.horizontalGradient(color)
                    )
                )
            )
        }

        Spacer(modifier = modifier.height(20.dp))
        Card(
            modifier = modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = modifier.fillMaxWidth().padding(16.dp)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp),
                        contentAlignment = Alignment.Center
                    )
                    {
                        val userBitmap = remember(user?.profilePictureUrl) {
                            user?.profilePictureUrl?.let { base64ToBitmap(it) }
                        }

                        if (userBitmap != null) {
                            Image(
                                bitmap = userBitmap,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.nopp),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = modifier.width(12.dp))
                    Column(modifier = modifier.fillMaxWidth()) {
                        user?.let {
                            Text("Nama: ${it.nama}  ")
                            Text("Gmail: ${it.username}")
                            Text("No Telepon: ${it.phoneNumber}")
                        }
                    }
                }
            }
        }
    }
}