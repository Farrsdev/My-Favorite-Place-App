package com.example.todobase.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.ui.theme.TodoBaseTheme

@Composable
fun LoginScreen(modifier: Modifier = Modifier, onGoToRegister: () -> Unit, onLoginSuccess: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { 
        val authRepo = AuthRepo()
        val context = LocalContext.current
        
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Text("Login", fontSize = MaterialTheme.typography.headlineLarge.fontSize)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { 
                authRepo.login(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        ){
            Text("Login")
        }
        Row (
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text("Belum punya akun?")
            Text("Register", color = Color.Blue, modifier = modifier.clickable{onGoToRegister()})
        }
    
    }
}