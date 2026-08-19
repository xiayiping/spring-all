# GAAP

*Thinking...*

> **Clarifying precious metals accounting**
>
> I need to clarify a typo regarding the accounting treatment of precious metals under GAAP. This is important because current standards can be complex and high stakes. I think using authoritative sources like the FASB or PwC guide would be beneficial. However, the FASB codification often has paywalls, so I might look at SEC filings too. Overall, it seems I need a general answer on inventory treatment, possibly relating to ASC 330 or ASC 940 for broker-dealers and fair value. Let’s search the web for more information!
>
> **Exploring precious metal GAAP**
>
> I need to understand the phrase, "what is precious metal GAAP," especially in relation to amortization. Whether it's amortized depends on the purpose: is it for personal or company holdings, investments, inventory, or productive assets? Under U.S. GAAP, physical precious metals are generally considered inventory, but there are nuances to this. According to FASB's source, there are exceptions for broker-dealers and miners that are valued at fair value. Let's open this up and clarify further!
>
> **Clarifying precious metals accounting**
>
> I see we need a concise answer about precious metal GAAP, possibly touching on amortization. Generally, there’s no amortization for products like these. We need to address journal entries and the LCNRV formula. If precious metals are held as collectibles, they could be classified as PP&E, but bullion investments are typically considered inventory. I should also distinguish between the two and cite all internet claims since I’ve browsed them.

If you mean **“How are precious metals accounted for under U.S. GAAP?”**, the treatment depends on why the company holds them.

### Precious-metal bullion or raw material

Gold, silver, platinum, and similar physical metals are normally classified as **inventory under ASC 330**. They are initially recorded at cost, including directly attributable acquisition costs. ([storage.fasb.org](https://storage.fasb.org/FASAC-Website%20Meeting%20Materials%20202103.pdf))

$$
\text{Inventory cost}
=
\text{Purchase price}+\text{Direct acquisition costs}
$$

Inventory is generally reported at:

$$
\boxed{\text{Lower of Cost or Net Realizable Value}}
$$

where:

\[
\text{NRV}
=
\text{Expected selling price}
-\text{Completion and disposal costs}
\]

For example:

- Gold cost: **$100,000**
- Expected selling price: **$95,000**
- Selling costs: **$2,000**
- NRV: \(95,000-2,000=\$93,000\)

The inventory is written down to **$93,000**, creating a **$7,000 loss**.

```text
Dr. Inventory write-down expense    $7,000
    Cr. Inventory                    $7,000
```

Mining companies commonly use weighted-average cost and carry bullion, doré, and related inventory at the lower of average cost or NRV. ([sec.gov](https://www.sec.gov/Archives/edgar/data/314203/000110465926028705/R33.htm?utm_source=openai))

### Is precious metal amortized?

**Normally, no.** Physical precious metal is not an intangible asset, so it is not amortized. When sold or consumed, its carrying cost becomes cost of goods sold:

```text
Dr. Cost of goods sold
    Cr. Precious-metal inventory
```

Certain broker-dealers and investment companies may instead measure qualifying precious-metal holdings at **fair value**, with value changes recognized in earnings. This is specialized accounting and generally cannot be applied by an ordinary operating company. ([storage.fasb.org](https://storage.fasb.org/FASAC-Website%20Meeting%20Materials%20202103.pdf))


# what is GAAP and ASC

*Thinking...*

> **Verifying ASC topic details**
>
> I need to clarify the typo regarding ASC 300 — it likely refers to Topic 300. I should browse current standards and focus on specific niches, using only official primary sources. The FASB ASC generally includes Topics 105-999, and I'm thinking Topic 300 might be related to something general. So, let’s verify this by searching official FASB sources to get the facts straight. That's the best way to ensure accuracy!
>
> **Clarifying ASC topics**
>
> It seems like there might be confusion about "ASC 300," which likely doesn't exist. The asset area starts from ASC 305, so perhaps 300 is just a placeholder. I should mention ASC 330, which covers inventory, and clarify how the ASC numbering works. It’s important to communicate gently that there is no standalone ASC Topic 300, with relevant codes running from 305-399. I'll ensure my statements are backed by cited internet claims.

### What is GAAP?

**GAAP** means **Generally Accepted Accounting Principles**. It is the main framework used by U.S. companies to prepare financial statements.

GAAP provides rules for:

- Recognizing revenue and expenses
- Valuing assets and liabilities
- Recording inventory
- Presenting financial statements
- Making financial-statement disclosures

For nongovernmental U.S. entities, the **FASB Accounting Standards Codification (ASC)** is the official source of authoritative U.S. GAAP. ([fasb.org](https://fasb.org/standards?utm_source=openai))

### What is ASC 300?

There is **no individual accounting standard called “ASC 300.”** The ASC numbers from **305 through 399** are the general **asset area** of the Codification. Therefore, “ASC 300” or the “ASC 300 series” may be used informally to refer to asset-related accounting topics. ([asc.fasb.org](https://asc.fasb.org/layoutComponents/getPdf?fileName=FASB_About_the_Codification.pdf&isSitesBucket=true&utm_source=openai))

Examples include:

- **ASC 305:** Cash and cash equivalents
- **ASC 310:** Receivables
- **ASC 320:** Investments—debt securities
- **ASC 323:** Equity-method investments
- **ASC 326:** Credit losses
- **ASC 330:** Inventory
- **ASC 350:** Intangibles and goodwill
- **ASC 360:** Property, plant, and equipment

### ASC 330 and precious metals

You probably mean **ASC 330, Inventory**, based on your previous precious-metals question.

When a business holds physical gold, silver, or another precious metal for sale or for use in production, it may be accounted for as inventory under ASC 330. The basic measurement is generally:

$$
\text{Inventory carrying amount}
=
\min(\text{Cost},\text{Net realizable value})
$$

However, the exact GAAP treatment depends on the company’s activities. For example, a manufacturer, mining company, commodity broker-dealer, and investment company may account for precious metals differently.

### How ASC references work

An ASC citation follows this format:

$$
\text{Topic–Subtopic–Section–Paragraph}
$$

For example:

$$
\text{ASC 330-10-35-1}
$$

- **330:** Inventory topic
- **10:** Overall subtopic
- **35:** Subsequent measurement section
- **1:** Specific paragraph

The ASC is the organized rulebook; **GAAP is the overall body of accounting principles contained in that rulebook**. ([asc.fasb.org](https://asc.fasb.org/layoutComponents/getPdf?fileName=FASB_About_the_Codification.pdf&isSitesBucket=true&utm_source=openai))




