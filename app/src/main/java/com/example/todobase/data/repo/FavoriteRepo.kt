package com.example.todobase.data.repo

import com.example.todobase.data.models.FavoritePlace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteRepo {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun favoritePlaceCollection() = db.collection("users").document(auth.currentUser?.uid ?: "").collection("favorite_places")
    fun addFavoritePlace(place: FavoritePlace, onComplete: (Boolean, String, FavoritePlace?) -> Unit) {
        val user = auth.currentUser
        
        if(user == null){
            onComplete(false, "User not logged in", null)
            return
        }

        val doc = favoritePlaceCollection().document()
        val newPlace = place.copy(uid = doc.id)
        
        doc.set(newPlace)
            .addOnSuccessListener {
                onComplete(true, "Favorite place added successfully", newPlace)
            }
            .addOnFailureListener {
                onComplete(false, "Failed to add favorite place", null)
            }
    }
    
    fun getFavoritePlaces(onComplete: (List<FavoritePlace>?, String?) -> Unit) {
        val user = auth.currentUser
        
        if(user == null){
            onComplete(null, "User not logged in")
            return
        }
        
        favoritePlaceCollection()
            .get()
            .addOnSuccessListener { querySnapshot ->
                val places = querySnapshot.toObjects(FavoritePlace::class.java)
                onComplete(places, null)
            }
            .addOnFailureListener {
                onComplete(null, "Failed to retrieve favorite places")
            }
    }
    
    fun updateFavoritePlace(place: FavoritePlace, onComplete: (Boolean, String) -> Unit) {
        if(auth.currentUser == null){
            onComplete(false, "User not logged in")
            return
        }

        favoritePlaceCollection()
            .document(place.uid)
            .set(place)
            .addOnSuccessListener { 
                onComplete(true, "Favorite place updated successfully")
            }
            .addOnFailureListener {
                onComplete(false, "Failed to update favorite place")
            }
    }
    
    fun deleteFavoritePlace(placeId:String, onComplete: (Boolean, String) -> Unit) {
        if(auth.currentUser == null){
            onComplete(false, "User not logged in")
            return
        }
        
        favoritePlaceCollection()
            .document(placeId)
            .delete()
            .addOnSuccessListener { 
                onComplete(true, "Favorite place deleted successfully")
            }
            .addOnFailureListener {
                onComplete(false, "Failed to delete favorite place")
            }
    }
}