import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import App from './App';

describe('App', () => {
  test('renders the review form', () => {
    render(<App />);
    expect(screen.getByTestId('form-title')).toBeInTheDocument();
    expect(screen.getByTestId('product-name-input')).toBeInTheDocument();
    expect(screen.getByTestId('customer-name-input')).toBeInTheDocument();
    expect(screen.getByTestId('comment-input')).toBeInTheDocument();
    expect(screen.getByTestId('rating-select')).toBeInTheDocument();
    expect(screen.getByTestId('submit-review-btn')).toBeInTheDocument();
  });

  test('submits the form and shows success message', async () => {
    global.fetch = jest.fn(() => Promise.resolve({ ok: true }));
    render(<App />);
    fireEvent.change(screen.getByTestId('product-name-input'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByTestId('customer-name-input'), { target: { value: 'John Doe' } });
    fireEvent.change(screen.getByTestId('comment-input'), { target: { value: 'Great product!' } });
    fireEvent.change(screen.getByTestId('rating-select'), { target: { value: '5' } });
    fireEvent.click(screen.getByTestId('submit-review-btn'));
    expect(await screen.findByTestId('form-message')).toHaveTextContent('Review submitted successfully!');
    global.fetch.mockRestore();
  });
});
