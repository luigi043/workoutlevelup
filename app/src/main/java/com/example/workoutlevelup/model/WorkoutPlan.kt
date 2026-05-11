package com.example.workoutlevelup.model

/** A single exercise item inside a workout. */
data class Exercise(
    val name: String,
    val reps: Int? = null,          // null when it's timed
    val durationSeconds: Int? = null, // null when it's reps-based
    val perSide: Boolean = false     // e.g. side plank (do each side)
)

/** One complete workout session (e.g. Workout 1 or Workout 2 of a level). */
data class Workout(
    val title: String,
    val subtitle: String,
    val exercises: List<Exercise>,
    val rounds: Int,
    val type: Int   // 1 = Strength, 2 = Cardio/Core
)

/** One training level with metadata and its two workouts. */
data class TrainingLevel(
    val level: Int,
    val rank: String,
    val subtitle: String,
    val durationWeeks: String,
    val goal: String,
    val workouts: List<Workout>,
    val extraNote: String = ""
)

/** Badge definition for the reward system. */
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val requiredDays: Int = 0,       // completions needed
    val requiredLevel: Int = 0,      // level unlock
    val requiredStreak: Int = 0      // streak needed
)

/** Static data object holding the full training plan and badge list. */
object WorkoutPlan {

    val levels: List<TrainingLevel> = listOf(

        // ─── LEVEL 1 – E-Rank Initiate ──────────────────────────────────────
        TrainingLevel(
            level = 1,
            rank = "E-Rank Initiate",
            subtitle = "⚔️ LEVEL 1",
            durationWeeks = "2–3 weeks",
            goal = "Build strength, endurance, and consistency.",
            workouts = listOf(
                Workout(
                    title = "Workout 1 – Full Body",
                    subtitle = "🏋️ Strength",
                    type = 1,
                    rounds = 3,
                    exercises = listOf(
                        Exercise("Sit-ups", reps = 15),
                        Exercise("Plank", durationSeconds = 20),
                        Exercise("Squats", reps = 15),
                        Exercise("Push-ups", reps = 10)
                    )
                ),
                Workout(
                    title = "Workout 2 – Cardio + Core",
                    subtitle = "🏃 Cardio",
                    type = 2,
                    rounds = 3,
                    exercises = listOf(
                        Exercise("Jumping Jacks", reps = 20),
                        Exercise("Mountain Climbers", reps = 15),
                        Exercise("Crunches", reps = 20),
                        Exercise("Side Plank", durationSeconds = 30, perSide = true)
                    )
                )
            ),
            extraNote = "Weekly: Mon/Thu → W1  |  Tue/Fri → W2  |  Sat → 30 min walk  |  Sun → Rest"
        ),

        // ─── LEVEL 2 – E-Rank Hunter ────────────────────────────────────────
        TrainingLevel(
            level = 2,
            rank = "E-Rank Hunter",
            subtitle = "⚔️ LEVEL 2",
            durationWeeks = "3–4 weeks",
            goal = "Improve strength and body control.",
            workouts = listOf(
                Workout(
                    title = "Workout 1 – Strength",
                    subtitle = "🏋️ Strength",
                    type = 1,
                    rounds = 4,
                    exercises = listOf(
                        Exercise("Squats", reps = 25),
                        Exercise("Push-ups", reps = 15),
                        Exercise("Sit-ups", reps = 20),
                        Exercise("Plank", durationSeconds = 40)
                    )
                ),
                Workout(
                    title = "Workout 2 – Cardio",
                    subtitle = "🏃 Cardio",
                    type = 2,
                    rounds = 4,
                    exercises = listOf(
                        Exercise("Jumping Jacks", reps = 25),
                        Exercise("Mountain Climbers", reps = 20),
                        Exercise("Burpees", reps = 10),
                        Exercise("Leg Raises", reps = 25)
                    )
                )
            ),
            extraNote = "Extra: Run or walk 3×/week for 20–30 min. No excuses – if it rains, do burpees at home."
        ),

        // ─── LEVEL 3 – B-Rank Hunter ────────────────────────────────────────
        TrainingLevel(
            level = 3,
            rank = "B-Rank Hunter",
            subtitle = "⚔️ LEVEL 3",
            durationWeeks = "4 weeks",
            goal = "Explosiveness, endurance, and mindset.",
            workouts = listOf(
                Workout(
                    title = "Workout 1 – Strength",
                    subtitle = "🏋️ Strength",
                    type = 1,
                    rounds = 5,
                    exercises = listOf(
                        Exercise("Squats", reps = 40),
                        Exercise("Push-ups", reps = 25),
                        Exercise("Sit-ups", reps = 30),
                        Exercise("Plank", durationSeconds = 60)
                    )
                ),
                Workout(
                    title = "Workout 2 – Explosive Cardio",
                    subtitle = "🏃 Cardio",
                    type = 2,
                    rounds = 4,
                    exercises = listOf(
                        Exercise("Jumping Jacks", reps = 30),
                        Exercise("Burpees", reps = 20),
                        Exercise("Mountain Climbers", reps = 30),
                        Exercise("Leg Raises", reps = 30)
                    )
                )
            ),
            extraNote = "💡 At this stage, you should start seeing visible results."
        ),

        // ─── LEVEL 4 – S-Rank Hunter (Jinwoo Mode) ─────────────────────────
        TrainingLevel(
            level = 4,
            rank = "S-Rank Hunter",
            subtitle = "⚔️ LEVEL 4 – Jinwoo Mode",
            durationWeeks = "Ongoing",
            goal = "Real strength, mental endurance, total discipline.",
            workouts = listOf(
                Workout(
                    title = "Solo Level Challenge",
                    subtitle = "⚡ Full Power",
                    type = 1,
                    rounds = 1,
                    exercises = listOf(
                        Exercise("Squats", reps = 100),
                        Exercise("Push-ups", reps = 100),
                        Exercise("Sit-ups", reps = 100),
                        Exercise("Run / Intense Walk", durationSeconds = 3600) // 60 min
                    )
                )
            ),
            extraNote = "Rules: Never skip 2 days in a row • +5 reps every week • Progress photos every 2 weeks • When you think you're done → do 5 more."
        )
    )

    /** Returns the workout for a specific day number using a schedule pattern. */
    fun getWorkoutForDay(dayNumber: Int, level: Int): Workout {
        val trainingLevel = levels.first { it.level == level }
        if (trainingLevel.workouts.size == 1) return trainingLevel.workouts[0]

        // Weekly schedule: Mon=W1, Tue=W2, Wed=Rest, Thu=W1, Fri=W2, Sat=Walk, Sun=Rest
        return when ((dayNumber - 1) % 7) {
            0, 3 -> trainingLevel.workouts[0]   // Mon, Thu → Workout 1
            1, 4 -> trainingLevel.workouts[1]   // Tue, Fri → Workout 2
            else -> trainingLevel.workouts[0]   // fallback
        }
    }

    val badges: List<Badge> = listOf(
        Badge("first_step",    "First Step",    "Complete your very first workout",    "👟", requiredDays = 1),
        Badge("week_warrior",  "Week Warrior",  "Complete 7 workouts",                 "🏅", requiredDays = 7),
        Badge("streak_3",      "On Fire",       "Maintain a 3-day streak",             "🔥", requiredStreak = 3),
        Badge("streak_7",      "Unstoppable",   "Maintain a 7-day streak",             "💪", requiredStreak = 7),
        Badge("streak_30",     "Iron Will",     "Maintain a 30-day streak",            "🦾", requiredStreak = 30),
        Badge("level_2",       "E-Rank Hunter", "Reach Level 2",                       "⚔️", requiredLevel = 2),
        Badge("level_3",       "B-Rank Hunter", "Reach Level 3",                       "🗡️", requiredLevel = 3),
        Badge("level_4",       "S-Rank Hunter", "Reach Level 4 – Jinwoo Mode",         "⚡", requiredLevel = 4),
        Badge("half_century",  "Half Century",  "Complete 50 workouts",                "🥈", requiredDays = 50),
        Badge("centurion",     "Centurion",     "Complete 100 workouts",               "🏆", requiredDays = 100),
        Badge("consistent_30", "Consistent",    "Work out 30 times in total",          "📅", requiredDays = 30)
    )
}
