const { test, expect } = require('@playwright/test');

console.log('Playwright test file loaded');
// E2E test: submit a review through the UI and verify backend response

test('submit review and verify backend analysis', async ({ page, request }) => {
  await page.goto('/');

  // Intercept the POST request to /api/review and capture the response
  let reviewId = null;
  const [response] = await Promise.all([
    page.waitForResponse(resp =>
      resp.url().includes('/api/review') && resp.request().method() === 'POST'),
    // Fill out and submit the form
    (async () => {
      await page.getByTestId('product-name-input').fill('Test Product');
      await page.getByTestId('customer-name-input').fill('Playwright User');
      await page.getByTestId('comment-input').fill('This is a Playwright E2E test review.');
      await page.getByTestId('rating-select').selectOption('5');
      await page.getByTestId('submit-review-btn').click();
    })()
  ]);

  // Wait for success message
  await expect(page.getByTestId('form-message')).toHaveText(/Review submitted successfully!/i);

  // Extract the id from the response JSON
  const reviewResponse = await response.json();
  reviewId = reviewResponse.id;
  expect(reviewId).toBeTruthy();

  // Poll the backend for analysis result using the captured id
  // (replace port if needed, or use config)
  const analysisResponse = await request.get(`http://localhost:8081/api/messages/${reviewId}`);
  expect(analysisResponse.ok()).toBeTruthy();
  const analysisData = await analysisResponse.json();
  expect(analysisData.reviewAnalysis).toBeDefined();
  expect(analysisData).toHaveProperty('id');
  expect(analysisData.id).toBe(reviewId);
  expect(analysisData).toHaveProperty('productName');
});
