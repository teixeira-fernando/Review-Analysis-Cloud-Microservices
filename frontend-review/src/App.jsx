import React, { useState } from 'react';
import './App.css';

function App() {
  const [form, setForm] = useState({
    productName: '',
    customerName: '',
    reviewContent: '',
    rating: 1,
  });
  const [message, setMessage] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      const response = await fetch('/api/review', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          productId: form.productName, // Backend expects UUID, but using name for demo
          customerName: form.customerName,
          reviewContent: form.reviewContent,
          rating: Number(form.rating),
        }),
      });
      if (response.ok) {
        setMessage('Review submitted successfully!');
        setForm({ productName: '', customerName: '', reviewContent: '', rating: 1 });
      } else {
        setMessage('Failed to submit review.');
      }
    } catch (err) {
      setMessage('Error submitting review.');
    }
  };

  return (
    <div className="container">
      <h2>Submit a Product Review</h2>
      <form onSubmit={handleSubmit} className="review-form">
        <label>
          Product Name:
          <input
            type="text"
            name="productName"
            value={form.productName}
            onChange={handleChange}
            required
          />
        </label>
        <label>
          Customer Name:
          <input
            type="text"
            name="customerName"
            value={form.customerName}
            onChange={handleChange}
            required
          />
        </label>
        <label>
          Comment:
          <textarea
            name="reviewContent"
            value={form.reviewContent}
            onChange={handleChange}
            required
          />
        </label>
        <label>
          Rating:
          <select name="rating" value={form.rating} onChange={handleChange} required>
            {[1,2,3,4,5].map((num) => (
              <option key={num} value={num}>{num}</option>
            ))}
          </select>
        </label>
        <button type="submit">Submit Review</button>
      </form>
      {message && <p className="message">{message}</p>}
    </div>
  );
}

export default App;
