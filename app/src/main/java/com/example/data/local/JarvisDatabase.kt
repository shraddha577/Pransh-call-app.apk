package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [ChatMessageEntity::class, CommandMacroEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class JarvisDatabase : RoomDatabase() {
  abstract fun jarvisDao(): JarvisDao

  companion object {
    @Volatile
    private var instance: JarvisDatabase? = null

    fun getInstance(context: Context): JarvisDatabase =
      instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                JarvisDatabase::class.java,
                "jarvis_ai_local.db",
              )
              .build()
              .also { instance = it }
        }
  }
}
