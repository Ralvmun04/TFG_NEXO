package ai.codia.x

import ai.codia.x.api.OpenAiResponse
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(navController: NavHostController, name: String, path: String) {
    val data = remember { mutableStateListOf<Map<String, String>>() }
    var tableNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedTable by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var menuVisibleIndex by remember { mutableStateOf<Int?>(null) }
    var selectedRow by remember { mutableStateOf<Map<String, String>?>(null) }
    var editableRow by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showEditRowDialog by remember { mutableStateOf(false) }
    var primaryKeyColumn by remember { mutableStateOf("") }
    var iaMode by remember { mutableStateOf(false) }

    fun runSqlQuery(sql: String) {
        try {
            val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.rawQuery(sql, null)
            val results = mutableListOf<Map<String, String>>()
            while (cursor.moveToNext()) {
                val row = mutableMapOf<String, String>()
                for (i in 0 until cursor.columnCount) {
                    row[cursor.getColumnName(i)] = cursor.getString(i) ?: ""
                }
                results.add(row)
            }
            cursor.close()
            db.close()
            data.clear()
            data.addAll(results)
        } catch (e: Exception) {
            data.clear()
            data.add(mapOf("error" to "Consulta inválida: ${e.message}"))
        }
    }

    LaunchedEffect(path) {
        val dbFile = File(path)
        if (dbFile.exists()) {
            val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            val tableCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                null
            )
            val tables = mutableListOf<String>()
            while (tableCursor.moveToNext()) {
                tables.add(tableCursor.getString(0))
            }
            tableCursor.close()
            db.close()
            tableNames = tables
            if (tables.isNotEmpty()) {
                selectedTable = tables[0]
                runSqlQuery("SELECT * FROM \"${tables[0]}\"")
            }
        }
    }

    LaunchedEffect(selectedTable) {
        if (path.isNotBlank() && selectedTable.isNotBlank()) {
            runSqlQuery("SELECT * FROM \"$selectedTable\"")
            val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
            val pkCursor = db.rawQuery("PRAGMA table_info(\"$selectedTable\")", null)
            while (pkCursor.moveToNext()) {
                val isPk = pkCursor.getInt(pkCursor.getColumnIndexOrThrow("pk"))
                if (isPk == 1) {
                    primaryKeyColumn = pkCursor.getString(pkCursor.getColumnIndexOrThrow("name"))
                    break
                }
            }
            pkCursor.close()
            db.close()
        }
    }

    val filteredData = if (iaMode) data else data.filter { row ->
        searchQuery.isBlank() || row.values.any { it.contains(searchQuery, ignoreCase = true) }
    }

    if (showEditRowDialog && selectedRow != null) {
        AlertDialog(
            onDismissRequest = {
                showEditRowDialog = false
                selectedRow = null
            },
            title = { Text("Editar fila") },
            text = {
                Column {
                    editableRow.forEach { (col, value) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = {
                                editableRow = editableRow.toMutableMap().apply { put(col, it) }
                            },
                            label = { Text(col) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
                    val pk = primaryKeyColumn
                    val pkValue = selectedRow?.get(pk) ?: return@TextButton
                    val setClause = editableRow.entries.joinToString(", ") { "${it.key} = ?" }
                    val args = editableRow.values.toTypedArray()
                    db.execSQL("UPDATE \"$selectedTable\" SET $setClause WHERE $pk = ?", args + pkValue)
                    db.close()
                    showEditRowDialog = false
                    selectedRow = null
                    editableRow = emptyMap()
                    selectedTable = ""
                    selectedTable = selectedTable
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditRowDialog = false
                    selectedRow = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                modifier = Modifier.shadow(4.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        iaMode = false
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar... o pregunta con lenguaje natural") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true
                )
            }
            Button(
                onClick = {
                    val columns = try {
                        val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
                        val cursor = db.rawQuery("PRAGMA table_info(\"$selectedTable\")", null)
                        val colNames = mutableListOf<String>()
                        while (cursor.moveToNext()) {
                            val rawName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                            val cleaned = rawName.trim().replace(".", "_")
                            if (cleaned.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                                colNames.add(cleaned)
                            }
                        }
                        cursor.close()
                        db.close()
                        if (colNames.isNotEmpty()) colNames.joinToString(", ") else "ninguna"
                    } catch (e: Exception) {
                        Log.e("COLUMN_ERROR", "Error leyendo columnas: ${e.message}")
                        "ninguna"
                    }

                    val prompt = """
                        Actúa como un traductor de lenguaje natural a SQL.
                        Tienes una base de datos SQLite con una tabla llamada "$selectedTable".
                        Las columnas de esta tabla son: $columns

                        Devuélveme SOLO la consulta SQL.
                        Frase: "$searchQuery"
                       """.trimIndent()

                    val client = OkHttpClient()
                    val gson = Gson()
                    val mediaType = "application/json".toMediaType()

                    val requestJson = """
                        {
                        "model": "gpt-3.5-turbo",
                        "messages": [
                            {"role": "system", "content": "Eres un asistente que responde en SQL para SQLite"},
                            {"role": "user", "content": "$prompt"}
                        ],
                        "temperature": 0.2
                        }
                    """.trimIndent().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                        .addHeader("Content-Type", "application/json")
                        .post(requestJson)
                        .build()

                    client.newCall(request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            isLoading = false
                            data.clear()
                            data.add(mapOf("error" to "Fallo al conectar con ChatGPT: ${e.message}"))
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            isLoading = false
                            val body = response.body?.string()

                            try {
                                val openAiResponse = gson.fromJson(body, OpenAiResponse::class.java)
                                val fullText = openAiResponse.choices.firstOrNull()?.message?.content

                                val filtroSQL = Regex("""(?i)(select\s+.+?);?(\s|\n|$)""", RegexOption.DOT_MATCHES_ALL)
                                    .find(fullText ?: "")
                                    ?.value
                                    ?.replace("```sql", "")
                                    ?.replace("```", "")
                                    ?.trim()

                                Log.d("SQL_CLEANED", "Consulta extraída: $filtroSQL")

                                if (!filtroSQL.isNullOrBlank()) {
                                    runSqlQuery(filtroSQL)
                                } else {
                                    data.clear()
                                    data.add(mapOf("error" to "La IA no devolvió una consulta SQL válida."))
                                }
                            } catch (e: Exception) {
                                Log.e("PARSE_ERROR", "Error al procesar la respuesta: ${e.message}")
                                data.clear()
                                data.add(mapOf("error" to "Error al procesar la respuesta de la IA."))
                            }
                        }
                    })

                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("IA")
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            if (tableNames.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { expanded = true }) {
                        Text(if (selectedTable.isBlank()) "Seleccionar tabla" else selectedTable)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tableNames.forEach { table ->
                            DropdownMenuItem(
                                text = { Text(table) },
                                onClick = {
                                    selectedTable = table
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                itemsIndexed(filteredData) { index, row ->
                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    menuVisibleIndex = index
                                    selectedRow = row
                                    editableRow = row
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (row.containsKey("error")) {
                                    Text(
                                        row["error"] ?: "Error desconocido",
                                        color = Color.Red,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    row.forEach { (col, value) ->
                                        Text("$col: $value", fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        if (menuVisibleIndex == index) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.White, shape = RoundedCornerShape(50))
                                    .shadow(4.dp, shape = RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                IconButton(onClick = {
                                    val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
                                    val pk = primaryKeyColumn
                                    val pkValue = editableRow[pk] ?: ""
                                    db.execSQL("DELETE FROM \"$selectedTable\" WHERE $pk = ?", arrayOf(pkValue))
                                    db.close()
                                    menuVisibleIndex = null
                                    selectedRow = null
                                    selectedTable = ""
                                    selectedTable = selectedTable
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                                IconButton(onClick = {
                                    showEditRowDialog = true
                                    menuVisibleIndex = null
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

