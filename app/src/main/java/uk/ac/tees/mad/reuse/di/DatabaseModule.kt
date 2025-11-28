package uk.ac.tees.mad.reuse.di


import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uk.ac.tees.mad.reuse.data.local.AppDatabase
import uk.ac.tees.mad.reuse.data.local.ReuseIdeaDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "reuse.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideReuseIdeaDao(db: AppDatabase): ReuseIdeaDao = db.reuseIdeaDao()
}
