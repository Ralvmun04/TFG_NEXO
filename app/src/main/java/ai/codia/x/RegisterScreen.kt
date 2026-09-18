package ai.codia.x.auth

import android.widget.EditText
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
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var Contrasena by remember { mutableStateOf("") }
    var confContrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF2381CD)),
            contentAlignment = Alignment.Center
        ) {
            Text("Registrarse", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF7FF))
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
                    value = Contrasena,
                    onValueChange = { Contrasena = it },
                    placeholder = { Text("Introduce tu contraseña") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text("Confirmar contraseña", fontSize = 16.sp, color = Color.Black)
                OutlinedTextField(
                    value = confContrasena,
                    onValueChange = { confContrasena = it },
                    placeholder = { Text("Repite tu contraseña") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                Button(
                    onClick = {
                        if (email.isNotEmpty() && Contrasena.isNotEmpty() && Contrasena == confContrasena) {
                            FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email, Contrasena)
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
                        } else {
                            Toast.makeText(context, "Revisa los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2381CD))
                ) {
                    Text("Crear cuenta", color = Color.White)
                }
            }

            Text(
                text = "¿Ya tienes una cuenta? Iniciar sesión",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2381CD),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clickable { navController.navigate("login") }
            )
        }
    }
}
