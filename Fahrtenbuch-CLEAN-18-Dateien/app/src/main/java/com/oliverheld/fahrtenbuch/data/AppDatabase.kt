
package com.oliverheld.fahrtenbuch.data
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [Fahrt::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun dao(): FahrtDao
}
