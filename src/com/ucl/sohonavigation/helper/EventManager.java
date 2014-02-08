package com.ucl.sohonavigation.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

public class EventManager {
	
	private final static String TAG = "EventManager";
	
	// json field names
	public static final String JSON_EXPERIMENT_CODE = "ExpermientCode";
	public static final String JSON_ROUTES = "routes";
	public static final String JSON_ROUTE_NAME = "routeName";
	public static final String JSON_ROUTE_NUMBER = "routeNumber";
	public static final String JSON_ROUTE_START = "routeStart";
	public static final String JSON_ROUTE_START_IMAGE = "routeStartImage";
	public static final String JSON_EVENTS = "events";
	public static final String JSON_EVENT_NAME = "eventName";
	public static final String JSON_EVENT_TYPE = "eventType";
	public static final String JSON_EVENT_NUMBER = "eventNumber";
	public static final String JSON_GOAL = "goal";
	public static final String JSON_GOAL_IMAGE = "goalImage";
	public static final String JSON_ASK_DIRECTIONS = "askDirections";
	public static final String JSON_LEFT = "left";
	public static final String JSON_STRAIGHT = "straight";
	public static final String JSON_RIGHT = "right";
	public static final String JSON_AZIMUTH = "azimuth";
	public static final String JSON_THINK_ALOUT_TIME = "thinkAloudTime";
	public static final String JSON_DIRECTIONS_ORIENTATION = "directionsOrientation";
	public static final String JSON_DIRECTIONS_STREET = "directionsStreet";
	
	// event types
	public static final String EVENT_ROUTE_START = "RouteStart";
	public static final String EVENT_CROSSING = "Crossing";
	public static final String EVENT_NEW_GOAL = "NewGoal";
	public static final String EVENT_ROUTE_COMPLETED = "RouteCompleted";
	
	
	public static final String DATA_GOAL_SEEN = "goalSeen";
	public static final String DATA_AZIMUTH = "azimuth";
	public static final String DATA_AZIMUTH_CLICKED = "timeClickedAzimuth";
	public static final String DATA_GET_DIRECTIONS_SEEN = "getDirectionTimeSeen";
	public static final String DATA_GET_DIRECTIONS = "getDirection";
	public static final String DATA_GET_DIRECTIONS_CLICKED = "getDirectionTimeClicked";
	public static final String DATA_THINKALOUD_SEEN = "thinkAloudTimeSeen";
	public static final String DATA_THINKALOUD_DURATION = "thinkAloudDuration";
	public static final String DATA_THINKALOUD_CLICKED = "thinkAloudTimeClicked";
	public static final String DATA_DIRECTIONS_SEEN = "directionsSeen";
	public static final String DATA_DIRECTIONS_CLICKED = "directionsClicked";
	
	// list of route object retrieved from json
	private List<Route> mRoutes;
	
	// current route in progress
	private Route mRoute;
	// current event in progress
	private Event mEvent;
	
	
	// json object for the entire experiment (one json file, multiple routes)
	private JSONObject mExperiment = null;
	// json array containing all routes of the experiment
	//private JSONArray mRoutes = null;
	
	// currently selected route
	private JSONObject mCurrentRoute = null;
	private JSONArray mCurrentRouteEvents = null;
	private JSONObject mCurrentEvent = null;
	
	public String experimentCode, routeName, routeStart, routeStartImage, eventType, eventName, goal, goalImage, directionsOrientation, directionsStreet;
	public int routeNumber, eventNumber;
	public boolean azimuth, left, straight, right, thinkAloud;
	
	public String participantCode = null;
	private JSONObject mSaveRoute;
	private JSONArray mSaveEvents;
	private String saveExperimentCode, saveRouteName;
	private int saveRouteNumber;
	public String saveGetDirections, saveThinkAloudDuration;
	public long saveGoalSeen, saveAzimuth, saveAzimuthClicked, saveGetDirectionTimeSeen, saveGetDirectionClicked, saveThinkaloudSeen, saveThinkAloutClicked, saveDirectionsSeen, saveDirectionsClicked;
	
	
	
	public EventManager(String fileName) {
		
		// load the json file
		JSONObject experiment = loadJsonFile(fileName);
		try {
			mRoutes = getRoutes(experiment);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*try {
			mCurrentRoute = mRoutes.getJSONObject(0);
			mCurrentRouteEvents = mCurrentRoute.getJSONArray(JSON_EVENTS);
			mCurrentEvent = mCurrentRouteEvents.getJSONObject(0);
			experimentCode = mExperiment.getString(JSON_EXPERIMENT_CODE);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		routeNumber = 1;
		eventNumber = 1;
		
		updateEventStats();*/
	}
	
	/**
	 * load Json File into JsonObject
	 * @param fileName
	 */
	public JSONObject loadJsonFile(String fileName) {
		File sdCard = Environment.getExternalStorageDirectory();
		File jsonFile = new File(sdCard, "SohoNavigation/routes/" + fileName + ".json");
		FileInputStream in;
		String experiment = null;
		try {
			in = new FileInputStream(jsonFile);
			FileChannel fc = in.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			experiment = Charset.defaultCharset().decode(bb).toString();
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			return new JSONObject(experiment);
			//mRoutes = mExperiment.getJSONArray(JSON_ROUTES);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	private List<Route> getRoutes(JSONObject o) throws JSONException {
		
		// get experiment code
		String experimentCode = o.getString(JSON_EXPERIMENT_CODE);
		
		// get routes from json
		JSONArray jRoutes = o.getJSONArray(JSON_ROUTES);
		List<Route> routes = new ArrayList<Route>();
		
		// iterate through json routes array
		for (int i = 0; i < jRoutes.length(); i++) {
			
			// get current route
			JSONObject jRoute = jRoutes.getJSONObject(i);
			
			// get current route details
			String routeName = jRoute.getString(JSON_ROUTE_NAME);
			int routeNumber = jRoute.getInt(JSON_ROUTE_NUMBER);
			String routeStart = jRoute.getString(JSON_ROUTE_START);
			String routeStartImage = jRoute.getString(JSON_ROUTE_START_IMAGE);
			
			// get route events
			JSONArray jEvents = jRoute.getJSONArray(JSON_EVENTS);
			List<Event> events = new ArrayList<Event>();
			
			// iterate through json events array
			for (int j = 0; j < jEvents.length(); j++) {
				
				// get current route
				JSONObject jEvent = jEvents.getJSONObject(j);
				
				// add it to the eventlist
				events.add(getEventFromJson(jEvent));
			}
			
			routes.add(new Route(experimentCode, routeName, routeNumber, routeStart, routeStartImage, events));
			
		}
		
		for (Route r : routes) {
			r = setGoalsInEvents(r);
		}
		
		return routes;
	}
	
	private Event getEventFromJson(JSONObject o) throws JSONException {
		String eventType = o.getString(JSON_EVENT_TYPE);
		String eventName = o.getString(JSON_EVENT_NAME);
		int eventNumber = o.getInt(JSON_EVENT_NUMBER);
		
		Event event = new Event(eventType, eventName, eventNumber);
		
		if (eventType.equals(EVENT_ROUTE_START) || eventType.equals(EVENT_NEW_GOAL)) {
			event.setGoal(o.getString(JSON_GOAL));
			event.setGoalImage(o.getString(JSON_GOAL_IMAGE));
			event.setAskDirections(o.getBoolean(JSON_ASK_DIRECTIONS));
			if (event.isAskDirections()) {
				event.setLeft(o.getBoolean(JSON_LEFT));
				event.setStraight(o.getBoolean(JSON_STRAIGHT));
				event.setRight(o.getBoolean(JSON_RIGHT));
			}
			event.setAzimuth(o.getBoolean(JSON_AZIMUTH));
			event.setThinkAloutTime(o.getBoolean(JSON_THINK_ALOUT_TIME));
		} else if (eventType.equals(EVENT_CROSSING)) {
			event.setAskDirections(o.getBoolean(JSON_ASK_DIRECTIONS));
			if (event.isAskDirections()) {
				event.setLeft(o.getBoolean(JSON_LEFT));
				event.setStraight(o.getBoolean(JSON_STRAIGHT));
				event.setRight(o.getBoolean(JSON_RIGHT));
			}
			event.setAzimuth(o.getBoolean(JSON_AZIMUTH));
			event.setThinkAloutTime(o.getBoolean(JSON_THINK_ALOUT_TIME));
			event.setDirectionsOrientation(o.getString(JSON_DIRECTIONS_ORIENTATION));
			event.setDirectionsStreet(o.getString(JSON_DIRECTIONS_STREET));
		}
		
		return event;
	}
	
	private Route setGoalsInEvents(Route route) {
		String goal = "";
		String goalImage = "";
		for (Event e : route.getEvents()) {
			if (e.getEventType().equals("RouteStart") || e.getEventType().equals("NewGoal")) {
				goal = e.getGoal();
				goalImage = e.getGoalImage();
			} else if (e.getEventType().equals("Crossing")) {
				e.setGoal(goal);
				e.setGoalImage(goalImage);
			}
		}
		return route;
	}
	
	public List<Route> getmRoutes() {
		return mRoutes;
	}

	public Route getmRoute() {
		return mRoute;
	}

	public void setmRoute(Route mRoute) {
		this.mRoute = mRoute;
	}

	public Event getmEvent() {
		return mEvent;
	}

	public void setmEvent(Event mEvent) {
		this.mEvent = mEvent;
	}
	
	public void nextEvent() {
		int currentEventId = mEvent.getEventNumber() - 1; // events are stored in list starting with 0, in json starting with 1
		if (currentEventId < mRoute.getEvents().size() - 1) { // not the last event in the route
			mEvent = mRoute.getEvents().get(currentEventId + 1);
		} else {  // last event in the route
			mRoute = null;
			mEvent = null;
		}
	}
	
	public void setRouteAndEvent(int routeNumber, int eventNumber) {
		mRoute = mRoutes.get(routeNumber - 1);
		mEvent = mRoute.getEvents().get(eventNumber - 1);
	}

	
	
	
	
	
	
	
	
	
	
	
	

	private void updateEventStats() {
	/*	
		if (mCurrentRoute == null) {
			experimentCode = null;
			routeName = null;
			routeStart = null;
			routeStartImage = null;
			eventType = null;
			eventName = null;
			goal = null;
			goalImage = null;
			directionsOrientation = null;
			directionsStreet = null;
			routeNumber = -1;
			eventNumber = -1;
			azimuth = false;
			left = false;
			straight = false;
			right = false;
			thinkAloud = false;
			return;
		}
		
		try {
			eventType = mCurrentEvent.getString(JSON_EVENT_TYPE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (eventType.equals("RouteStart")) {
			try {
				routeName = mCurrentRoute.getString(JSON_ROUTE_NAME);
				routeStart = mCurrentRoute.getString(JSON_ROUTE_START);
				routeStartImage = mCurrentRoute.getString(JSON_ROUTE_START_IMAGE);
				eventName = mCurrentEvent.getString(JSON_EVENT_NAME);
				goal = mCurrentEvent.getString(JSON_GOAL);
				goalImage = mCurrentEvent.getString(JSON_GOAL_IMAGE);
				azimuth = mCurrentEvent.getBoolean(JSON_AZIMUTH);
				thinkAloud = mCurrentEvent.getBoolean(JSON_THINK_ALOUT_TIME);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (eventType.equals("Crossing")) {
			try {
				eventName = mCurrentEvent.getString(JSON_EVENT_NAME);
				left = mCurrentEvent.getBoolean(JSON_LEFT);
				straight = mCurrentEvent.getBoolean(JSON_STRAIGHT);
				right = mCurrentEvent.getBoolean(JSON_RIGHT);
				azimuth = mCurrentEvent.getBoolean(JSON_AZIMUTH);
				thinkAloud = mCurrentEvent.getBoolean(JSON_THINK_ALOUT_TIME);
				directionsOrientation = mCurrentEvent.getString(JSON_DIRECTIONS_ORIENTATION);
				directionsStreet = mCurrentEvent.getString(JSON_DIRECTIONS_STREET);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (eventType.equals("NewGoal")) {
			try {
				eventName = mCurrentEvent.getString(JSON_EVENT_NAME);
				goal = mCurrentEvent.getString(JSON_GOAL);
				goalImage = mCurrentEvent.getString(JSON_GOAL_IMAGE);
				azimuth = mCurrentEvent.getBoolean(JSON_AZIMUTH);
				thinkAloud = mCurrentEvent.getBoolean(JSON_THINK_ALOUT_TIME);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (eventType.equals("RouteCompleted")) {
			try {
				eventName = mCurrentEvent.getString(JSON_EVENT_NAME);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
	
	/*public void nextEvent() {
		Log.i(TAG, "eventNumber: " + eventNumber + "; mCurrentRouteEvents.length: " + mCurrentRouteEvents.length());
		if (eventNumber < mCurrentRouteEvents.length()) {
			eventNumber += 1;
			try {
				mCurrentEvent = mCurrentRouteEvents.getJSONObject(eventNumber-1);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateEventStats();
			return;
		} else if (eventNumber == mCurrentRouteEvents.length()) {
			if (routeNumber < mRoutes.length()) {
				eventNumber = 1;
				routeNumber += 1;
				mCurrentRoute = loadRoute(routeNumber);
				try {
					mCurrentRouteEvents = mCurrentRoute.getJSONArray(JSON_EVENTS);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i(TAG, "routeNumber: " + routeNumber + " eventNumber: " + eventNumber);
				mCurrentEvent = loadEvent(routeNumber, eventNumber);
				updateEventStats();
				return;
			} else if (routeNumber == mRoutes.length()) {
				mCurrentRoute = null;
				mCurrentRouteEvents = null;
				mCurrentEvent = null;
				updateEventStats();
			}
		}
	}*/
	
	public JSONObject loadRoute(int routeNumber) {
		/*if (mRoutes != null && routeNumber <= mRoutes.length()) {
			try {
				return mRoutes.getJSONObject(routeNumber - 1);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		return null;
	}
	
	public JSONObject loadEvent(int routeNumber, int eventNumber) {
		/*JSONObject route = loadRoute(routeNumber);
		JSONArray events = null;
		try {
			events = route.getJSONArray(JSON_EVENTS);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (events != null && eventNumber <= events.length()) {
			try {
				
				return events.getJSONObject(eventNumber - 1);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		return null;
	}
	
	
	
	public void initiateSavedDataRoute() {
		/*saveExperimentCode = experimentCode;
		saveRouteName = routeName;
		saveRouteNumber = routeNumber;
		mSaveEvents = new JSONArray();*/
	}
	
	public void addLastEvent() {
		/*JSONObject json = new JSONObject();
		
		try {
			json.put(JSON_EVENT_TYPE, eventType);
			json.put(JSON_EVENT_NAME, eventName);
			json.put(JSON_EVENT_NUMBER, eventNumber);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (azimuth) {
			try {
				json.put(DATA_AZIMUTH, saveAzimuth);
				json.put(DATA_AZIMUTH_CLICKED, saveAzimuthClicked);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (thinkAloud) {
			try {
				json.put(DATA_THINKALOUD_SEEN, saveThinkaloudSeen);
				json.put(DATA_THINKALOUD_DURATION, saveThinkAloudDuration);
				json.put(DATA_THINKALOUD_CLICKED, saveThinkAloutClicked);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (eventType.equals("Crossing")) {
			try {
				json.put(DATA_GET_DIRECTIONS_SEEN, saveGetDirectionTimeSeen);
				json.put(DATA_GET_DIRECTIONS, saveGetDirections);
				json.put(DATA_GET_DIRECTIONS_CLICKED, saveGetDirectionClicked);
				json.put(DATA_DIRECTIONS_SEEN, saveDirectionsClicked);
				json.put(DATA_DIRECTIONS_CLICKED, saveDirectionsClicked);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				json.put(DATA_GOAL_SEEN, saveGoalSeen);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		mSaveEvents.put(json);*/
	}
	
	public void saveRouteData() {
		/*JSONObject jsonRoute = new JSONObject();
		
		try {
			jsonRoute.put(JSON_EXPERIMENT_CODE, saveExperimentCode);
			jsonRoute.put(JSON_ROUTE_NAME, saveRouteName);
			jsonRoute.put(JSON_ROUTE_NUMBER, saveRouteNumber);
			jsonRoute.put(JSON_EVENTS, mSaveEvents);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String filename = participantCode + "_" + saveExperimentCode + "_" + saveRouteName + "_" + saveRouteNumber + ".json";
		File file = new File(Environment.getExternalStorageDirectory(), "SohoNavigation/results/" + filename);
		FileOutputStream out;
		
		String jsonRouteString = jsonRoute.toString();
		byte[] data = jsonRouteString.getBytes();
		
		try {
			out = new FileOutputStream(file);
			out.write(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
