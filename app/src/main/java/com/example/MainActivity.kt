package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.core.configureAppCheck
import com.example.data.model.UserSettings
import com.example.data.repository.NotesRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.main.MainAppScaffold
import com.example.ui.theme.AnmolNotesTheme
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.FiraCodeFontFamily
import com.example.ui.theme.HackerFont
import com.example.ui.theme.HackerFontSize
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val repository = remember { NotesRepository(applicationContext) }

            // Dynamic Font & Theme state from cloud settings
            var currentFontFamily by remember { mutableStateOf(FiraCodeFontFamily) }
            var currentFontScale by remember { mutableStateOf(1.0f) }

            AnmolNotesTheme(
                fontFamily = currentFontFamily,
                fontSizeScale = currentFontScale
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyberBlack)
                        .safeDrawingPadding(),
                    color = CyberBlack
                ) {
                    AppRoot(
                        repository = repository,
                        onSettingsLoaded = { settings ->
                            val matchedFont = HackerFont.entries.firstOrNull { it.displayName == settings.fontFamily }
                            if (matchedFont != null) {
                                currentFontFamily = matchedFont.fontFamily
                            }
                            val matchedSize = HackerFontSize.entries.firstOrNull { it.name == settings.fontSize }
                            if (matchedSize != null) {
                                currentFontScale = matchedSize.scale
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppRoot(
    repository: NotesRepository,
    onSettingsLoaded: (UserSettings) -> Unit
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        val uid = currentUser!!.uid

        // Observe settings for typography
        val settingsFlow = remember(uid) { repository.observeSettings() }
        val settings by settingsFlow.collectAsState(initial = UserSettings(userId = uid, userName = "Anamol"))

        DisposableEffect(settings) {
            onSettingsLoaded(settings)
            onDispose {}
        }

        key(uid) {
            MainAppScaffold(
                repository = repository,
                currentUserId = uid,
                onSignOut = {
                    currentUser = null
                }
            )
        }
    }
}
