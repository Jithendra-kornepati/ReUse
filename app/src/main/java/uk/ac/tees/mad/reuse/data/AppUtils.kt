package uk.ac.tees.mad.reuse.data

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import uk.ac.tees.mad.reuse.MainActivity
import uk.ac.tees.mad.reuse.R
import uk.ac.tees.mad.reuse.data.repository.SavedIdeasRepository
import uk.ac.tees.mad.reuse.presentation.auth.AuthViewmodel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val repository: SavedIdeasRepository
) {

    private val isLoggedIn: Boolean
        get() = auth.currentUser != null

    companion object {
        private const val CHANNEL_ID = "saved_idea_channel"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Saved Ideas"
            val descriptionText = "Notifications for your saved ideas"
            val importance = AndroidNotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = context.getSystemService(AndroidNotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun sendNotification(
        title: String,
        message: String,
        intent: PendingIntent? = null
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.re_use_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, builder.build())
    }

    suspend fun sendSavedIdeaNotification() {
        if (!isLoggedIn) return
        Log.d("Notifications", "Were here")
        val idea = repository.getOneSavedIdea() ?: return
        Log.d("Notifications", idea.toString())
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                putExtra("idea_id", idea.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        sendNotification(
            title = "Your Saved Idea",
            message = idea.title ?: "You have a saved idea waiting!",
            intent = pendingIntent
        )
    }
}