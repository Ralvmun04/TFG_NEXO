package ai.codia.x

import ai.codia.x.auth.LoginScreen
import ai.codia.x.auth.RegisterScreen
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.*
import com.google.firebase.FirebaseApp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        requestWindowFeature(Window.FEATURE_NO_TITLE)
        FirebaseApp.initializeApp(this)
        setContent {
            Text("Probando la API...")
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2196F3),
                    onPrimary = Color.White,
                    background = Color.White,
                    surface = Color.White
                )
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(navController)
                    }
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable("register") {
                        RegisterScreen(navController)
                    }
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("database/{name}/{path}") { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val path = backStackEntry.arguments?.getString("path") ?: ""
                        DatabaseScreen(navController, name, path)
                    }
                }
            }
        }
    }
}

