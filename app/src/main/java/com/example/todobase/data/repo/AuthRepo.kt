package com.example.todobase.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.todobase.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import kotlin.io.encoding.Base64

class AuthRepo( 
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val fs: FirebaseFirestore = FirebaseFirestore.getInstance() 
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null
    
    fun register(email:String, password:String, username:String, profilePictureUrl:String = "", onComplete: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?:""
                    val user = User(uid, username, email, profilePictureUrl)
                        
                    fs.collection("users").document(uid).set(user)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                onComplete(true, "Registration Successful")
                            } else {
                                onComplete(false, "Registration Failed")
                            }
                        }
                    
                } else{
                    onComplete(false, "Registration Failed")
                }
            }
    }
    
    fun login(email:String, password:String, onComplete: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { 
            if (it.isSuccessful) {
                onComplete(true, "Login Successful")
            } else {
                onComplete(false, "Login Failed")
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
                        onComplete(null, "Failed to retrieve user profile")
                    }
                }
        } else{
            onComplete(null, "User not logged in")
        }
    }
    
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try{
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            val scaledBitmap = originalBitmap.scale(250, 250)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        } catch (e: Exception){
            null
        }
    }
    
    fun updateUserProfile(base64String: String, onComplete: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false, "User not logged in")
        
        fs.collection("users").document(uid).update("profilePictureUrl", base64String)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}