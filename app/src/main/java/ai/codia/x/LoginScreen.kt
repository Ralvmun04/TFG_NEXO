package ai.codia.x.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Parte superior azul con título
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF2381CD)), // Azul corporativo
            contentAlignment = Alignment.Center
        ) {
            Text("Iniciar Sesión", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        // Parte inferior gris
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF7FF)) // Gris claro
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Text("Correo electrónico", fontSize = 16.sp, color = Color.Black)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Introduce tu correo") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text("Contraseña", fontSize = 16.sp, color = Color.Black)
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    placeholder = { Text("Introduce tu contraseña") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                Button(
                    onClick = {
                        if (email.isNotEmpty() && contrasena.isNotEmpty()) {
                            FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email, contrasena)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.navigate("home")
                                    } else {
                                        Toast.makeText(
                                            context,
                                            task.exception?.message ?: "Error",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(top = 16.dp)
            ) {
                Text("¿No tienes una cuenta?", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Crear una cuenta",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2381CD),
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    }
                )
            }
        }
    }
}
