package uz.ravshanbaxranov.data.db

import androidx.room.*
import uz.ravshanbaxranov.util.Converters


@Database(entities = [Run::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunDatabase : RoomDatabase() {

    abstract val dao: RunDao

}