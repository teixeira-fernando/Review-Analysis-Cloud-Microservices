const { test, expect } = require('@playwright/test');

// E2E test: submit a review through the UI and verify backend response

test('submit review and verify backend analysis', async ({ page, request }) => {
  await page.goto('/');

  // Intercept the POST request to /api/review and capture the response
  let reviewId = null;
  const [createReviewResponse] = await Promise.all([
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

  // Verify the response status and content for the review submission
  expect(createReviewResponse.status()).toBe(200);
  const reviewResponse = await createReviewResponse.json();
  expect(reviewResponse).toBeDefined();
  expect(reviewResponse).toHaveProperty('id');

  // Extract the id from the response JSON
  reviewId = reviewResponse.id;


  // Get from S3 Bucket the created review using Playwright's expect with retry mechanism
  await expect(async () => {
    const S3response = await request.get(`http://localhost:8081/api/messages/${reviewId}`);
    expect(S3response.status()).toBe(200);
    const jsonResponse = await S3response.json();
    expect(jsonResponse).toBeDefined();
    expect(jsonResponse).toHaveProperty('id');
    expect(jsonResponse.id).toBe(reviewId);
    expect(jsonResponse).toHaveProperty('productName');
  }).toPass({
    timeout: 60000, // total time to wait (60s)
    intervals: [5000] // poll every 5s
  });

});
