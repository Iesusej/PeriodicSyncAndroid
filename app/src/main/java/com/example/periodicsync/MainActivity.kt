package com.example.periodicsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import com.example.periodicsync.AppTheme
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

@Entity(tableName = "counts")
data class CountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val count: Int
)

@Dao
interface CountDao {
    @Query("SELECT * FROM counts")
    fun getAll(): Flow<List<CountEntity>>

    @Insert
    suspend fun insert(entity: CountEntity)

    @Query("DELETE FROM counts")
    suspend fun clear()
}

@Database(entities = [CountEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): CountDao
}

class CountRepository(private val dao: CountDao) {
    val counts: Flow<Int> = dao.getAll().map { it.sumOf { e -> e.count } }
}

class MainViewModel(private val repo: CountRepository) : ViewModel() {
    val total = repo.counts
}

class MainViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(CountRepository(db.dao())) as T
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "counts.db"
        ).build()

        scheduleSync()

        val factory = MainViewModelFactory(database)
        setContent {
            val viewModel: MainViewModel = viewModel(factory = factory)
            AppTheme {
                Surface { Content(viewModel) }
            }
        }
    }

    private fun scheduleSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}

@Composable
fun Content(viewModel: MainViewModel) {
    val total = viewModel.total.collectAsState(initial = 0)
    androidx.compose.material3.Text("Total: ${'$'}{total.value}")
}

@Preview
@Composable
fun PreviewContent() {
    Content(viewModel())
}
