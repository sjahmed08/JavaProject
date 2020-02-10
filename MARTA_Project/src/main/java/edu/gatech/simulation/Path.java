package edu.gatech.simulation;

import java.util.Random;

public class Path {
	private Double traffic;
	private Integer maxSpeed;
	private Integer distance;
	private static Random randGenerator;
	
	public Path() {
		this.traffic = 1.0;
		this.maxSpeed = 5;
		this.distance = 20;
		randGenerator = new Random();
	}
	
	public Path(double traffic, int maxSpeed, int distance) {
		this.traffic = traffic;
		this.maxSpeed = maxSpeed;
		this.distance = distance;
		randGenerator = new Random();
	}
	
	// This is called when user wants to change parameters of a certain path.
	public void setPathParameters(double traffic, int maxSpeed, int distance) {
		
		System.out.println(" path info updated. traffic: " + Double.toString(traffic) + " maxSpeed: " + Integer.toString(maxSpeed) + " distance: " + Integer.toString(distance) + "\n");
		this.traffic = traffic;
		this.maxSpeed = maxSpeed;
		this.distance = distance;		
	}
	
	// This function is called to get time to travel to next stop on this path.
	// Parameters to be passed -> current time and max vehicle speed.
	public int getTimeToTravel(int vehicleSpeed) {		
		double speedLimit, speed;
		
		speedLimit = this.getSpeedLimit();
		
		// Actual speed is minimum of speed limit and max vehicle speed.
		speed = Math.min(speedLimit, (double)vehicleSpeed);
		
		int time;
		
		time = (int)(((double) this.distance * 60.0) / speed);
		
		return time;		
	}
	
	public void modifyTraffic(int time) {		
		double trafficGenerated;
		
		if (((time >= 800) && (time <= 1000)) || ((time >= 1600) && (time <=1800))) {
			// Peak traffic hours. 
			// 8:00 AM to 10:00 AM and 4:00 PM to 6:00 PM.
			// To generate 2.0 to 3.0
			trafficGenerated = (randGenerator.nextInt(10) / 10.0) + 2.0;
		} else if (((time > 1000) && (time <= 1100)) || ((time > 1800) && (time <= 1900))) {
			// Medium traffic hours.
			// 10:00 AM to 11:00 AM and 6:00 PM to 7:00 PM.			
			// To generate 1.5 to 2.0
			trafficGenerated = (randGenerator.nextInt(5) / 10.0) + 1.5;					
		} else {
			// Rest of the hours.
			// To generate 1.0 to 1.5
			trafficGenerated = (randGenerator.nextInt(5) / 10.0) + 1.0;					
		}	
		
		System.out.println(" traffic updated to: " + Double.toString(trafficGenerated) + "\n");
		
		this.traffic = trafficGenerated;
	}
	
	// This function returns max speed of the path taking into account the traffic.
	private double getSpeedLimit() {
		double speedLimit;
		
		speedLimit = ((double) this.maxSpeed) / this.traffic;
		
		return speedLimit;
	}
}
