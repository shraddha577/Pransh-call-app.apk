package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
  @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC, id ASC")
  fun observeMessages(userId: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity)

  @Query("DELETE FROM chat_messages WHERE userId = :userId")
  suspend fun clearMessagesForUser(userId: String)

  @Query("SELECT * FROM command_macros WHERE userId = :userId ORDER BY createdAt ASC, id ASC")
  fun observeMacros(userId: String): Flow<List<CommandMacroEntity>>

  @Query("SELECT COUNT(*) FROM command_macros WHERE userId = :userId")
  suspend fun countMacrosForUser(userId: String): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMacro(macro: CommandMacroEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMacros(macros: List<CommandMacroEntity>)

  @Query("DELETE FROM command_macros WHERE id = :macroId")
  suspend fun deleteMacroById(macroId: Long)
}
