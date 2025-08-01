const { test, expect } = require('@playwright/test');

// E2E test: submit a review through the UI and verify backend response

test('submit review and verify backend analysis', async ({ page }) => {
  await page.goto('/');

  // Fill out the review form
  await page.getByTestId('product-name-input').fill('Test Product');
  await page.getByTestId('customer-name-input').fill('Playwright User');
  await page.getByTestId('comment-input').fill('This is a Playwright E2E test review.');
  await page.getByTestId('rating-select').selectOption('5');

  // Submit the form
  await page.getByTestId('submit-review-btn').click();

  // Wait for success message
  await expect(page.getByTestId('form-message')).toHaveText(/Review submitted successfully!/i);

  // Optionally, poll the backend for analysis result (example endpoint)
  // Replace with actual API endpoint and logic if needed
  const response = await page.request.get('http://localhost:8081/api/messages/{id}');
  expect(response.ok()).toBeTruthy();
  const data = await response.json();
  expect(data.reviewAnalysis).toBeDefined();
});
