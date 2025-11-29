package com.kd.classmate.services

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.media.AudioAttributes // NEW IMPORT
import android.os.Build

interface SoundPlayer {
    fun playCycleEndSound()
}

class SoundPlayerImpl(private val context: Context) : SoundPlayer {

    private val notificationUri: Uri by lazy {
        // Use the system's default notification sound
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    override fun playCycleEndSound() {
        try {
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)

            // Set audio attributes to treat it as an alarm/notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }

            ringtone.play()
        } catch (e: Exception) {
            // Log or handle failure to play sound
            e.printStackTrace()
        }
    }
}