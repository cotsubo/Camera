package org.fossify.camera.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.fossify.camera.models.CapturedMedia

@Database(entities = [CapturedMedia::class], version = 1, exportSchema = false)
abstract class MediaDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    
    companion object {
        @Volatile
        private var INSTANCE: MediaDatabase? = null
        
        fun getDatabase(context: Context): MediaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediaDatabase::class.java,
                    "camera_media_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
