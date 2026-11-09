package com.example.todobase.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.todobase.data.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

class AuthRepo( 
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val fs: FirebaseFirestore = FirebaseFirestore.getInstance() 
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    private fun usernameToEmail(username: String): String {
        val sanitized = username.trim().lowercase().replace("[^a-z0-9._-]".toRegex(), "")
        return "$sanitized@favplace.local"
    }

    fun register(
        username: String,
        nama: String,
        phoneNumber: String,
        password: String,
        profilePictureUrl: String = "",
        onComplete: (Boolean, String) -> Unit
    ) {
        val cleanUsername = username.trim()

        fs.collection("users")
            .whereEqualTo("username", cleanUsername)
            .get()
            .addOnCompleteListener { checkTask ->
                if (checkTask.isSuccessful && !checkTask.result.isEmpty) {
                    onComplete(false, "Username sudah digunakan, silakan pilih yang lain")
                    return@addOnCompleteListener
                }

                auth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: ""
                            val user = User(
                                id = uid,
                                username = cleanUsername,
                                nama = nama.trim(),
                                phoneNumber = phoneNumber.trim(),
                                profilePictureUrl = profilePictureUrl
                            )
                            fs.collection("users").document(uid).set(user)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        auth.signOut()
                                        onComplete(true, "Registrasi berhasil, silakan login")
                                    } else {
                                        onComplete(false, "Gagal menyimpan data ke database")
                                    }
                                }
                        } else {
                            onComplete(false, task.exception?.localizedMessage ?: "Registrasi gagal")
                        }
                    }
            }
    }
    
    fun login(username: String, password: String, onComplete: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, "Login berhasil")
            } else {
                onComplete(false, "Username atau password salah")
            }
        }
    }
        
    fun getUserProfile(onComplete: (User?, String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            fs.collection("users").document(uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.toObject(User::class.java)
                        onComplete(user, null)
                    } else {
                        onComplete(null, "Gagal memuat profil user")
                    }
                }
        } else {
            onComplete(null, "User belum login")
        }
    }
    
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            
            // Crop to center square to avoid aspect ratio distortion (gepeng)
            val dimension = minOf(originalBitmap.width, originalBitmap.height)
            val x = (originalBitmap.width - dimension) / 2
            val y = (originalBitmap.height - dimension) / 2
            val squareBitmap = Bitmap.createBitmap(originalBitmap, x, y, dimension, dimension)
            
            val scaledBitmap = squareBitmap.scale(250, 250)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
    
    fun updateUserProfile(base64String: String, onComplete: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false, "User belum login")
        
        fs.collection("users").document(uid).update("profilePictureUrl", base64String)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun updateUserInfo(
        nama: String,
        phoneNumber: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onComplete(false, "User belum login")
            return
        }

        val updates = mapOf(
            "nama" to nama.trim(),
            "phoneNumber" to phoneNumber.trim()
        )
        fs.collection("users").document(uid).update(updates)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    onComplete(true, "Informasi profil berhasil diperbarui")
                } else {
                    onComplete(false, dbTask.exception?.message ?: "Gagal memperbarui informasi user")
                }
            }
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            onComplete(false, "User belum login")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                onComplete(true, "Password berhasil diubah")
                            } else {
                                onComplete(false, updateTask.exception?.localizedMessage ?: "Gagal mengubah password")
                            }
                        }
                } else {
                    onComplete(false, "Password lama salah")
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}
