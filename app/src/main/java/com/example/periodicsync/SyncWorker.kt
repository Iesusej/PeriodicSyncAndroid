package com.example.periodicsync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "counts.db"
        ).build()
        val total = db.dao().getAll().map { it.sumOf { e -> e.count } }
        val value = total.first()
        Log.d("SyncWorker", "Syncing count: ${'$'}value")
        // TODO: perform actual network sync
        return Result.success()
    }
}
