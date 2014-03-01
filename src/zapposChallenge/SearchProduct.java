package zapposChallenge;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class SearchProduct {

	private int itemCount;
	private double totalPrice;
	private String zapposKey;
	private double maxPrice;
	private ArrayList<Product> allProductList;
	private ArrayList<Combination> desiredProductList;
	private final double TOL = Math.pow(10, -7);


	public SearchProduct(int num, double total, String key) {
		itemCount = num;
		totalPrice = total;
		zapposKey = key;
		maxPrice = Integer.MAX_VALUE; 	//will set later
		allProductList = new ArrayList<Product>();
		desiredProductList = new ArrayList<Combination>();
	}

	/**
	 * Get the double value of JSON object
	 */
	private Double getDouble(Object item){
		return Double.parseDouble(((String) ((JSONObject) item).get("price")).substring(1));
	}

	/**
	 * Do a Zappos Search and return the result and returns the result
	 */
	public String GetResult(int page) throws IOException {
		String urlStr = "http://api.zappos.com/Search?key="
				+zapposKey
				+"&term=&limit=100&sort={\"price\":\"asc\"}";
		if(page !=0)
		{
			urlStr += "&page=" + page;
		}
		URL url = new URL(urlStr);
		HttpURLConnection conn =
				(HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();

		conn.disconnect();
		return sb.toString();
	}

	/**
	 * Parse the Search result and return JSON Array
	 */
	public static JSONArray resultParse(String reply) throws ParseException{
		JSONParser parser = new JSONParser();
		JSONObject obj = (JSONObject) parser.parse(reply);
		JSONArray resultArray = (JSONArray)obj.get("results");
		return resultArray;
	}

	/**
	 * Give the users search criteria (item count and price range), find products in that range.
	 */
	@SuppressWarnings("unchecked")
	private void findProducts() throws IOException, ParseException {
		int page = 0;
		int priceRange = 2;
		//Get first 100 lowest priced products listed in Zappos.
		String reply = GetResult(page);
		JSONArray resultArray = resultParse(reply);

		//Lowest priced product, offcourse the first one.
		double lowestPrice = getDouble(resultArray.get(0));

		//If you can't match users criteria with the lowest priced item, return NULL.
		if( (lowestPrice * itemCount) < (totalPrice + priceRange) ) {			

			//We can proceed, find the max price given the min price.
			maxPrice = totalPrice - (itemCount - 1)*(lowestPrice);

			//We have first page, fetch the next page.
			page++;

			//Last product price.
			Double lastPrice = getDouble(resultArray.get(resultArray.size() - 1));

			//Keep fetching till we get products within our search range.
			while(lastPrice < maxPrice) 
			{
				String nextPage = GetResult(page);

				JSONArray nextArray = resultParse(nextPage);

				//append new page of results to original array
				resultArray.addAll(nextArray);

				//get new last product and price
				lastPrice = getDouble(nextArray.get(nextArray.size() - 1));

				page++;
			}

			allProductList.add(new Product((JSONObject)resultArray.get(0)));

			//count how many times a price has already shown up
			int already = 1;
			int numPrices = 1;
			//go through the whole 
			for(int i = 1; i < resultArray.size() && getDouble(resultArray.get(i)) < maxPrice; i++) {
				System.out.println("I : "+i);
				double currentPrice = getDouble(resultArray.get(i));
				if( currentPrice > allProductList.get(numPrices-1).getPrice()) {
					allProductList.add(new Product((JSONObject)resultArray.get(i)));
					numPrices++;
					already = 1;
				} else if(Math.abs(currentPrice - allProductList.get(numPrices-1).getPrice()) < TOL && already < itemCount){
					allProductList.add(new Product((JSONObject)resultArray.get(i)));
					numPrices++;
					already++;
				} else {
					//Finding products with the same price as the last one added to the productObjects list
					while(i < resultArray.size() && Math.abs(currentPrice - allProductList.get(numPrices-1).getPrice()) < TOL) {
						i++;
						currentPrice = getDouble(resultArray.get(i));
					}
					i++;
					already = 0;
				}
			}
		}
	}


	/**
	 * Finds the product combinations of numItems items with $1 of the totalPrice
	 */
	private void findProductCombination(
			ArrayList<Product> productList, double target, ArrayList<Product> partial)
	{
		//Keeping +-2 dollar as the price search window.
		int priceBuffer = 2;

		//if partial size > numItems, you already have too many items, so stop
		if(partial.size() > itemCount) { return; }

		double sum = 0;
		for(Product x : partial) sum += x.getPrice();

		if(Math.abs(sum - target) < priceBuffer && partial.size() == itemCount && desiredProductList.size() < 25) {
			//if no price combos yet, just add it on
			if(desiredProductList.size() == 0) {	desiredProductList.add(new Combination(partial, totalPrice)); }
			//otherwise, check it against the most recent product combo to make sure you're not repeating
			//TODO: check all product combos
			else{
				Combination testerCombo = desiredProductList.get(desiredProductList.size() -1);
				Combination partialCombo = new Combination(partial, totalPrice);
				if(!partialCombo.equals(testerCombo)) {
					desiredProductList.add(partialCombo);
				}
			}
		}
		//if sum is at or within $1 of target, then stop - done!
		if(sum >= target + priceBuffer) {
			return;
		}

		//otherwise, recursively continue adding another product to combo and test it
		for(int i = 0; i < productList.size() && !(partial.size() == itemCount && sum < target); i++){
			ArrayList<Product> remaining = new ArrayList<Product>();
			Product n = productList.get(i);
			for(int j=i+1; j < productList.size(); j++) {remaining.add(productList.get(j)); }
			ArrayList<Product> partial_rec = new ArrayList<Product>(partial);
			partial_rec.add(n);
			findProductCombination(remaining, target, partial_rec);
		}
	}

	/**
	 * Returns the gift combinations that are closest to the total dollar amount
	 */
	@SuppressWarnings("unchecked")
	public String getGiftCombos() throws IOException, ParseException {
		//get products from API
		System.out.println("Searching Zappos...");
		this.findProducts();

		//find combinations that work
		this.findProductCombination(allProductList, totalPrice, new ArrayList<Product>());

		Collections.sort(desiredProductList);

		//see if you have any combos
		if(desiredProductList.size() != 0) {
			String toPrint = "\nDone!\n";
			for(Combination x:desiredProductList) {
				toPrint += x.toString() + "\n";
			}
			return toPrint;
		}
		else {
			return "No items matching your criteria. Try fewer items or higher price range.";
		}
	}


	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);
		System.out.println("Enter desired # of products : ");
		int itemcount=reader.nextInt();
		System.out.println("Enter desired $ amount : ");
		double price = reader.nextInt();
		System.out.println("Enter your Key to Zappos API: ");
		String zapposKey = reader.next();

		try {
			SearchProduct searcher = new SearchProduct(itemcount, price, zapposKey);
			System.out.println(searcher.getGiftCombos());

		} catch (ParseException e) {
			// occurs if parsed incorrectly
			System.err.println("Sorry, we couldn't parse the server's response.");
			e.printStackTrace();
		} catch (IOException e) {
			// occurs if response code wasn't 200
			System.err.println("Sorry, something went wrong with our search.");
			e.printStackTrace();
		}
	}
}
