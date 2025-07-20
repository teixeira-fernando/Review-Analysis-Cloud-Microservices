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
    <div className="container card">
      <h2 className="title">
        <span role="img" aria-label="star" style={{marginRight: '8px'}}>⭐</span>
        Submit a Product Review
      </h2>
      <form onSubmit={handleSubmit} className="review-form">
        <div className="form-group">
          <label htmlFor="productName">
            <span role="img" aria-label="box">📦</span> Product Name
          </label>
          <input
            type="text"
            id="productName"
            name="productName"
            value={form.productName}
            onChange={handleChange}
            required
            placeholder="e.g. Wireless Mouse"
          />
        </div>
        <div className="form-group">
          <label htmlFor="customerName">
            <span role="img" aria-label="user">👤</span> Customer Name
          </label>
          <input
            type="text"
            id="customerName"
            name="customerName"
            value={form.customerName}
            onChange={handleChange}
            required
            placeholder="e.g. John Doe"
          />
        </div>
        <div className="form-group">
          <label htmlFor="reviewContent">
            <span role="img" aria-label="comment">💬</span> Comment
          </label>
          <textarea
            id="reviewContent"
            name="reviewContent"
            value={form.reviewContent}
            onChange={handleChange}
            required
            placeholder="Write your review here..."
          />
        </div>
        <div className="form-group">
          <label htmlFor="rating">
            <span role="img" aria-label="rating">🌟</span> Rating
          </label>
          <div className="rating-select">
            <select id="rating" name="rating" value={form.rating} onChange={handleChange} required>
              {[1,2,3,4,5].map((num) => (
                <option key={num} value={num}>{num}</option>
              ))}
            </select>
          </div>
        </div>
        <button type="submit" className="submit-btn">
          <span role="img" aria-label="send">🚀</span> Submit Review
        </button>
      </form>
      {message && <p className="message">{message}</p>}
    </div>
  );
}

export default App;
