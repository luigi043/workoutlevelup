import { levels, badges } from './plan.js';

const screen = document.querySelector('#screen');
const key = 'workout-level-up-demo-v1';
let logs = {};
try { logs = JSON.parse(localStorage.getItem(key)) ?? {}; } catch { /* Start with an empty log. */ }
const today = new Date();
const dateKey = date => [date.getFullYear(), date.getMonth() + 1, date.getDate()]
  .map((part, index) => String(part).padStart(index ? 2 : 4, '0')).join('-');
const todayKey = dateKey(today);
const label = key => new Intl.DateTimeFormat('en', { dateStyle: 'long' }).format(new Date(key + 'T12:00:00'));
const monthLabel = new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' });
const thresholds = [0, 14, 30, 60];
const drafts = new Map();
let month = new Date(today.getFullYear(), today.getMonth(), 1);
let selected = todayKey;
let tab = 'calendar';

function stats() {
  const dates = Object.keys(logs).sort();
  let best = 0, run = 0, previous = null;
  for (const date of dates) {
    const timestamp = Date.parse(date);
    run = timestamp - previous === 86400000 && previous !== null ? run + 1 : 1;
    previous = timestamp;
    best = Math.max(best, run);
  }
  let cursor = Date.parse(todayKey);
  const contains = time => Boolean(logs[new Date(time).toISOString().slice(0, 10)]);
  if (!contains(cursor)) cursor -= 86400000;
  let streak = 0;
  while (contains(cursor)) { streak++; cursor -= 86400000; }
  return { count: dates.length, best, streak, level: thresholds.filter(n => dates.length >= n).length };
}

function calendar(progress) {
  const start = (month.getDay() + 6) % 7;
  const days = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
  const cells = Array.from({ length: days }, (_, i) => {
    const key = dateKey(new Date(month.getFullYear(), month.getMonth(), i + 1));
    return `<button data-date="${key}" aria-label="${label(key)}${logs[key] ? ', completed' : ''}"
      class="${logs[key] ? 'done' : ''}" ${key === todayKey ? 'aria-current="date"' : ''}
      ${key > todayKey ? 'disabled' : ''}>${i + 1}</button>`;
  }).join('');
  return `<h2>Calendar</h2><p class="stat">${progress.streak} day streak · ${progress.count} complete</p>
    <section class="card"><div class="row"><button data-month="-1" aria-label="Previous month">‹</button>
    <h3>${monthLabel.format(month)}</h3><button data-month="1" aria-label="Next month">›</button></div>
    <div class="month">${['Mo','Tu','We','Th','Fr','Sa','Su'].map(day => `<small>${day}</small>`).join('')}
    ${'<span></span>'.repeat(start)}${cells}</div></section>
    <p class="muted">Choose today or a past date to view or log a workout. Green dates are complete.</p>`;
}

function workout(progress) {
  const log = logs[selected];
  const level = levels[(log?.level ?? progress.level) - 1];
  const index = log?.day ?? progress.count;
  const plan = level.workouts[level.workouts.length > 1 && [1,4].includes(index % 7) ? 1 : 0];
  const heading = `<p class="muted">${label(selected)}</p><h2>${plan.title}</h2>`;
  if (log) return heading + `<section class="card"><h3>Workout completed</h3><p>Time: ${time(log.seconds)}</p>
    <p>${plan.exercises.map(exercise => exercise.name).join(' · ')}</p></section>`;
  const draft = drafts.get(selected) ?? { checked: new Set(), seconds: 0, started: 0 };
  drafts.set(selected, draft);
  return heading + `<div class="row card"><span id="timer" class="timer">${time(elapsed(draft))}</span>
    <button id="toggle-timer">${draft.started ? 'Pause timer' : 'Start timer'}</button></div>
    <p>Complete all exercises for ${plan.rounds} ${plan.rounds === 1 ? 'round' : 'rounds'}.</p>
    <section class="card">${plan.exercises.map((exercise, index) => `<label class="exercise">
    <input type="checkbox" data-exercise="${index}" ${draft.checked.has(index) ? 'checked' : ''}>
    <span>${exercise.name}<br><small>${exercise.amount}</small></span></label>`).join('')}</section>
    <button id="complete" class="primary" ${draft.checked.size === plan.exercises.length ? '' : 'disabled'}>Mark workout complete</button>
    <p id="save-status" role="status"></p>`;
}

function badgeScreen(progress) {
  const { count, best, streak, level } = progress;
  const next = thresholds[level];
  return `<h2>Your Progress</h2><div class="card">
    <p>${streak} current streak · ${best} best streak · ${count} workouts</p>
    <h3>Level ${level} · ${levels[level - 1].rank}</h3>
    ${next ? `<progress aria-label="Workouts toward the next level" value="${count}" max="${next}"></progress><small>${count} / ${next} workouts</small>` : '<small>Maximum level</small>'}
    </div><h3>Badges</h3>${badges.map(badge => {
      const earned = count >= badge.days && best >= badge.streak && level >= badge.level;
      return `<section class="card"><strong>${badge.title}</strong> <small>· ${earned ? 'Earned' : 'Locked'}</small>
        <p>${badge.description}</p></section>`;
    }).join('')}`;
}

const elapsed = draft => draft.seconds + (draft.started ? Math.floor((Date.now() - draft.started) / 1000) : 0);
const time = seconds => [Math.floor(seconds / 60), seconds % 60].map(n => String(n).padStart(2, '0')).join(':');

function render(focus = false) {
  const progress = stats();
  screen.innerHTML = ({ calendar, workout, badges: badgeScreen })[tab](progress);
  for (const item of document.querySelectorAll('[data-tab]')) {
    if (item.dataset.tab === tab) item.setAttribute('aria-current', 'page');
    else item.removeAttribute('aria-current');
  }
  if (focus) { window.scrollTo(0, 0); screen.focus(); }
}

document.addEventListener('click', event => {
  const button = event.target.closest('button');
  if (!button) return;
  if (button.dataset.tab) {
    tab = button.dataset.tab;
    if (tab === 'workout') selected = todayKey;
    render(true);
  } else if (button.dataset.month) {
    month = new Date(month.getFullYear(), month.getMonth() + Number(button.dataset.month), 1);
    const selector = `[data-month="${button.dataset.month}"]`;
    render();
    screen.querySelector(selector).focus();
  } else if (button.dataset.date) {
    selected = button.dataset.date;
    tab = 'workout';
    render(true);
  } else if (button.id === 'toggle-timer') {
    const draft = drafts.get(selected);
    if (draft.started) { draft.seconds = elapsed(draft); draft.started = 0; }
    else draft.started = Date.now();
    button.textContent = draft.started ? 'Pause timer' : 'Start timer';
  } else if (button.id === 'complete') {
    const draft = drafts.get(selected);
    const progress = stats();
    const updated = { ...logs, [selected]: { day: progress.count, level: progress.level, seconds: elapsed(draft) } };
    try {
      localStorage.setItem(key, JSON.stringify(updated));
      logs = updated;
      drafts.delete(selected);
      tab = 'badges';
      render(true);
    } catch { screen.querySelector('#save-status').textContent = 'Could not save your workout. Please try again.'; }
  }
});
screen.addEventListener('change', event => {
  const checkbox = event.target;
  if (!checkbox.matches('[data-exercise]')) return;
  const draft = drafts.get(selected);
  const index = Number(checkbox.dataset.exercise);
  if (checkbox.checked) draft.checked.add(index);
  else draft.checked.delete(index);
  screen.querySelector('#complete').disabled = [...screen.querySelectorAll('input')].some(input => !input.checked);
});
setInterval(() => {
  const timer = document.querySelector('#timer');
  if (timer) timer.textContent = time(elapsed(drafts.get(selected)));
}, 250);
render();
