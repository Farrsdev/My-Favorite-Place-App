package com.example.todobase.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.todobase.data.repo.AuthRepo

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, onGoToLogin: () -> Unit, onRegisterSuccess: () -> Unit) {
    val scrollState = rememberScrollState()
    val authRepo = remember { AuthRepo() }
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRetypePasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { 
        Text(
            text = "Daftar Akun",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Silakan lengkapi data untuk membuat akun",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Email") },
            placeholder = { Text("Contoh: budi_santoso") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Lengkap") },
            placeholder = { Text("Contoh: Budi Santoso") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '+' }) {
                    phoneNumber = input
                }
            },
            label = { Text("Nomor Telepon") },
            placeholder = { Text("Contoh: 08123456789 atau +628123456789") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val icon = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = retypePassword,
            onValueChange = { retypePassword = it },
            label = { Text("Retype Password") },
            singleLine = true,
            visualTransformation = if (isRetypePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val icon = if (isRetypePasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (isRetypePasswordVisible) "Sembunyikan password" else "Tampilkan password"
                IconButton(onClick = { isRetypePasswordVisible = !isRetypePasswordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val validationError = validateRegisterInput(username, nama, phoneNumber, password, retypePassword)
                if (validationError != null) {
                    Toast.makeText(context, validationError, Toast.LENGTH_LONG).show()
                    return@Button
                }

                isLoading = true
                authRepo.register(
                    username = username.trim(),
                    nama = nama.trim(),
                    phoneNumber = phoneNumber.trim(),
                    password = password
                ) { success, message ->
                    isLoading = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        onRegisterSuccess()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(2.dp)
                )
            } else {
                Text("Daftar Sekarang")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { 
            Text("Sudah punya akun? ")
            Text("Login", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onGoToLogin() })
        }
    }
}

fun validateRegisterInput(username: String, nama: String, phoneNumber: String, pass: String, retypePass: String): String? {
    if (username.isBlank()) {
        return "Username tidak boleh kosong"
    }
    if (username.length < 3) {
        return "Username minimal 3 karakter"
    }
    if (nama.isBlank()) {
        return "Nama lengkap tidak boleh kosong"
    }
    if (phoneNumber.isBlank()) {
        return "Nomor telepon tidak boleh kosong"
    }
    val cleanPhone = phoneNumber.replace(" ", "").replace("-", "")
    if (!cleanPhone.startsWith("08") && !cleanPhone.startsWith("+62")) {
        return "Nomor telepon harus diawali dengan 08 atau +62"
    }
    val digitsOnly = if (cleanPhone.startsWith("+")) cleanPhone.substring(1) else cleanPhone
    if (!digitsOnly.all { it.isDigit() }) {
        return "Format nomor telepon tidak valid"
    }
    if (cleanPhone.startsWith("08")) {
        if (cleanPhone.length !in 10..15) {
            return "Nomor telepon (format 08...) harus antara 10 hingga 15 digit"
        }
    } else if (cleanPhone.startsWith("+62")) {
        if (cleanPhone.length !in 11..16) {
            return "Nomor telepon (format +62...) harus antara 11 hingga 16 karakter"
        }
    }
    if (pass.length !in 6..18) {
        return "Password harus antara 6 hingga 18 karakter"
    }
    if (!pass.any { it.isUpperCase() }) {
        return "Password harus mengandung minimal 1 huruf besar"
    }
    if (!pass.any { it.isLowerCase() }) {
        return "Password harus mengandung minimal 1 huruf kecil"
    }
    val specialCharacters = setOf(
        '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
        '-', '_', '=', '+', '[', ']', '{', '}', ';', ':',
        '\'', '"', '\\', '|', ',', '.', '<', '>', '/', '?', '`', '~'
    )
    if (!pass.any { it in specialCharacters }) {
        return "Password harus mengandung minimal 1 karakter spesial (#, @, $, dll)"
    }
    if (pass != retypePass) {
        return "Password dan Retype Password tidak cocok"
    }
    return null
}
