package edu.gatech.simulation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class SimQueue {
    private static PriorityQueue<SimEvent> eventQueue;
    private Comparator<SimEvent> simComparator;
    final static Integer passengerFrequency = 3;
    public int totalPassengerDropped = 0;
    public int totalPassengerPicked = 0;

    public SimQueue() {
        simComparator = new SimEventComparator();
        eventQueue = new PriorityQueue<SimEvent>(100, simComparator);
    }

    public void triggerNextEvent(MartaSystem martaModel) {
    	Random randGenerator = new Random();
    	int newRank;
    	int timeOfEvent;
        if (eventQueue.size() > 0) {
            SimEvent activeEvent = eventQueue.poll();
            activeEvent.displayEvent();
            switch (activeEvent.getType()) {

                case "move_vehicle":
                    // identify the Vehicle that will move
                    int onboardCount = 0;
                	final String DELIMITER = ",";
                	String[] tokens;
                    Vehicle activeVehicle = martaModel.getVehicle(activeEvent.getID());
                    String vehicleType = activeVehicle.getVehicleType();
                    System.out.println(" the "+vehicleType+" being observed is: " + Integer.toString(activeVehicle.getID()));

                    // identify the current stop
                    MartaRoute activeRoute = martaModel.getRoute(activeVehicle.getRouteID());
                    System.out.println(" the "+vehicleType+" is driving on route: " + Integer.toString(activeRoute.getID()));

                    int activeLocation = activeVehicle.getLocation();
                    int activeStopID = activeRoute.getStopID(activeLocation);
                    Stop activeStop = martaModel.getStop(activeStopID);
                    System.out.println(" the "+vehicleType+" is currently at stop: " + Integer.toString(activeStop.getID()) + " - " + activeStop.getName());


                    //bus stop queue
                    Queue<Rider> busStopQueue = new LinkedList<>();

                    // Query DB and get riders waiting at current stop for this routeID.
                    // Will only get as many riders as max capacity of the bus.
                    busStopQueue = getRiderInfoFromDB(activeStop.getName(), activeVehicle.getRouteID(), activeVehicle.getCapacity());

                    // compare number between busStopqueue and busRider
                    // if busRider < busStop queue remove passengers from bus queue
                    // insert new number of passengers back to matchPassenToStop

                    // each arriving bus will have riders in cache

                    int currentPassengers = activeVehicle.getPassengerList().size();

                    int passengerDifferential = activeStop.exchangeRiders(activeEvent.getRank(), currentPassengers, activeVehicle.getCapacity(), busStopQueue.size());

                    ArrayList<Rider> ridersGettingOffbus = new ArrayList<Rider>();
                    ridersGettingOffbus = activeVehicle.riderDestinationStop(activeStop.getName());

                    if (ridersGettingOffbus.size() > 0)
                    {
                    	totalPassengerDropped += ridersGettingOffbus.size();
                    	for (Rider passenger : ridersGettingOffbus)
                    	{
		                	String riderNextRoute = "";
		                	riderNextRoute = passenger.getRouteMap();
		                	System.out.println("Dropping off rider : " + Integer.toString(passenger.getRiderId()) + ". Next route : " + riderNextRoute);
		                	// Check if this rider is at final stop.
		                	if (riderNextRoute.equals("") || riderNextRoute.equals(",")) {
		                		System.out.println("Dropping off rider : " + Integer.toString(passenger.getRiderId()));
		                		// Update DB that this rider has reached their final stop.
		                		uploadRiderInfo(activeEvent.getRank(), passenger.getRiderId(), activeVehicle.getRouteID(), "", activeStop.getName(), "", true, true);
		                	} else {
		                		// Parse the next routeID and stopID.
		                		System.out.println("riderNextRoute: " + riderNextRoute);
		                		tokens = riderNextRoute.split(DELIMITER);
		                		int routeID = Integer.parseInt(tokens[0]);
		                		int stopID = Integer.parseInt(tokens[1]);
		                		riderNextRoute = "";

		                		for (int index = 2; index < tokens.length; index++) {
		                			riderNextRoute += tokens[index] + ",";
		                		}
		                		// Update in DB this rider's new onBoardStop, destStop, routeID, and remaining stops.
		                		System.out.println("riderNextRoute after parsing: " + riderNextRoute);
		                		uploadRiderInfo(activeEvent.getRank(), passenger.getRiderId(), routeID, activeStop.getName(), martaModel.getStop(stopID).getName(), riderNextRoute, true, false);
		                	}

                    	}
                    }

                    System.out.println("Riders geting off bus ------" + ridersGettingOffbus.size()
                	+ " total dropped:----------------- " + totalPassengerDropped);

                    System.out.println("Passenger Diff is---" + passengerDifferential);

                    ArrayList<Rider> boardingPassenger = new ArrayList<Rider>();

                    //Ensure that the bus/train should be Running:
                    boolean canRunNow = martaModel.getSchedule().shouldVehicleBeRunning(getTimeFromRank(activeEvent.getRank())/100);
                    if (!canRunNow){
                        //evict passengers and create event for vehicle to begin moving again soon
                        int startTime = martaModel.getSchedule().getStartTime()*100;
                        int endTime = martaModel.getSchedule().getEndTime()*100;
                        Integer difference;
                        if (startTime > endTime) {
                            difference = ((startTime - endTime)/100)*60;
                        } else {
                            difference = ((2400 - (endTime - startTime))/100)*60;
                        }


                        eventQueue.add(new SimEvent(Integer.parseInt(difference.toString()) + activeEvent.getRank(), "move_vehicle", activeEvent.getID()));
                        for( Rider passenger : activeVehicle.getPassengerList()){
                            uploadRiderInfo(activeEvent.getRank(), passenger.getRiderId(), activeRoute.getID(), activeStop.getName(), passenger.getDestinationStop(), passenger.getRouteMap(), true, false);
                        }
                        activeVehicle.setPassengerList(new ArrayList<>());
                        activeVehicle.setPassengers(0);
                        break;
                    }

                    if (passengerDifferential <= busStopQueue.size())
                    {
                    	System.out.println("Bus Queue size b--- " + busStopQueue.size());

                    	for(Rider passenger : busStopQueue)
                    	{
                    		if(passengerDifferential == 0)
                    		{
                    			break;
                    		}
                    		else if (activeVehicle.getRouteID() == passenger.getRoute())
                    		{
                    			totalPassengerPicked++;
                    			passengerDifferential--;
                    			activeVehicle.addPassenger(passenger);
                    			boardingPassenger.add(passenger);
                    		}
                    	}
                    	System.out.println("total passenger picked ----------------------" + totalPassengerPicked);
		                for (Rider removePass : boardingPassenger)
	                    {
                    		// Update DB that this rider has boarded a vehicle.
		                	uploadRiderInfo(activeEvent.getRank(), removePass.getRiderId(), activeVehicle.getRouteID(), "", "", "", false, false);
	                    	busStopQueue.remove(removePass);
	                    }
                    }

                    System.out.println(" passengers pre-stop: " + Integer.toString(currentPassengers) + " post-stop: " + (currentPassengers - ridersGettingOffbus.size()));

                    /*for (int j = 0; j <= activeBus.getPassengerList().size() - 1; j++)
                    {
                    	System.out.println("passenger getting stuck at " + activeBus.getPassengerList().get(j).getDestinationStop());
                    }*/
                    //activeBus.adjustPassengers(passengerDifferential);

                    // determine next stop
                    int nextLocation = activeRoute.getNextLocation(activeLocation);
                    int nextStopID = activeRoute.getStopID(nextLocation);
                    Stop nextStop = martaModel.getStop(nextStopID);
                    System.out.println(" the Vehicle is heading to stop: " + Integer.toString(nextStopID) + " - " + nextStop.getName() + "\n");

                    Path pathInfo = activeRoute.getPath(activeLocation);
                    int travelTime = 1 + pathInfo.getTimeToTravel(activeVehicle.getSpeed());
                    System.out.println(activeVehicle.getVehicleType() + " travel time: " + Integer.toString(travelTime) + "\n");
                    activeVehicle.setLocation(nextLocation);


                    // generate next event for this vehicle
                    eventQueue.add(new SimEvent(activeEvent.getRank() + travelTime, "move_vehicle", activeEvent.getID()));
                    break;


                case "add_riders":
                	int numberOfRiders = activeEvent.getID();
                	int baseNumRiders = 0;
                	int varNumRiders = 0;
                	int newNumRiders = 0;

                	newRank = randGenerator.nextInt(30) + 45;
                	timeOfEvent = getTimeFromRank(activeEvent.getRank());

                	if (numberOfRiders != 0) {
                		martaModel.makeRiders(numberOfRiders);
                	}

                	// Between 00:00 to 05:00, no new riders.
                	// Between 05:00 to 08:00, and 11:00 to 16:00, and 19:00 to 00:00 - fewer riders.
                	// Between 08:00 to 11:00, and 16:00 to 19:00 - lots of riders.
                	if (((timeOfEvent >= 500) && (timeOfEvent < 800)) || ((timeOfEvent >= 1100) && (timeOfEvent < 1600)) ||
                			((timeOfEvent >= 1900) && (timeOfEvent < 2400))) {
                		baseNumRiders = 50;
                		varNumRiders = 100;
                	} else if (((timeOfEvent >= 800) && (timeOfEvent < 1100)) || ((timeOfEvent >= 1600) && (timeOfEvent < 1900))) {
                		baseNumRiders = 300;
                		varNumRiders = 500;
                	}

                	if (baseNumRiders != 0) {
                		newNumRiders = randGenerator.nextInt(varNumRiders + 1) + baseNumRiders;
                	}

                	eventQueue.add(new SimEvent(activeEvent.getRank() + newRank, "add_riders", newNumRiders));
                	break;

                case "adjust_traffic":
                	MartaRoute thisRoute = martaModel.getRoute(activeEvent.getID());

                	timeOfEvent = getTimeFromRank(activeEvent.getRank());

                	if (martaModel.checkIfBusRoute(thisRoute.getID()) == true) {
						        thisRoute.updateRoutePaths(timeOfEvent);
					        }
                	               	
                	// Generate next event randomly between 45-75 minutes.               	
                	newRank = randGenerator.nextInt(30) + 45;

                	System.out.println(" traffic update for route: " + Integer.toString(activeEvent.getID()) + " next at: " + Integer.toString(newRank) + "\n");
                	eventQueue.add(new SimEvent(activeEvent.getRank() + newRank, "adjust_traffic", activeEvent.getID()));
                	break;

                default:
                    System.out.println(" event not recognized");
                    break;
            }
        } else {
            System.out.println(" event queue empty");
        }
    }

    public void addNewEvent(Integer eventRank, String eventType, Integer eventID) {
        eventQueue.add(new SimEvent(eventRank, eventType, eventID));
    }

    public Integer getTimeFromRank(Integer rank) {
    	int hourOfTheDay = (rank / 60) % 24;
    	int minuteOfTheHour = (rank % 60);

    	return (hourOfTheDay * 100 + minuteOfTheHour);
    }

    private static void uploadRiderInfo(Integer eventRank, Integer riderID, Integer routeID, String currStop, String nextStop, String nextRoutes, Boolean drop, Boolean lastStop) {
    	ResultSet rs;

    	try {
    		// connect to the local database system
    		String url = "jdbc:postgresql://localhost:5432/martadb";
    		Properties props = new Properties();
    		props.setProperty("user", "postgres");
    		props.setProperty("password", "cs6310");
    		props.setProperty("ssl", "true");

			Connection conn = DriverManager.getConnection(url, props);
			Statement stmt = conn.createStatement();
			String query = new String();
			String riderNextRoute = "";

			if (drop == false) {
				query = "UPDATE riders SET pickupTime = " + Integer.toString(eventRank) + ", onVehicle = TRUE WHERE riderID = " + Integer.toString(riderID);
			} else {
				// rider being dropped.
				if (lastStop == true) {
					query = "UPDATE riders SET dropTime = " + Integer.toString(eventRank) + ", onVehicle = FALSE, finalDrop = TRUE WHERE riderID = " + Integer.toString(riderID);
				} else {
					query = "UPDATE riders SET onBoardStop = '" + currStop + "', destStop = '" + nextStop + "', nextRoutes = '" + nextRoutes + "', routeID = " + Integer.toString(routeID) + ", onVehicle = FALSE WHERE riderID = " + Integer.toString(riderID);
				}
			}

			// System.out.println(query);
			stmt.executeUpdate(query);
			conn.close();

    	} catch (Exception e) {
            System.err.println("Discovered exception: ");
            System.err.println(e.getMessage());
        }
    }

    private static Queue<Rider> getRiderInfoFromDB(String stopName, int routeID, int busCapacity) {
    	ResultSet rs;
    	Queue<Rider> riderQueue = new LinkedList<Rider>();
		int riderID;
		String destStop;
		Rider newRider;
		int numRiders = 0;
		String riderNextRoute;

    	try {
    		// connect to the local database system
        	System.out.println(" connecting to the database");
    		String url = "jdbc:postgresql://localhost:5432/martadb";
    		Properties props = new Properties();
    		props.setProperty("user", "postgres");
    		props.setProperty("password", "cs6310");
    		props.setProperty("ssl", "true");

			Connection conn = DriverManager.getConnection(url, props);
			Statement stmt = conn.createStatement();

			rs = stmt.executeQuery("SELECT * FROM riders WHERE onVehicle is FALSE AND finalDrop is FALSE AND onBoardStop = '" + stopName + "' AND routeID = " + Integer.toString(routeID));
			while (rs.next() ) {
				riderID = rs.getInt("riderID");
				destStop = rs.getString("destStop");
				riderNextRoute = rs.getString("nextRoutes");
				newRider = new Rider(riderID, stopName, destStop, routeID, riderNextRoute);
				riderQueue.add(newRider);
				numRiders++;
				if (numRiders == busCapacity) { break; }
			}

			conn.close();

    	} catch (Exception e) {
            System.err.println("Discovered exception: ");
            System.err.println(e.getMessage());
        }

    	return riderQueue;
    }
}
