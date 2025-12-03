package uk.ac.tees.mad.reuse

import android.app.Application
import android.util.Log
import com.cloudinary.android.Utils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.ac.tees.mad.reuse.data.AppUtils
import javax.inject.Inject

@HiltAndroidApp
class ReUse : Application() {

    @Inject
    lateinit var appUtils: AppUtils
    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            sendDelayedNotification()
        }
    }

    private suspend fun sendDelayedNotification() {
        delay(10000L)

        appUtils.sendSavedIdeaNotification()
    }

    override fun onTerminate() {
        appScope.cancel()
        super.onTerminate()
    }
}