import { defineConfig, devices } from '@playwright/test'
export default defineConfig({
  testDir: './tests/e2e', timeout: 45000, expect: { timeout: 8000 }, fullyParallel: false, workers: 1,
  reporter: [['list'], ['json', { outputFile: process.env.PLAYWRIGHT_RESULT_FILE || 'output/playwright/modernization/results.json' }]],
  outputDir: 'output/playwright/modernization/test-results',
  use: { viewport: { width: 1440, height: 1000 }, trace: 'off', screenshot: 'only-on-failure' },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'], viewport: {width:1440,height:1000} } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'], viewport: {width:1440,height:1000} } },
    { name: 'webkit', use: { ...devices['Desktop Safari'], viewport: {width:1440,height:1000} } },
  ],
})
