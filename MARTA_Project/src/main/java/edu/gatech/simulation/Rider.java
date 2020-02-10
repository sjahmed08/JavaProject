package edu.gatech.simulation;

public class Rider {
	private String onBoardinStop;
	private String destinationStop;
	private Integer route;
	
	private int[] intendedBus = new int[3];
	private int riderId;
	String  routeMap;
	
	public Rider(int riderNum, String onBoard, String dsntStop, Integer route1, String nextRoutes)
	{
		setRiderId(riderNum);
		setOnBoardinStop(onBoard);
		setDestinationStop(dsntStop);
		route = route1;
		routeMap = nextRoutes;
		//intendedBus = intndBus;
	}
	
	public int getRiderId() {
		return riderId;
	}

	public void setRiderId(int riderId) {
		this.riderId = riderId;
	}

	public Integer getRoute() {
		return route;
	}

	public void setRoute(Integer route) {
		this.route = route;
	}

	public String getDestinationStop() {
		return destinationStop;
	}

	public void setDestinationStop(String destinationStop) {
		this.destinationStop = destinationStop;
	}

	public String getOnBoardinStop() {
		return onBoardinStop;
	}

	public void setOnBoardinStop(String onBoardinStop) {
		this.onBoardinStop = onBoardinStop;
	}
	
	public String getRouteMap() {
		return routeMap;		
	}
}
