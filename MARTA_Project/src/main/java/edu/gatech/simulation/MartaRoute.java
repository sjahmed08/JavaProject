package edu.gatech.simulation;

import java.util.HashMap;

public class MartaRoute {
    private Integer ID;
    private Integer routeNumber;
    private String routeName;
    private HashMap<Integer, Integer> stopsOnRoute;
    private HashMap<Integer, Path> pathsOnRoute;

    public MartaRoute() {
        this.ID = -1;
    }

    public MartaRoute(int uniqueValue) {
        this.ID = uniqueValue;
        this.routeNumber = -1;
        this.routeName = "";
        this.stopsOnRoute = new HashMap<Integer, Integer>();
        this.pathsOnRoute = new HashMap<Integer, Path>();
    }

    public MartaRoute(int uniqueValue, int inputNumber, String inputName) {
        this.ID = uniqueValue;
        this.routeNumber = inputNumber;
        this.routeName = inputName;
        this.stopsOnRoute = new HashMap<Integer, Integer>();
        this.pathsOnRoute = new HashMap<Integer, Path>();
   }

    public void setNumber(int inputNumber) { this.routeNumber = inputNumber; }

    public void setName(String inputName) { this.routeName = inputName; }

    public void addNewStop(int stopID, Path pathInfo) { 
    	this.stopsOnRoute.put(stopsOnRoute.size(), stopID);
    	this.pathsOnRoute.put(pathsOnRoute.size(), pathInfo);    	
    }
    
    public void updatePathForStop(int stopIndex, double traffic, int maxSpeed, int distance) {
    	if (this.pathsOnRoute.size() > stopIndex) {
    		Path pathInfo = this.pathsOnRoute.get(stopIndex);
    		pathInfo.setPathParameters(traffic, maxSpeed, distance);
    	} else {
    		System.out.println("invalid stopIndex in path");
    	}
    	
    }

    public Integer getID() { return this.ID; }

    public Integer getNumber() { return this.routeNumber; }

    public String getName() { return this.routeName; }

    public HashMap<Integer, Integer> getStopsOnRoute() { return this.stopsOnRoute; }

    public void displayEvent() {
        System.out.println(" Marta route: " + Integer.toString(this.ID));
    }

    public void takeTurn() {
        System.out.println("provide next stop on route along with the distance");
    }

    public Integer getNextLocation(int routeLocation) {
        int routeSize = this.stopsOnRoute.size();
        if (routeSize > 0) { return (routeLocation + 1) % routeSize; }
        return -1;
    }

    public Integer getStopID(int routeLocation) { return this.stopsOnRoute.get(routeLocation); }

    public Integer getLength() { return this.stopsOnRoute.size(); }

    public void updateRoutePaths(int time) {
    	for (int i = 0; i < pathsOnRoute.size(); i++) {
    		Path pathInfo = pathsOnRoute.get(i);
    		pathInfo.modifyTraffic(time);
    	}
    }
    
    public Path getPath(int routeLocation) { return this.pathsOnRoute.get(routeLocation); }
    
    public void displayInternalStatus() {
        System.out.print("> route - ID: " + Integer.toString(ID));
        System.out.print(" number: " + Integer.toString(routeNumber) + " name: " + routeName);
        System.out.print(" stops: [ ");
        for (int i = 0; i < stopsOnRoute.size(); i++) {
            System.out.print(Integer.toString(i) + ":" + Integer.toString(stopsOnRoute.get(i)) + " ");
        }
        System.out.println("]");
    }

    //Override the equals method to compare the object
    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            MartaRoute me = (MartaRoute) object;
            if (this.ID == me.getID()) {
                result = true;
            }
        }
        return result;
    }

}
