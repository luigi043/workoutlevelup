# Screen index

The Android captures below come from a real API 35 emulator running the debug APK with synthetic workout data. Smaller copies are available in the [public Android screen gallery](https://workout-level-up-portfolio.vercel.app/gallery.html). The browser captures come from the [public interactive demo](https://workout-level-up-portfolio.vercel.app/) in Edge. Browser storage is separate from Android Room, and the browser does not show Android permission dialogs or reminders.

| Android screen or state | How to reach it | Capture |
| --- | --- | --- |
| Notification permission | Open a fresh installation on API 33 or later | [01](native/01-notification-permission.png) |
| Current month calendar | Decline the permission prompt or tap Calendar | [02](native/02-calendar.png) |
| Previous month | Tap the previous month arrow | [03](native/03-calendar-previous-month.png) |
| Locked badges and starting rank | Tap Badges before logging a workout | [04](native/04-badges-locked.png) |
| Daily workout checklist | Tap Today or choose an unfinished past date | [05](native/05-workout-checklist.png) |
| Running timer | Tap Start timer | [06](native/06-timer-running.png) |
| Ready to complete | Pause the timer and check each exercise | [07](native/07-workout-ready.png) |
| Earned badge and updated progress | Tap Mark Workout Complete after checking every exercise | [08](native/08-badges-earned.png) |
| Completed workout history | Tap Today again after logging the workout | [09](native/09-completed-workout.png) |
| Calendar with saved progress | Relaunch the app after saving a workout | [10](native/10-calendar-after-restart.png) |

| Browser screen or state | How to reach it | Phone | Desktop |
| --- | --- | --- | --- |
| Calendar | Open the demo or select Calendar | [01](browser/phone-01-calendar.png) | [01](browser/desktop-01-calendar.png) |
| Workout checklist | Select Today | [02](browser/phone-02-workout.png) | [02](browser/desktop-02-workout.png) |
| Progress and earned badges | Start and pause the timer, check each exercise and save | [03](browser/phone-03-badges.png) | [03](browser/desktop-03-badges.png) |
| Completed workout | Reload and open today's date from the calendar | [04](browser/phone-04-completed.png) | [04](browser/desktop-04-completed.png) |

The Android notification shade and save failure message were outside the captured workflow. A scheduled reminder requires enabled notifications and an Android background work window. The save failure message requires a Room write failure.
