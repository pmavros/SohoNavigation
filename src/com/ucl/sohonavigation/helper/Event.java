package com.ucl.sohonavigation.helper;

public class Event {
	
	private String eventType;
	private String eventName;
	private int eventNumber;
	private String goal;
	private String goalImage;
	private boolean askDirections;
	private boolean left;
	private boolean straight;
	private boolean right;
	private boolean azimuth;
	private boolean thinkAloutTime;
	private String directionsOrientation;
	private String directionsStreet;
	
	
	public Event(String eventType, String eventName, int eventNumber) {
		this.eventType = eventType;
		this.eventName = eventName;
		this.eventNumber = eventNumber;
		this.goal = "";
		this.goalImage = "";
	}


	public String getEventType() {
		return eventType;
	}


	public void setEventType(String eventType) {
		this.eventType = eventType;
	}


	public String getEventName() {
		return eventName;
	}


	public void setEventName(String eventName) {
		this.eventName = eventName;
	}


	public int getEventNumber() {
		return eventNumber;
	}


	public void setEventNumber(int eventNumber) {
		this.eventNumber = eventNumber;
	}


	public String getGoal() {
		return goal;
	}


	public void setGoal(String goal) {
		this.goal = goal;
	}


	public String getGoalImage() {
		return goalImage;
	}


	public void setGoalImage(String goalImage) {
		this.goalImage = goalImage;
	}


	public boolean isAskDirections() {
		return askDirections;
	}


	public void setAskDirections(boolean askDirections) {
		this.askDirections = askDirections;
	}


	public boolean isLeft() {
		return left;
	}


	public void setLeft(boolean left) {
		this.left = left;
	}


	public boolean isStraight() {
		return straight;
	}


	public void setStraight(boolean straight) {
		this.straight = straight;
	}


	public boolean isRight() {
		return right;
	}


	public void setRight(boolean right) {
		this.right = right;
	}


	public boolean isAzimuth() {
		return azimuth;
	}


	public void setAzimuth(boolean azimuth) {
		this.azimuth = azimuth;
	}


	public boolean isThinkAloutTime() {
		return thinkAloutTime;
	}


	public void setThinkAloutTime(boolean thinkAloutTime) {
		this.thinkAloutTime = thinkAloutTime;
	}


	public String getDirectionsOrientation() {
		return directionsOrientation;
	}


	public void setDirectionsOrientation(String directionsOrientation) {
		this.directionsOrientation = directionsOrientation;
	}


	public String getDirectionsStreet() {
		return directionsStreet;
	}


	public void setDirectionsStreet(String directionsStreet) {
		this.directionsStreet = directionsStreet;
	}


	@Override
	public String toString() {
		return eventNumber + ": " + eventName;
	}
	
	
	
}
