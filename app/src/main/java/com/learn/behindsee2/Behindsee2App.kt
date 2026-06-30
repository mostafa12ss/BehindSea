package com.learn.behindsee2

import android.app.Application
import com.cloudinary.android.MediaManager

class Behindsee2App : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "dgbdssl5t",
            "secure" to true
        )
        MediaManager.init(this, config)
    }
}
