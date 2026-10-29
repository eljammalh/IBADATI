package com.ibadati.app

import android.app.Application
import com.ibadati.app.notify.Reminders

class IbadatiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Reminders.createChannel(this)
    }
}
