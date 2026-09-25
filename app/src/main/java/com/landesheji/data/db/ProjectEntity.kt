package com.landesheji.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val canvasJson: String,
    val layersJson: String,
    val layerCount: Int,
    val updatedAt: Long = System.currentTimeMillis(),
    val previewColorArgb: Int = 0xFF222831.toInt()
)
