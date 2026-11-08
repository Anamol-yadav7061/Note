package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.error.OperationType
import com.example.data.error.handleFirestoreError
import com.example.data.model.JarvisMessage
import com.example.data.model.Note
import com.example.data.model.UserSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NotesRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // Observe all notes in real time
    fun observeNotes(): Flow<List<Note>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notes"
        emitAll(
            db.collection("users").document(uid).collection("notes")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Note::class.java).sortedWith(
                        compareByDescending<Note> { it.isPinned }
                            .thenByDescending { it.updatedAt ?: it.createdAt }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveNote(note: Note): String {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("notes")
        val docRef = if (note.id.isNotBlank()) collectionRef.document(note.id) else collectionRef.document()

        val isCreate = note.id.isBlank() || note.createdAt == null

        val payload = mutableMapOf<String, Any?>(
            "userId" to uid,
            "title" to note.title,
            "content" to note.content,
            "category" to note.category,
            "tags" to note.tags,
            "colorTheme" to note.colorTheme,
            "isVault" to note.isVault,
            "isPinned" to note.isPinned,
            "isArchived" to note.isArchived,
            "noteType" to note.noteType,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (note.drawingData != null) {
            payload["drawingData"] = note.drawingData
        }
        if (note.imageUrl != null) {
            payload["imageUrl"] = note.imageUrl
        }

        if (isCreate) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        } else {
            payload["createdAt"] = note.createdAt!!
        }

        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isCreate) OperationType.CREATE else OperationType.UPDATE,
                docRef.path
            )
            throw e
        }
    }

    suspend fun deleteNote(noteId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Bulk Import Notes (e.g. from Keep, JSON, or external backups)
    suspend fun batchImportNotes(notes: List<Note>): Int {
        val uid = requireUserId()
        var importedCount = 0
        val batch = db.batch()
        val collectionRef = db.collection("users").document(uid).collection("notes")

        for (note in notes) {
            val docRef = collectionRef.document()
            val payload = mapOf(
                "userId" to uid,
                "title" to note.title,
                "content" to note.content,
                "category" to (if (note.category.isNotBlank()) note.category else "KeepBackup"),
                "tags" to note.tags,
                "colorTheme" to note.colorTheme,
                "isVault" to note.isVault,
                "isPinned" to note.isPinned,
                "isArchived" to note.isArchived,
                "noteType" to note.noteType,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(docRef, payload)
            importedCount++
        }
        batch.commit().await()
        return importedCount
    }

    // Jarvis real-time message history
    fun observeJarvisMessages(): Flow<List<JarvisMessage>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/jarvis_messages"
        emitAll(
            db.collection("users").document(uid).collection("jarvis_messages")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(JarvisMessage::class.java).sortedBy {
                        it.createdAt?.toDate()?.time ?: 0L
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveJarvisMessage(msg: JarvisMessage): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("jarvis_messages").document()
        val payload = mutableMapOf<String, Any?>(
            "userId" to uid,
            "sender" to msg.sender,
            "message" to msg.message,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (msg.pythonCode != null) payload["pythonCode"] = msg.pythonCode
        if (msg.suggestedCategory != null) payload["suggestedCategory"] = msg.suggestedCategory

        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun clearJarvisHistory() {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("jarvis_messages")
        val snapshots = collection.get().await()
        val batch = db.batch()
        for (doc in snapshots.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    // User Settings
    fun observeSettings(): Flow<UserSettings> = callbackFlow {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("user_settings")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                return@addSnapshotListener
            }
            val settings = snapshot?.toObject(UserSettings::class.java)
                ?: UserSettings(userId = uid, userName = "Anamol")
            trySend(settings)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveSettings(settings: UserSettings) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("user_settings")
        val payload = mapOf(
            "userId" to uid,
            "fontFamily" to settings.fontFamily,
            "fontSize" to settings.fontSize,
            "terminalTheme" to settings.terminalTheme,
            "vaultPin" to settings.vaultPin,
            "userName" to settings.userName,
            "geminiApiKey" to settings.geminiApiKey,
            "createdAt" to (settings.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
