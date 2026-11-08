package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Note(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "General",
    val tags: List<String> = emptyList(),
    val colorTheme: String = "#00FF66",
    val isVault: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val drawingData: String? = null,
    val imageUrl: String? = null,
    val noteType: String = "text", // "text", "python", "drawing", "photo"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
