package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        TaskItem::class,
        SemesterItem::class,
        SubjectItem::class,
        AttendanceRecord::class,
        TimetableEntry::class,
        ExamItem::class,
        AssignmentItem::class,
        StudyTopic::class,
        HabitItem::class,
        NoteItem::class,
        FocusSession::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MySpaceDatabase : RoomDatabase() {

    abstract fun mySpaceDao(): MySpaceDao

    companion object {
        @Volatile
        private var INSTANCE: MySpaceDatabase? = null

        fun getDatabase(context: Context): MySpaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MySpaceDatabase::class.java,
                    "myspace_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
