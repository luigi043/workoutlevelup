package com.example.workoutlevelup

import android.app.Application
import com.example.workoutlevelup.notifications.WorkoutReminderWorker

class WorkoutApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Schedule the daily reminder at 18:00 (6 PM) every day
        WorkoutReminderWorker.scheduleDailyReminder(
            context    = this,
            hourOfDay  = 18,
            minute     = 0
        )
    }
}
