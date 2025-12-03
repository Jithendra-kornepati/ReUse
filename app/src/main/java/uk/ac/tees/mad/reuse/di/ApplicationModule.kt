package uk.ac.tees.mad.reuse.di

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uk.ac.tees.mad.reuse.data.AppUtils
import uk.ac.tees.mad.reuse.data.repository.SavedIdeasRepository
import uk.ac.tees.mad.reuse.presentation.auth.AuthViewmodel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun providesAuth() : FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun providesFirestore() : FirebaseFirestore = Firebase.firestore


    @Provides @Singleton
    fun provideAppUtils(
        @ApplicationContext ctx: Context,
        auth: FirebaseAuth,
        repository: SavedIdeasRepository
    ): AppUtils = AppUtils(ctx, auth = auth , repository)

}