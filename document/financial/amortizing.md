# Amotizing

*Thinking...*

> **Explaining amortization concepts**
>
> I can explain amortization in the context of loans and intangible assets. I'll focus on the typical payment formula, which calculates scheduled interest and principal. The loan formula is PMT = P r(1+r)^n / ((1+r)^n-1). I might include the balance formula and define the period rate, r, as well. An example could clarify it, plus I could touch on the straight-line formula for intangible assets. Keeping it concise is key!

## Amortization in finance

**Amortizing** means gradually reducing or allocating an amount over a specified period. It commonly refers to:

### 1. Amortizing a loan

An **amortizing loan** is repaid through scheduled payments that include:

- **Interest** on the outstanding balance
- **Principal repayment**, which reduces the balance

The standard fixed-payment formula is:

$$
\text{Payment}
=
P\frac{r(1+r)^n}{(1+r)^n-1}
$$

Where:

- \(P\) = original loan principal
- \(r\) = interest rate per payment period
- \(n\) = total number of payments

For a loan with annual interest rate \(i\) and monthly payments:

$$
r=\frac{i}{12}, \qquad n=12\times\text{number of years}
$$

For each payment:

$$
\text{Interest}_t=r\times\text{Beginning Balance}_t
$$

$$
\text{Principal}_t=\text{Payment}-\text{Interest}_t
$$

$$
\text{Ending Balance}_t
=
\text{Beginning Balance}_t-\text{Principal}_t
$$

Early payments generally contain more interest; later payments contain more principal.

### 2. Amortizing an intangible asset

In accounting, amortization allocates the cost of an intangible asset—such as a patent—over its useful life.

The typical straight-line formula is:

$$
\text{Annual Amortization}
=
\frac{\text{Asset Cost}-\text{Residual Value}}
{\text{Useful Life}}
$$

This is similar to **depreciation**, except depreciation is generally used for tangible assets, while amortization is generally used for intangible assets.