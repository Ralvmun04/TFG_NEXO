package ai.codia.x


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DatabaseViewModel : ViewModel() {
    private val _databases = MutableStateFlow<List<DatabaseEntry>>(emptyList())
    val databases: StateFlow<List<DatabaseEntry>> = _databases
    data class DatabaseEntry(val name: String, val path: String)


    fun addDatabase(name: String) {
        _databases.value = (_databases.value + name) as List<DatabaseEntry>
    }

    fun removeDatabase(index: Int) {
        val current = _databases.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _databases.value = current
        }
    }

    fun updateDatabase(index: Int, newName: String) {
        val current = _databases.value.toMutableList()
        if (index in current.indices) {
            val oldEntry = current[index]
            current[index] = oldEntry.copy(name = newName)
            _databases.value = current
        }
    }

    fun addDatabaseWithPath(name: String, path: String) {
        _databases.value = _databases.value + DatabaseEntry(name, path)
    }

    fun getDatabaseByName(name: String): DatabaseEntry? {
        return _databases.value.find { it.name == name }
    }
}
