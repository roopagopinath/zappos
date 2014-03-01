zapposChallenge

The application takes three inputs:
 - Number of items the user wishes to buy
 - Maximum amount the user is willing to spend
 - The key to access Zappos API(This can be hardcoded)

It then goes and fetches resultset matching the user criteria using Zappos Search API.
Prints the combination of products meeting user's criteria.

- SearchProduct.java
  The main class which uses Zappos API to fetch the results.

- Product.java
  The class used to describe each Zappos product

- Combination.java
  The utility class used to compare and calculate the results based on the user's criteria.

NOTE:
For lower amount the application really works fine. With higher amount, since too many results are returned the application sometimes may not work as intended.

Referrence:

http://stackoverflow.com/questions/4632322/finding-all-possible-combinations-of-numbers-to-reach-a-given-sum
