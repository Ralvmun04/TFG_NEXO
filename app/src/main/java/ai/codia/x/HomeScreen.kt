package ai.codia.x

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val dbViewModel: DatabaseViewModel = viewModel()
    val databaseList by dbViewModel.databases.collectAsState()
    var Dialog by remember { mutableStateOf(false) }
    var newDatabaseName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTab by remember { mutableStateOf(0) }
    var expandedMenuIndex by remember { mutableStateOf<Int?>(null) }
    var editDialogText by remember { mutableStateOf(TextFieldValue("")) }
    var showEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val input = context.contentResolver.openInputStream(uri)
                val outputFile = File(context.filesDir, "imported_${System.currentTimeMillis()}.db")
                input?.use { ins -> outputFile.outputStream().use { ins.copyTo(it) } }

                if (newDatabaseName.text.isNotBlank()) {
                    dbViewModel.addDatabaseWithPath(newDatabaseName.text, outputFile.absolutePath)
                    newDatabaseName = TextFieldValue("")
                    Dialog = false
                }
            }
        }
    )

    fun getInitial(text: String): String =
        text.firstOrNull()?.uppercase() ?: "A"

    if (Dialog) {
        AlertDialog(
            onDismissRequest = { Dialog = false },
            confirmButton = {
                TextButton(onClick = { importLauncher.launch("*/*") }) {
                    Text("Importar .db")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Dialog = false
                    newDatabaseName = TextFieldValue("")
                }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Crear nueva base de datos") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDatabaseName,
                        onValueChange = { newDatabaseName = it },
                        label = { Text("Nombre de la base de datos") }
                    )
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val index = expandedMenuIndex
                    if (index != null && editDialogText.text.isNotBlank()) {
                        dbViewModel.updateDatabase(index, editDialogText.text)
                    }
                    showEditDialog = false
                    editDialogText = TextFieldValue("")
                    expandedMenuIndex = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    editDialogText = TextFieldValue("")
                    expandedMenuIndex = null
                }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Editar base de datos") },
            text = {
                OutlinedTextField(
                    value = editDialogText,
                    onValueChange = { editDialogText = it },
                    label = { Text("Nuevo nombre") }
                )
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Bases de Datos") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Person, contentDescription = "Usuario")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { Dialog = true },
                    modifier = Modifier.padding(bottom = 80.dp),
                    containerColor = Color(0xFF2381CD)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFFEF7FF),
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = 100.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(databaseList) { index, entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val encodedPath = Uri.encode(entry.path)
                                    navController.navigate("database/${entry.name}/$encodedPath")
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2381CD)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = getInitial(entry.name),
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = entry.name,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Base de datos local",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Box {
                                    IconButton(onClick = {
                                        expandedMenuIndex = if (expandedMenuIndex == index) null else index
                                    }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                    }
                                    DropdownMenu(
                                        expanded = expandedMenuIndex == index,
                                        onDismissRequest = { expandedMenuIndex = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Editar") },
                                            onClick = {
                                                editDialogText = TextFieldValue(entry.name)
                                                showEditDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Edit, contentDescription = null)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar") },
                                            onClick = {
                                                dbViewModel.removeDatabase(index)
                                                expandedMenuIndex = null
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Info, contentDescription = null)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .height(64.dp)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { selectedTab = 0 }) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Databases",
                                tint = if (selectedTab == 0) Color(0xFF2381CD) else Color.Gray
                            )
                        }
                        IconButton(onClick = { selectedTab = 1 }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (selectedTab == 1) Color(0xFF2381CD) else Color.Gray
                            )
                        }
                        IconButton(onClick = { selectedTab = 2 }) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Info",
                                tint = if (selectedTab == 2) Color(0xFF2381CD) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    )
}
