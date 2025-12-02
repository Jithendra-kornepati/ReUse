package uk.ac.tees.mad.reuse

import android.app.Application
import android.util.Log
import com.cloudinary.android.Utils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltAndroidApp
class ReUse : Application() {

    override fun onCreate() {
        super.onCreate()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            sendNotification()
        }
    }
}

suspend fun sendNotification(){
    delay(10000)
    Log.d("Notificaation" ,"AA gaya")
}