package com.ucl.sohonavigation.helper;

import java.util.List;

public class Route {
	
	private String experimentCode;
	private String routeName;
	private int routeNumber;
	private String routeStart;
	private String routeStartImage;
	private List<Event> events;
	
	public Route(String experimentCode, String routeName, int routeNumber, String routeStart,
			String routeStartImage, List<Event> events) {
		this.experimentCode = experimentCode;
		this.routeName = routeName;
		this.routeNumber = routeNumber;
		this.routeStart = routeStart;
		this.routeStartImage = routeStartImage;
		this.events = events;
	}
	
	
	public String getExperimentCode() {
		return experimentCode;
	}
	
	public void setExperimentCode(String experimentCode) {
		this.experimentCode = experimentCode;
	}
	
	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}

	public int getRouteNumber() {
		return routeNumber;
	}

	public void setRouteNumber(int routeNumber) {
		this.routeNumber = routeNumber;
	}

	public String getRouteStart() {
		return routeStart;
	}

	public void setRouteStart(String routeStart) {
		this.routeStart = routeStart;
	}

	public String getRouteStartImage() {
		return routeStartImage;
	}

	public void setRouteStartImage(String routeStartImage) {
		this.routeStartImage = routeStartImage;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}


	@Override
	public String toString() {
		return routeNumber + ": " + routeName;
	}
	
	
	
}
