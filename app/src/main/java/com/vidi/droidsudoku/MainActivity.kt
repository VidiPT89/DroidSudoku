package com.vidi.droidsudoku

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.vidi.droidsudoku.data.Difficulty
import com.vidi.droidsudoku.engine.GameEngine
import com.vidi.droidsudoku.engine.SaveStore
import com.vidi.droidsudoku.i18n.Localization
import com.vidi.droidsudoku.ui.screens.GameScreen
import com.vidi.droidsudoku.ui.screens.HowToPlayScreen
import com.vidi.droidsudoku.ui.screens.MainMenuScreen
import com.vidi.droidsudoku.ui.screens.SplashScreen

private enum class AppScreen { SPLASH, MENU, HOW_TO_PLAY, GAME }

private const val PREFS = "droidsudoku_prefs"
private const val KEY_DIFFICULTY = "difficulty"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RootApp()
        }
    }
}

private fun loadStoredDifficulty(context: Context): Difficulty {
    val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DIFFICULTY, null)
    return Difficulty.entries.firstOrNull { it.name == stored } ?: Difficulty.EASY
}

private fun storeDifficulty(context: Context, difficulty: Difficulty) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString(KEY_DIFFICULTY, difficulty.name)
        .apply()
}

@Composable
private fun RootApp() {
    val context = LocalContext.current
    val loc = remember { Localization(context) }
    val saveStore = remember { SaveStore(context) }

    var difficulty by remember { mutableStateOf(loadStoredDifficulty(context)) }
    var engine by remember { mutableStateOf<GameEngine?>(null) }
    var screen by remember { mutableStateOf(AppScreen.SPLASH) }
    var hasSave by remember { mutableStateOf(saveStore.hasSave()) }

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        when (screen) {
            AppScreen.SPLASH -> SplashScreen(loc) {
                screen = AppScreen.MENU
            }
            AppScreen.MENU -> MainMenuScreen(
                loc = loc,
                hasSave = hasSave,
                selectedDifficulty = difficulty,
                onDifficultyChange = {
                    difficulty = it
                    storeDifficulty(context, it)
                },
                onPlay = {
                    engine = GameEngine.newGame(difficulty)
                    saveStore.clear()
                    hasSave = false
                    screen = AppScreen.GAME
                },
                onContinue = {
                    val snapshot = saveStore.load()
                    engine = if (snapshot != null) GameEngine.fromSnapshot(snapshot) else GameEngine.newGame(difficulty)
                    screen = AppScreen.GAME
                },
                onHowToPlay = { screen = AppScreen.HOW_TO_PLAY },
                onToggleLang = { loc.toggle() }
            )
            AppScreen.HOW_TO_PLAY -> HowToPlayScreen(loc) {
                screen = AppScreen.MENU
            }
            AppScreen.GAME -> engine?.let { currentEngine ->
                GameScreen(
                    engine = currentEngine,
                    loc = loc,
                    onExit = {
                        hasSave = saveStore.hasSave()
                        screen = AppScreen.MENU
                    },
                    onNewGame = {
                        engine = GameEngine.newGame(difficulty)
                        saveStore.clear()
                    }
                )
            }
        }
    }
}
