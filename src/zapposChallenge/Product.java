package zapposChallenge;

import org.json.simple.*;

public class Product {
	//Following are the properties of a product returned from Zappos Search.
	private double price;
	private String id;
	private String name;
	private String styleId;
	
	public Product(JSONObject product) {
		//substring(1) on the price to remove the $
		price = Double.parseDouble(((String) product.get("price")).substring(1));
		id = (String)product.get("productId");
		name = (String)product.get("productName");
		styleId = (String)product.get("styleId");
	}
	
	/**
	 * Returns String representation of product
	 */
	public String toString() {
		return name + " (ID:" + id + ", StyleId:" + styleId + ") : $" + String.format("%.2f", price) ;
	}
	
	/**
	 * Price of a product, required for comparisons and calculations.
	 */
	public double getPrice() {
		return price;
	}
}
