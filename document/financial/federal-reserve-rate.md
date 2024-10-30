## fed cut off rate by 100 bps

When someone says the **Federal Reserve (Fed) cuts the interest rate by 100 basis points (bps)**, this means that the Fed has reduced its target interest rate by **1 percentage point**.

Here’s a breakdown:

- **Basis points (bps)**: A basis point is equal to 1/100th of a percentage point. So, 100 basis points = 1%.
- **Cut by 100 bps**: A cut of 100 basis points means the interest rate is reduced by 1%. For example, if the Fed's target interest rate was 3%, and they cut it by 100 bps, the new rate would be 2%.

The **Federal Reserve’s interest rate** (often referred to as the **federal funds rate**) is the rate at which banks lend to each other overnight. Changes in this rate can influence borrowing costs for consumers and businesses, affecting everything from mortgage rates to credit card interest.

In summary, cutting the rate by 100 bps translates to a **1% decrease in the Fed's target interest rate**, which is a significant move aimed at influencing economic conditions, usually to stimulate the economy by making borrowing cheaper.

The interest rate targeted by the Federal Reserve (often referred to as the **federal funds rate**) typically represents an **annualized rate**. However, since the fed funds rate is an **overnight rate** — meaning it applies to overnight lending between banks — you can convert the annualized rate to its daily equivalent using an exponential formula.

### Conversion Formula

To convert an annualized interest rate **r** into an overnight rate, you can use the continuous compounding formula:

$$
\text{Overnight rate} = e^{\left(\frac{r}{365}\right)} - 1
$$

Where:
- \( $ r $ \) is the annualized interest rate (expressed as a decimal, e.g., 5% = 0.05).
- 365 assumes there are 365 days in a year (for simplicity; leap years would use 366).

### Explanation:
- \( $ e^{r / 365} $ \) represents the continuous compounding over a single day.
- Subtracting 1 at the end gives you the effective overnight rate for that day.

### Example:

Let’s say the Federal Reserve sets the **fed funds rate** at **5% per year** (0.05 as a decimal).

The overnight rate would be:

$$
\text{Overnight rate} = e^{\left(\frac{0.05}{365}\right)} - 1
$$

Calculating this:

$$
\text{Overnight rate} = e^{(0.00013699)} - 1 \approx 1.000137 - 1 = 0.000137
$$

So, the **effective overnight rate** would be approximately **0.0137%** per day.

### Simplification for Small Rates

For small interest rates, you can often use the approximation:

$$
\text{Overnight rate} \approx \frac{r}{365}
$$

For example, if the annual rate is 5%:

$$
\frac{0.05}{365} \approx 0.000137 \text{ (or 0.0137% per day)}
$$

This approximation is very close to the result from the exponential formula, and it's often used for practical purposes when dealing with small interest rates.

### Summary

- The **federal funds rate** is typically stated as an **annualized rate**.
- To convert it to a **daily overnight rate**, you can use the formula \( e^{\left(\frac{r}{365}\right)} - 1 \), where \( r \) is the annual rate in decimal form.
- For small rates, a simpler approximation is to divide the annualized rate by 365 to get the effective daily rate.