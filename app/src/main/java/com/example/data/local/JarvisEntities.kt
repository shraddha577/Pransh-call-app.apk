package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: String,
  val role: String,
  val content: String,
  val actionTag: String = "",
  val isSampleAi: Boolean = false,
  val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "command_macros")
data class CommandMacroEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: String,
  val title: String,
  val triggerPhrase: String,
  val description: String,
  val category: String,
  val createdAt: Long = System.currentTimeMillis(),
)
