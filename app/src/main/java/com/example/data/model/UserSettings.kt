package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserSettings(
    @DocumentId val id: String = "user_settings",
    val userId: String = "",
    val fontFamily: String = "Fira Code",
    val fontSize: String = "MEDIUM",
    val terminalTheme: String = "MATRIX_GREEN",
    val vaultPin: String = "",
    val userName: String = "Anamol",
    val geminiApiKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
