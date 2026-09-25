package com.landesheji.data.db

import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.LayerItem
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProject(id: Long): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun saveProject(
        title: String,
        canvasConfig: CanvasConfig,
        layers: List<LayerItem>,
        existingId: Long = 0
    ): Long {
        val entity = ProjectEntity(
            id = existingId,
            title = title.ifBlank { "我的设计" },
            canvasJson = ProjectSerializer.serializeCanvas(canvasConfig),
            layersJson = ProjectSerializer.serializeLayers(layers),
            layerCount = layers.size,
            updatedAt = System.currentTimeMillis(),
            previewColorArgb = canvasConfig.backgroundColorArgb
        )
        return projectDao.insertProject(entity)
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }
}
