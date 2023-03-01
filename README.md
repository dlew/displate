# Displate Data

Queries [Displate](https://displate.com/) for stats about their [Limited Editions](https://displate.com/limited-edition). 

You can see the output nicely formatted [here](https://docs.google.com/spreadsheets/d/1VQklhZs6vdYVuYK_dliG8BGDXSijjtOfxXN5YIRfVq0/edit?usp=sharing).

Run by using `$ ./gradlew run`.

## Sales Data Explanation

While the code that crunches the sales data on the sheet is private (due to data privacy issues), I will explain how it all works here. 

### Sources

[**eBay Terapeak**](https://www.ebay.com/help/selling/selling-tools/terapeak-research?id=4853) - Data straight from eBay. Usually reliable but has been known to occasionally have issues (such as canceled transactions still sometimes showing up in the data).

**Self-reported private sales** - People report private sales to me on reddit or Discord. So far, I generally know the reputations of those who report so I trust them, but if that trust is broken I may remove this source.

### Weighted Average

Simple average prices can be misleading because the price of any given LE fluctuates over time. What might have been only worth $300 in 2020 may be worth $1000 years later. 

To counter this issue, there's a **weighted average** which gives more credence to recent sales (and less on distant sales).

The weight of a sale is `1 / 2^(# of months since sale)`. In other words:

- A sale made in the past month has a weight of 1.
- A sale one month ago has a weight of 1/2.
- A sale two months ago has a weight of 1/4.
- ...and so on.

The overall weighted average is `sum(weighted sale price) / sum(all weights)`.

As an example, suppose an LE was sold this month for $500, one month ago $250, and 2 months ago for $150. Their weighted prices are `$500 * 1 = $500`, `$250 * .5 = $125` and `$150 * .25 = $37.5`, respectively, for a total of `$662.50`. The sum of all weights is `1 + .5 + .25 = 1.75`. Thus the weighted average is `$662.50 / 1.75 = $378`, which is higher than the simple average of $300 it would get unweighted. 

### Bundles

Sometimes more than one LE is sold at once - say, two LEs for $600. How does the sales data account for this?

A naive solution would be to simply divide the price by number of LEs. $600 for two LEs means $300 each. But what if one of the two is historically more pricey than the other? There have been bundles before where one LE is worth ~$150 and the other is worth $1000. It would be misleading to say they were of equal value in the bundle.

The answer is, again, to do weighting. The weighting for each LE in a bundle is based on sales data when that LE was sold on its own. For example, if A sells for $100 and B sells for $400, then a $500 bundle of the two will apportion 20% of the sales price to A and 80% to B.

Coming up with the correct solo price for each LE is tricky. It's a weighted average (like above), but based on the date of the sale of the bundle. That means that sales of the LE in the distant past or far future affect the price, but less than sales that happened at the same time as the bundle.

For example, suppose a bundle was sold in June 2021. The weighted price for each LE will be highly influenced by sales around that date (May through July 2021). They will be less affected by other months in 2021, and much less affected by sales in 2020 or 2022 and beyond. 

As a side effect of this bundle math, it is possible that the historical high or low price for an LE is based on a bundle sale. That is why sometimes the high/low price for an LE can change even though that particular LE hasn't sold recently - it's because the other LEs in the bundle *did* sell recently and affected the bundle's weighting.