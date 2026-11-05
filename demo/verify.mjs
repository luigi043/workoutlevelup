import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { readFile, mkdir } from 'node:fs/promises';
import { resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const { chromium } = await import(process.env.PLAYWRIGHT_MODULE
  ? pathToFileURL(resolve(process.env.PLAYWRIGHT_MODULE)).href : 'playwright');
const output = fileURLToPath(new URL('../docs/verification/browser/', import.meta.url));
await mkdir(output, { recursive: true });
const files = { '/': ['index.html', 'text/html'], '/app.js': ['app.js', 'text/javascript'],
  '/plan.js': ['plan.js', 'text/javascript'], '/style.css': ['style.css', 'text/css'],
  '/gallery.html': ['gallery.html', 'text/html'] };
const server = createServer(async (request, response) => {
  const galleryImage = /^\/gallery\/\d{2}-[a-z-]+\.jpg$/.test(request.url);
  const file = files[request.url] ?? (galleryImage ? [request.url.slice(1), 'image/jpeg'] : null);
  if (!file) { response.writeHead(404).end(); return; }
  try {
    const data = await readFile(new URL(file[0], import.meta.url));
    response.writeHead(200, { 'Content-Type': file[1] }).end(data);
  } catch { response.writeHead(404).end(); }
});
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
const base = process.env.DEMO_URL || `http://127.0.0.1:${server.address().port}`;
let browser;
try {
  browser = await chromium.launch({ executablePath: process.env.BROWSER_EXECUTABLE });
  for (const [name, width, height] of [['phone', 390, 844], ['desktop', 1440, 1000], ['small-phone', 320, 640]]) {
    const page = await browser.newPage({ viewport: { width, height }, hasTouch: name !== 'desktop' });
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    const check = async locator => await locator.waitFor({ state: 'visible' });
    const capture = async screen => {
      assert.ok(await page.locator('.phone').evaluate(node => node.getBoundingClientRect().width) <= 430);
      assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
      if (name !== 'small-phone') await page.screenshot({ path: `${output}/${name}-${screen}.png`, fullPage: true });
    };
    await page.goto(base);
    await check(page.getByRole('heading', { name: 'Calendar', exact: true }));
    await capture('01-calendar');
    const month = await page.locator('h3').textContent();
    await page.getByRole('button', { name: 'Previous month' }).click();
    assert.notEqual(await page.locator('h3').textContent(), month);
    await page.getByRole('button', { name: 'Next month' }).click();
    await page.getByRole('button', { name: 'Today', exact: true }).click();
    const complete = page.getByRole('button', { name: 'Mark workout complete' });
    assert.ok(await complete.isDisabled());
    await capture('02-workout');
    await page.getByRole('button', { name: 'Start timer' }).click();
    await page.waitForFunction(() => document.querySelector('#timer').textContent !== '00:00');
    await page.getByRole('button', { name: 'Pause timer' }).click();
    for (const item of await page.getByRole('checkbox').all()) await item.check();
    await complete.click();
    await check(page.getByRole('heading', { name: 'Your Progress' }));
    await check(page.locator('section').filter({ hasText: 'First Step' }).getByText('Earned', { exact: false }));
    await capture('03-badges');
    await page.reload();
    await page.locator('.month button[aria-current=date]').click();
    await check(page.getByRole('heading', { name: 'Workout completed' }));
    assert.equal(await page.getByRole('button', { name: 'Start timer' }).count(), 0);
    await capture('04-completed');
    assert.deepEqual(errors, []);
    await page.close();
    console.log(`PASS ${name} ${width}×${height}: calendar, timer, checklist, completion, badge and reload.`);
  }
  const page = await browser.newPage();
  await page.goto(base);
  await page.getByRole('button', { name: 'Today', exact: true }).click();
  await page.getByRole('checkbox').first().check();
  await page.getByRole('button', { name: 'Calendar', exact: true }).click();
  let pastDay = page.locator('.month button:enabled:not([aria-current=date])').last();
  if (!(await pastDay.count())) {
    await page.getByRole('button', { name: 'Previous month' }).click();
    pastDay = page.locator('.month button:enabled').last();
  }
  await pastDay.click();
  for (const item of await page.getByRole('checkbox').all()) await item.check();
  await page.getByRole('button', { name: 'Mark workout complete' }).click();
  await page.getByRole('button', { name: 'Today', exact: true }).click();
  await page.getByRole('heading', { name: 'Workout 2 – Cardio + Core' }).waitFor();
  assert.equal(await page.locator('input:checked').count(), 0);
  await page.close();
  console.log('PASS dated drafts reset when an earlier completion changes the assigned workout.');
  const gallery = await browser.newPage({ viewport: { width: 390, height: 844 }, hasTouch: true });
  await gallery.goto(base);
  await gallery.getByRole('link', { name: 'Android screen gallery' }).tap();
  await gallery.getByRole('heading', { name: 'Screen gallery' }).waitFor();
  await gallery.waitForFunction(() => Array.from(document.querySelectorAll('.gallery img')).every(image => image.complete));
  const images = await gallery.locator('.gallery img').evaluateAll(elements =>
    elements.map(image => ({ loaded: image.complete && image.naturalWidth > 0, alt: image.alt })));
  assert.equal(images.length, 10);
  assert.ok(images.every(image => image.loaded && image.alt));
  assert.ok(await gallery.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
  await gallery.close();
  console.log('PASS public Android screen gallery loads all ten captures at phone width.');
} finally {
  await browser?.close();
  server.close();
}
