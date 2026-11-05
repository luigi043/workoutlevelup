# Workout Level Up verification

November 5, 2026. Testing used synthetic workout entries. The app serves an individual following the bundled workout plan and stores progress on the device. It has no login, server, or external workout hardware integration.

| Check | Result |
| --- | --- |
| Android debug build | Passed with JDK 17, SDK 34, AGP 8.4 and Gradle 8.9 |
| Android lint | Passed with 0 errors and 16 warnings. The initial offline run lacked the lint dependency. |
| Native API 35 emulator | Passed. Installed the debug APK and exercised notification denial, calendar navigation, the checklist and timer, Room persistence after relaunch, badges, and completion history. |
| Local and public Edge walkthroughs | Passed at 320 × 640, 390 × 844 and 1440 × 1000. No horizontal page overflow or JavaScript page errors were observed. |
| Public gallery | All ten Android captures loaded at 390 × 844 without horizontal overflow. |

The browser test checks month navigation, disabled completion before checking exercises, timer ticks, badge earning, and persistence after reloading and reopening the completed date. An additional regression test checks that a dated checklist resets if another workout changes the assigned plan. The main app stays within a 430 px column at each tested viewport. Mobile checks use emulated touch viewports in desktop Edge.

To reproduce the native workflow, build the app, install the APK on a test emulator with an English system language, and run `native-walkthrough.ps1 -Adb <path-to-adb> -Serial <emulator-serial>`. The script clears Workout Level Up data on that emulator and records screenshots as it verifies the workflow. Run `node demo/verify.mjs` with Playwright installed to test the local demo, or set `DEMO_URL` to test the deployed version. `PLAYWRIGHT_MODULE` and `BROWSER_EXECUTABLE` can select an existing local installation.

The [debug APK](downloads/workout-level-up-debug.apk) was built from Android source commit `2a14f34`. Its [manifest](downloads/build-info.json) contains the digest. The [public Vercel site](https://workout-level-up-portfolio.vercel.app/) serves the static demo and smaller copies of the ten Android captures from commit `f9590fe`. GitHub access is required for the APK and full-resolution originals.

Physical devices and Android versions other than API 35 were outside this verification. Android controls the timing of WorkManager reminders. Delivery after an overnight interval, reboot, or time zone change was outside this test. The browser timer and local storage are independent of Android. Android's debug signing key is intended for testing.
