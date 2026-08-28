package in.namolabs.laby.wear

import android.app.Application
import in.namolabs.laby.mesh.PowerManager
import in.namolabs.laby.wear.notification.WearNotificationCoordinator
import in.namolabs.laby.wear.ui.WearPeerIdentityState

class LabyWearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PowerManager.getInstance(applicationContext)
        WearNotificationCoordinator.getInstance(applicationContext)
        WearPeerIdentityState.initialize(applicationContext)
    }
}
