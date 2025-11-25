package org.fossify.camera.database

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.fossify.camera.models.CapturedMedia

/**
 * Simple file-based storage for captured media using SharedPreferences.
 * This is a temporary implementation until Room database KSP issues are resolved.
 */
class SimpleMediaStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("media_storage", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val mediaListFlow = MutableStateFlow<List<CapturedMedia>>(emptyList())
    
    init {
        // Load initial data
        mediaListFlow.value = loadAllMedia()
    }
    
    fun getAllMedia(): Flow<List<CapturedMedia>> = mediaListFlow
    
    fun insertMedia(media: CapturedMedia): Long {
        val currentList = loadAllMedia().toMutableList()
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        val mediaWithId = media.copy(id = newId)
        currentList.add(0, mediaWithId) // Add at beginning for newest first
        saveAllMedia(currentList)
        mediaListFlow.value = currentList
        return newId
    }
    
    fun getMediaById(id: Long): CapturedMedia? {
        return loadAllMedia().firstOrNull { it.id == id }
    }
    
    fun updateMedia(media: CapturedMedia) {
        val currentList = loadAllMedia().toMutableList()
        val index = currentList.indexOfFirst { it.id == media.id }
        if (index != -1) {
            currentList[index] = media
            saveAllMedia(currentList)
            mediaListFlow.value = currentList
        }
    }
    
    private fun loadAllMedia(): List<CapturedMedia> {
        val json = prefs.getString("media_list", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CapturedMedia>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveAllMedia(list: List<CapturedMedia>) {
        val json = gson.toJson(list)
        prefs.edit().putString("media_list", json).apply()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SimpleMediaStorage? = null
        
        fun getInstance(context: Context): SimpleMediaStorage {
            return INSTANCE ?: synchronized(this) {
                val instance = SimpleMediaStorage(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
