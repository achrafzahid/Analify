# Statistics & Analytics - Quick Reference Guide

## 📊 Key Metrics Explained

### Revenue & Sales Metrics

#### **Total Revenue**
```
Sum of all completed orders value (product sales + section sales)
```

#### **Average Order Value (AOV)**
```
Total Revenue ÷ Number of Orders
```

#### **Revenue Growth Rate**
```
((Current Period Revenue - Previous Period Revenue) / Previous Period Revenue) × 100
```

---

### Section & Bidding Metrics

#### **Active Sections**
```
Count of sections with status = "OPEN"
```

#### **Average Bids Per Section**
```
Total Bids ÷ Total Sections
```

#### **Bid Win Rate** (For Investors)
```
(Number of Winning Bids ÷ Total Bids Placed) × 100
```

#### **Average Bid Increase**
```
Average of (Current Price - Base Price) for all sections
```

#### **Market Trend**
- **HOT**: Active Sections > 10 AND Total Bids > 100
- **STABLE**: Normal activity levels
- **COOLING**: Active Sections < 3 OR Total Bids < 20

---

### Financial Health Indicators

#### **Profit Margin**
```
Profit Margin = (Gross Profit ÷ Total Revenue) × 100

Where:
  Gross Profit = Total Revenue - Stock Cost - Operational Costs
  Stock Cost = Stock Value × 0.7 (estimated)
  Operational Costs = Total Revenue × 0.15 (estimated)
```

**Rating:**
- **EXCELLENT**: > 30%
- **GOOD**: 20-30%
- **MODERATE**: 10-20%
- **CONCERNING**: < 10%

#### **ROI (Return on Investment)**
```
ROI = (Gross Profit ÷ Total Investment) × 100

Where:
  Total Investment = Stock Cost + Section Investments
```

**Rating:**
- **EXCELLENT**: ROI > 40% AND Profit Margin > 30%
- **GOOD**: ROI > 25% AND Profit Margin > 20%
- **MODERATE**: ROI > 10% AND Profit Margin > 10%
- **CONCERNING**: Below moderate thresholds

#### **Available Cash**
```
Available Cash = Gross Profit - Pending Payments

Where:
  Pending Payments = Total Revenue × 0.05 (estimated)
```

---

### Inventory Metrics

#### **Low Stock Count**
```
Count of products with quantity < threshold (default: 10 units)
```

#### **Stock Value**
```
Sum of (Product Price × Quantity) for all inventory items
```

#### **Stock Recommendations**

**Priority Levels:**
| Current Stock | Priority | Recommended Stock | Action |
|--------------|----------|-------------------|--------|
| < 5 units    | HIGH     | 50 units         | RESTOCK |
| < 10 units   | MEDIUM   | 40 units         | RESTOCK |
| < 20 units   | LOW      | 30 units         | MAINTAIN |

**Predicted Demand** (Next 30 days):
```
max(20, 50 - Current Stock)
```

---

### Prediction Algorithms

#### **Linear Regression Forecast**

```java
Given historical data points (x₁, y₁), (x₂, y₂), ..., (xₙ, yₙ):

1. Calculate slope (m):
   m = (n × Σ(xy) - Σx × Σy) / (n × Σ(x²) - (Σx)²)

2. Calculate intercept (b):
   b = (Σy - m × Σx) / n

3. Predict future value:
   y_future = m × x_future + b
```

**Applied to Revenue:**
- Historical data: Last 30-60 days of revenue
- Prediction: Next 30 days forecast
- Confidence: 0.70 - 0.90 based on data consistency

#### **Trend Analysis**

```
Growth Rate = (End Value - Start Value) / Start Value

Trend Classification:
- BULLISH: Growth > 10%
- BEARISH: Growth < -10%
- NEUTRAL: -10% ≤ Growth ≤ 10%
```

---

### Leaderboard Rankings

#### **Top Products** (By Revenue)
```sql
SELECT product_name, SUM((price - discount) × quantity)
FROM orders JOIN order_items JOIN products
GROUP BY product_id
ORDER BY total_revenue DESC
LIMIT 10
```

#### **Top Sections** (By Value)
```sql
SELECT section_name, current_price
FROM sections
ORDER BY current_price DESC
LIMIT 10
```

#### **Most Competitive Sections** (By Bid Count)
```sql
SELECT section_name, COUNT(bids)
FROM sections LEFT JOIN bids
GROUP BY section_id
ORDER BY bid_count DESC
```

#### **Top Bidders** (By Total Amount)
```sql
SELECT investor_name, SUM(bid_amount)
FROM bids JOIN investors
GROUP BY investor_id
ORDER BY total_amount DESC
```

---

### Category Analytics

#### **Category Revenue Distribution**
```
Percentage = (Category Revenue ÷ Total Revenue) × 100
```

#### **Category Performance Rating**

Based on combined score of:
1. Revenue (40%)
2. Growth Rate (30%)
3. Product Count (20%)
4. Bidding Activity (10%)

**Ratings:**
- **EXCELLENT**: Score > 80
- **GOOD**: Score 60-80
- **AVERAGE**: Score 40-60
- **POOR**: Score < 40

---

### Seasonality Factor

```
Based on current month:
- HIGH_SEASON: November, December, January (Holiday season)
- LOW_SEASON: June, July, August (Summer slowdown)
- NORMAL: All other months
```

---

### Insight Generation Rules

#### **Warning Insights**
- Low stock > 10 items → "Low Stock Alert"
- Profit margin < 15% → "Margin Pressure Warning"
- ROI < 10% → "Investment Performance Concern"

#### **Success Insights**
- Profit margin > 25% → "Excellent Profit Margins"
- Portfolio growth > 20% → "Strong Portfolio Performance"
- Win rate > 30% → "High Bidding Success Rate"

#### **Opportunity Insights**
- Active sections > 5 → "High Bidding Activity"
- Revenue > 100k → "Revenue Milestone"
- Market trend = HOT → "Competitive Market Opportunity"

#### **Info Insights**
- General performance updates
- Milestones reached
- System notifications

---

### Performance Ratings

#### **Investor Performance Rating**

```
Based on percentile in platform:
- TOP_10_PERCENT: Top 10% of investors by revenue
- ABOVE_AVERAGE: 11-40 percentile
- AVERAGE: 41-70 percentile
- BELOW_AVERAGE: Below 70th percentile
```

**Calculation:**
```java
Portfolio Score = 
  (Total Revenue × 0.4) +
  (Portfolio Growth × 0.3) +
  (Bid Win Rate × 0.2) +
  (Active Participation × 0.1)
```

---

### Time Series Compression

When displaying charts, data is compressed to max 20 points:

```java
if (dataPoints.size() > 20) {
  groupSize = ceil(dataPoints.size() / 20)
  
  For each group:
    - Take first date as representative
    - Sum all values in group
    - Create single compressed point
}
```

---

### Stock Cost Estimation

Since actual costs may not be tracked:

```
Estimated Stock Cost = Stock Value × 0.7
(Assumes 30% markup on average)
```

### Operational Cost Estimation

```
Estimated Operational Costs = Total Revenue × 0.15
(Assumes 15% of revenue for operations)
```

---

## 🎯 How to Interpret Dashboards

### For Investors

**Green Signals (Good):**
- ✅ Portfolio growth > 15%
- ✅ Bid win rate > 20%
- ✅ Performance rating: ABOVE_AVERAGE or better
- ✅ Low stock alerts < 5

**Red Signals (Action Needed):**
- ⚠️ Portfolio growth < 5%
- ⚠️ Bid win rate < 10%
- ⚠️ Low stock alerts > 15
- ⚠️ No active bids

### For Admin-G

**Green Signals (Healthy Platform):**
- ✅ Platform growth rate > 15%
- ✅ Active sections > 10
- ✅ System health: HEALTHY
- ✅ All categories performing

**Red Signals (Attention Required):**
- ⚠️ Critical low stock items > 20
- ⚠️ Platform growth < 5%
- ⚠️ System alerts present
- ⚠️ Low bidding activity

### For Store Admins

**Green Signals (Good Operations):**
- ✅ Profit margin > 20%
- ✅ Revenue growth > 10%
- ✅ Low stock items < 10
- ✅ Strong category performance

**Red Signals (Action Needed):**
- ⚠️ Profit margin < 10%
- ⚠️ Declining revenue trend
- ⚠️ High low-stock count
- ⚠️ Weak category performance

---

## 📈 Optimization Tips

### To Improve Profit Margins
1. Review pricing strategy
2. Negotiate better supplier costs
3. Reduce operational waste
4. Focus on high-margin products

### To Increase Revenue
1. Target high-performing categories
2. Participate in hot bidding sections
3. Stock popular products
4. Expand product portfolio

### To Improve Bid Win Rate
1. Bid earlier in section lifecycle
2. Research competitive sections
3. Set strategic bid amounts
4. Monitor section deadlines

---

**Last Updated:** January 2026  
**Version:** 1.0.0
