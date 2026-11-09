package com.example.todobase.ui

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.todobase.R
import com.example.todobase.data.models.User
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.data.services.MapSettingHelper
import com.example.todobase.data.services.MapStyleOptions

fun base64ToBitmap(base64String: String): ImageBitmap? {
    return try {
        val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bmp.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modif: Modifier = Modifier, onProfileUpdated: (() -> Unit)? = null) {
    val authRepo = remember { AuthRepo() }
    var user by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    var isEditProfile by remember { mutableStateOf(false) }
    var editNama by remember { mutableStateOf("") }
    var editPhoneNumber by remember { mutableStateOf("") }
    var isSavingProfile by remember { mutableStateOf(false) }

    var isChangePasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var retypeNewPassword by remember { mutableStateOf("") }
    var isOldPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isRetypeNewPasswordVisible by remember { mutableStateOf(false) }
    var isSavingPassword by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    var selectedStyle by remember { mutableStateOf(MapSettingHelper.getSelectedOption(context)) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploading = true
            val base64String = authRepo.uriToBase64(context, uri)
            if (base64String != null) {
                authRepo.updateUserProfile(base64String) { success, msg ->
                    isUploading = false
                    if (success) {
                        user = user?.copy(profilePictureUrl = base64String)
                        Toast.makeText(context, "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, msg ?: "Gagal mengubah foto", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                isUploading = false
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { 
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable {
                        showPhotoOptionsDialog = true
                    },
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
                
                if (isUploading) {
                    CircularProgressIndicator()
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Card Informasi User
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Informasi Pengguna",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                editNama = user?.nama ?: ""
                                editPhoneNumber = user?.phoneNumber ?: ""
                                isEditProfile = true
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                ProfileItemRow(label = "Username", value = user?.username ?: "-")
                ProfileItemRow(label = "Nama Lengkap", value = if (!user?.nama.isNullOrBlank()) user?.nama!! else "-")
                ProfileItemRow(label = "Nomor Telepon", value = if (!user?.phoneNumber.isNullOrBlank()) user?.phoneNumber!! else "-")

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        oldPassword = ""
                        newPassword = ""
                        retypeNewPassword = ""
                        isOldPasswordVisible = false
                        isNewPasswordVisible = false
                        isRetypeNewPasswordVisible = false
                        isChangePasswordDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Ganti Password",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ubah Password")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text("Pilih Tampilan Peta", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp)
                ) { 
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
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

    // Dialog Edit Profil (Nama & Nomor Telepon)
    if (isEditProfile) {
        AlertDialog(
            onDismissRequest = { if (!isSavingProfile) isEditProfile = false },
            title = { Text("Edit Informasi Pengguna") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editNama,
                        onValueChange = { editNama = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPhoneNumber,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '+' }) {
                                editPhoneNumber = input
                            }
                        },
                        label = { Text("Nomor Telepon") },
                        placeholder = { Text("08... atau +62...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validationError = validateProfileEditInput(editNama, editPhoneNumber)
                        if (validationError != null) {
                            Toast.makeText(context, validationError, Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        isSavingProfile = true
                        authRepo.updateUserInfo(
                            nama = editNama.trim(),
                            phoneNumber = editPhoneNumber.trim()
                        ) { success, message ->
                            isSavingProfile = false
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                user = user?.copy(
                                    nama = editNama.trim(),
                                    phoneNumber = editPhoneNumber.trim()
                                )
                                isEditProfile = false
                                onProfileUpdated?.invoke()
                            }
                        }
                    },
                    enabled = !isSavingProfile
                ) {
                    if (isSavingProfile) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Simpan")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditProfile = false }, enabled = !isSavingProfile) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Ubah Password
    if (isChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSavingPassword) isChangePasswordDialog = false },
            title = { Text("Ubah Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Password Lama") },
                        singleLine = true,
                        visualTransformation = if (isOldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val icon = if (isOldPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { isOldPasswordVisible = !isOldPasswordVisible }) {
                                Icon(imageVector = icon, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password Baru") },
                        singleLine = true,
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val icon = if (isNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(imageVector = icon, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = retypeNewPassword,
                        onValueChange = { retypeNewPassword = it },
                        label = { Text("Retype Password Baru") },
                        singleLine = true,
                        visualTransformation = if (isRetypeNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val icon = if (isRetypeNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { isRetypeNewPasswordVisible = !isRetypeNewPasswordVisible }) {
                                Icon(imageVector = icon, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validationError = validateChangePasswordInput(oldPassword, newPassword, retypeNewPassword)
                        if (validationError != null) {
                            Toast.makeText(context, validationError, Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        isSavingPassword = true
                        authRepo.changePassword(
                            oldPassword = oldPassword,
                            newPassword = newPassword
                        ) { success, message ->
                            isSavingPassword = false
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                isChangePasswordDialog = false
                            }
                        }
                    },
                    enabled = !isSavingPassword
                ) {
                    if (isSavingPassword) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Simpan")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { isChangePasswordDialog = false }, enabled = !isSavingPassword) {
                    Text("Batal")
                }
            }
        )
    }

    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = { Text("Foto Profil") },
            text = { Text("Pilih aksi untuk foto profil Anda") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoOptionsDialog = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text("Ganti Foto")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!user?.profilePictureUrl.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                showPhotoOptionsDialog = false
                                isUploading = true
                                authRepo.updateUserProfile("") { success, msg ->
                                    isUploading = false
                                    if (success) {
                                        user = user?.copy(profilePictureUrl = "")
                                        Toast.makeText(context, "Foto profil berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, msg ?: "Gagal menghapus foto", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text("Hapus Foto", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showPhotoOptionsDialog = false }) {
                        Text("Batal")
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

fun validateProfileEditInput(nama: String, phoneNumber: String): String? {
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
    return null
}

fun validateChangePasswordInput(oldPass: String, newPass: String, retypeNewPass: String): String? {
    if (oldPass.isBlank()) {
        return "Password lama wajib diisi"
    }
    if (newPass.length !in 6..18) {
        return "Password baru harus antara 6 hingga 18 karakter"
    }
    if (!newPass.any { it.isUpperCase() }) {
        return "Password baru harus mengandung minimal 1 huruf besar"
    }
    if (!newPass.any { it.isLowerCase() }) {
        return "Password baru harus mengandung minimal 1 huruf kecil"
    }
    val specialCharacters = setOf(
        '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
        '-', '_', '=', '+', '[', ']', '{', '}', ';', ':',
        '\'', '"', '\\', '|', ',', '.', '<', '>', '/', '?', '`', '~'
    )
    if (!newPass.any { it in specialCharacters }) {
        return "Password baru harus mengandung minimal 1 karakter spesial (#, @, $, dll)"
    }
    if (newPass != retypeNewPass) {
        return "Password baru dan Retype Password tidak cocok"
    }
    if (oldPass == newPass) {
        return "Password baru tidak boleh sama dengan password lama"
    }
    return null
}
