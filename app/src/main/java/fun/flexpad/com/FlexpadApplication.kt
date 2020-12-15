package `fun`.flexpad.com

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FlexpadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}