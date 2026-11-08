package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class JarvisMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sender: String = "user", // "user" or "jarvis"
    val message: String = "",
    val pythonCode: String? = null,
    val suggestedCategory: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)
