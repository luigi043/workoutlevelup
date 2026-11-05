param(
    [Parameter(Mandatory)][string]$Adb,
    [Parameter(Mandatory)][string]$Serial
)
$ErrorActionPreference = 'Stop'
if ($Serial -notmatch '^emulator-\d+$') { throw 'An emulator serial is required.' }
$package = 'com.example.workoutlevelup'
$output = Join-Path $PSScriptRoot 'native'
$remoteDump = '/data/local/tmp/workoutlevelup-window.xml'
$remoteImage = '/data/local/tmp/workoutlevelup-screen.png'
New-Item -ItemType Directory -Force $output | Out-Null

function Invoke-Adb {
    $result = & $Adb -s $Serial @args
    if ($LASTEXITCODE) { throw "ADB failed: $args" }
    $result
}
function Read-Screen {
    for ($attempt = 0; $attempt -lt 4; $attempt++) {
        Invoke-Adb shell rm -f $remoteDump
        $dump = Invoke-Adb shell uiautomator dump $remoteDump 2>&1
        if ($dump -match 'dumped to:') {
            return [xml]((Invoke-Adb shell cat $remoteDump) -join "`n")
        }
        Start-Sleep -Milliseconds 500
    }
    throw "Could not read Android UI hierarchy: $dump"
}
function Find-Node([xml]$Ui, [string]$Name) {
    $Ui.SelectNodes('//node') | Where-Object {
        $_.GetAttribute('text').Replace([char]0x2019, [char]0x27) -eq $Name -or
        $_.GetAttribute('content-desc') -eq $Name
    } | Select-Object -Last 1
}
function Require-Node([string]$Name) {
    for ($attempt = 0; $attempt -lt 4; $attempt++) {
        $node = Find-Node (Read-Screen) $Name
        if ($node) { return $node }
        Start-Sleep -Milliseconds 400
    }
    throw "Expected UI element: $Name"
}
function Tap([string]$Name) {
    $bounds = (Require-Node $Name).GetAttribute('bounds')
    if ($bounds -notmatch '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') { throw "Invalid bounds: $bounds" }
    $x = ([int]$Matches[1] + [int]$Matches[3]) / 2
    $y = ([int]$Matches[2] + [int]$Matches[4]) / 2
    Invoke-Adb shell input tap ([int]$x) ([int]$y)
}
function Capture([string]$Name) {
    Start-Sleep -Milliseconds 400
    Invoke-Adb shell screencap -p $remoteImage
    Invoke-Adb pull $remoteImage (Join-Path $output "$Name.png") | Out-Null
    Write-Output "Captured $Name"
}

# Clears only this app on the named emulator. Use a test instance with synthetic data.
Invoke-Adb shell input keyevent KEYCODE_WAKEUP
Invoke-Adb shell wm dismiss-keyguard
Invoke-Adb shell pm clear $package
Invoke-Adb shell am start -n "$package/.MainActivity"
Start-Sleep -Seconds 3
$null = Require-Node "Don't allow"
Capture '01-notification-permission'
Tap "Don't allow"
$null = Require-Node 'Calendar'
Capture '02-calendar'
Tap 'Previous'
Capture '03-calendar-previous-month'
Tap 'Next'
Tap 'Badges'
$null = Require-Node 'Your Progress'
Capture '04-badges-locked'
Tap 'Today'
$null = Require-Node 'Sit-ups'
Capture '05-workout-checklist'
Tap 'Start timer'
Start-Sleep -Seconds 2
$null = Require-Node 'Pause timer'
Capture '06-timer-running'
Tap 'Pause timer'
foreach ($exercise in @('Sit-ups', 'Plank', 'Squats', 'Push-ups')) { Tap $exercise }
$null = Require-Node 'Mark Workout Complete!'
Capture '07-workout-ready'
Tap 'Mark Workout Complete!'
$null = Require-Node 'First Step'
Capture '08-badges-earned'
Tap 'Today'
$null = Require-Node 'Workout Completed!'
Capture '09-completed-workout'
Invoke-Adb shell am force-stop $package
Invoke-Adb shell am start -n "$package/.MainActivity"
$null = Require-Node '1 done'
Capture '10-calendar-after-restart'
# The calendar legend also contains "Today". Its matching node precedes the navigation item.
Tap 'Today'
$null = Require-Node 'Workout Completed!'
if (Find-Node (Read-Screen) 'Start timer') { throw 'Completed workouts must not expose a start control.' }
Write-Output 'PASS: native workout logging, timer, badge, history and persistence after restart.'
