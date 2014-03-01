package zapposChallenge;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class Combination implements Comparable{
	private ArrayList<Product> productList;
	private double sumOfPrices;
	private double totalPrice;
	private double pricegap;
	private final double tollerance = Math.pow(10, -7);
	
	public Combination(ArrayList<Product> combinations, double total) {
		productList = combinations;
		sumOfPrices = 0;
		totalPrice = total;
		for(Product x:productList) sumOfPrices += x.getPrice(); 
		pricegap = Math.abs(total - sumOfPrices);
	}
	
	/**
	 * Have to implement as I am extending Comparable.
	 */
	@Override
	public int compareTo(Object o) {
		Combination other = (Combination) o;
		if(this.equals(other)) return 0;
		else if(this.pricegap < other.pricegap) return -1;
		else return 1;
	}
	
	/**
	 * Equality check
	 */
	public boolean equals(Combination other) {
		if(this.productList.size() != other.productList.size()) {
			return false;
		}
		if(this.totalPrice != other.totalPrice) {
			return false;
		}
		for(int i = 0; i < productList.size(); i++){
			if(Math.abs(this.productList.get(i).getPrice() - other.productList.get(i).getPrice()) > tollerance) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Return the combination in printable format.
	 */
	public String toString() {
		String toReturn = "Products with sum $" + sumOfPrices + "\n";
		for(int i = 0; i < productList.size(); i ++) {
			toReturn += (i+1) + ": " + productList.get(i).toString() + "\n";
		}
		return toReturn;
	}
	
}
