import { readFileSync, writeFileSync } from 'node:fs';

const source = readFileSync(new URL('../app/src/main/java/com/example/workoutlevelup/model/WorkoutPlan.kt', import.meta.url), 'utf8');
const levels = [...source.matchAll(/TrainingLevel\(\s*level = (\d+),\s*rank = "([^"]+)",([\s\S]*?)extraNote =/g)]
  .map(([, level, rank, body]) => ({
    level: Number(level), rank,
    workouts: [...body.matchAll(/Workout\(\s*title = "([^"]+)",[\s\S]*?rounds = (\d+),\s*exercises = listOf\(([\s\S]*?)\n\s*\)/g)]
      .map(([, title, rounds, exercises]) => ({
        title, rounds: Number(rounds),
        exercises: [...exercises.matchAll(/Exercise\("([^"]+)", (reps|durationSeconds) = (\d+)(, perSide = true)?\)/g)]
          .map(([, name, measure, amount, perSide]) => ({
            name,
            amount: (measure === 'reps' ? amount + ' reps'
              : Number(amount) % 60 === 0 ? Number(amount) / 60 + ' min' : amount + ' seconds')
              + (perSide ? ' each side' : '')
          }))
      }))
  }));
const badges = [...source.matchAll(/Badge\("[^"]+",\s*"([^"]+)",\s*"([^"]+)",\s*"[^"]+",\s*required(Days|Streak|Level) = (\d+)\)/g)]
  .map(([, title, description, requirement, amount]) => ({
    title, description, days: 0, streak: 0, level: 0, [requirement.toLowerCase()]: Number(amount)
  }));
if (levels.length !== 4 || badges.length !== 11 || levels.some(level => !level.workouts.length ||
    level.workouts.some(workout => !workout.exercises.length))) throw new Error('Unexpected native workout plan format');
writeFileSync(new URL('./plan.js', import.meta.url),
  '// Generated from the Android workout plan. Run node demo/generate-plan.mjs.\n' +
  `export const levels = ${JSON.stringify(levels, null, 2)};\nexport const badges = ${JSON.stringify(badges, null, 2)};\n`);
