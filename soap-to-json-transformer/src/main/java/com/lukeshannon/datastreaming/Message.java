package com.lukeshannon.datastreaming;

public class Message {
	
	private String name;
	private String capital;
	private int population;
	
	public Message() {
	
	}
	
	public Message(String name, String capital, int population) {
		this.name = name;
		this.capital = capital;
		this.population = population;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCapital() {
		return capital;
	}
	public void setCapital(String capital) {
		this.capital = capital;
	}
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
	}

	@Override
	public String toString() {
		return "Country [name=" + name + ", capital=" + capital + ", population=" + population + "]";
	}
	
}
