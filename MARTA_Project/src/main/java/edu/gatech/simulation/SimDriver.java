package edu.gatech.simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class SimDriver {
    private static SimQueue simEngine;
    private static MartaSystem martaModel;
    private static Random randGenerator;

    public SimDriver() {
        simEngine = new SimQueue();
        martaModel = new MartaSystem();
        randGenerator = new Random();
    }

    public void runInterpreter() {
        try{
            runInterpreter(null);
        }
        //Supress the error as this would never happen (reading from STDIN)
        catch (FileNotFoundException fnfe){}
    }

    public void runInterpreter(File file) throws FileNotFoundException {
        final String DELIMITER = ",";
        Scanner takeCommand;
        if (file == null){
            takeCommand = new Scanner(System.in);
        } else {
            FileInputStream fs = new FileInputStream(file);
            takeCommand = new Scanner(fs);
        }
        String[] tokens;

        do {
            System.out.print("# main: ");
            String userCommandLine = takeCommand.nextLine();
            tokens = userCommandLine.split(DELIMITER);

            switch (tokens[0]) {
                case "add_event":
                    simEngine.addNewEvent(Integer.parseInt(tokens[1]), tokens[2], Integer.parseInt(tokens[3]));
                    System.out.print(" new event - rank: " + Integer.parseInt(tokens[1]));
                    System.out.println(" type: " + tokens[2] + " ID: " + Integer.parseInt(tokens[3]) + " created");
                    break;
                case "add_stop":
                    int stopID = martaModel.makeStop(Integer.parseInt(tokens[1]), tokens[2], Integer.parseInt(tokens[3]), Double.parseDouble(tokens[4]), Double.parseDouble(tokens[5]));
                    System.out.println(" new stop: " + Integer.toString(stopID) + " created");
                    break;
                case "add_route":
                    int routeID = martaModel.makeRoute(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), tokens[3]);
                    System.out.println(" new route: " + Integer.toString(routeID) + " created");
                    break;
                case "add_vehicle":
                    int vehicleID = martaModel.makeVehicle(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]), Boolean.parseBoolean(tokens[7]));
                    System.out.println(" new vehicle: " + Integer.toString(vehicleID) + " created");
                    break;
                case "extend_route":                	
                	Path pathInfo = new Path(Double.parseDouble(tokens[3]), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
                    martaModel.appendStopToRoute(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), pathInfo);
                    System.out.println(" stop: " + Integer.parseInt(tokens[2]) + " appended to route " + Integer.parseInt(tokens[1]));
                    break;
                case "update_path":
                	martaModel.updatePathInRoute(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Double.parseDouble(tokens[3]), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
                	break;
                case "upload_real_data":
                    uploadMARTAData();
                    break;
                case "step_once":
                    simEngine.triggerNextEvent(martaModel);
                    System.out.println(" queue activated for 1 event");
                    break;
                case "step_multi":
                    System.out.println(" queue activated for " + Integer.parseInt(tokens[1]) + " event(s)");
                    for (int i = 0; i < Integer.parseInt(tokens[1]); i++) {
                    	// display the number of events completed for a given frequency
                    	if (tokens.length >= 3) {
                    		if (i % Integer.parseInt(tokens[2]) == 0) { System.out.println("> " + Integer.toString(i) + " events completed"); }
                    	}
                    	
                    	// execute the next event
                    	simEngine.triggerNextEvent(martaModel);
                   	
                    	// pause after each event for a given number of seconds
                    	if (tokens.length >= 4) {
                    		try { Thread.sleep(Integer.parseInt(tokens[3]) * 1000); }
                    			catch (InterruptedException e) { e.printStackTrace(); }
                    	}
                    	// regenerate the model display (Graphviz dot file) for a given frequency
                    	if (tokens.length >= 5) {
                    		if (i % Integer.parseInt(tokens[4]) == 0) { martaModel.displayModel();}
                    	}
                    }
                    break;
                    
                case "system_report":
                    System.out.println(" system report - stops, vehicles and routes:");
                    for (Stop singleStop: martaModel.getStops().values()) { singleStop.displayInternalStatus(); }
                    for (Vehicle singleVehicle: martaModel.getVehicles().values()) { singleVehicle.displayInternalStatus(); }
                    for (MartaRoute singleRoute: martaModel.getRoutes().values()) { singleRoute.displayInternalStatus(); }
                    break;
                case "display_model":
                	martaModel.displayModel();
                	break;
                case "quit":
                    System.out.println(" stop the command loop");
                    break;
                    
                    //Syed edit:
                case "create_rider":
                	martaModel.makeRiders(Integer.parseInt(tokens[1]));
                	System.out.println("Creating " + tokens[1] + " riders");
                    break;
                    
                case "test":
                	analyseRiderInfo();
                	break;    
                	
                case "drop_rider_table":
                	dropRiderTable();
                	break;
                	
                case "test_stop_id":
                	ArrayList<Integer> routesChecked = new ArrayList<Integer>();
                	ArrayList<Integer> routesToBeTaken = new ArrayList<Integer>();
                	ArrayList<Integer> stopInRoutes = new ArrayList<Integer>();
                	boolean result = martaModel.validateFinalStop(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), routesChecked, routesToBeTaken, stopInRoutes);
                	int index = 0;
                	for (index = 0; index < routesToBeTaken.size(); index++) {                		
                		System.out.println("Route : " + Integer.toString(routesToBeTaken.get(index)) + " , stop : " + Integer.toString(stopInRoutes.get(index)));
                	}
                	break;
                case "set_schedule":
                    Schedule schedule = new Schedule(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                    martaModel.setSchedule(schedule);
                    break;
             
                default:
                    System.out.println(" command not recognized");
                    break;
            }

        } while (!tokens[0].equals("quit"));

        takeCommand.close();
    }

    private static void dropRiderTable() {    	
    	try {
    		// connect to the local database system
    		String url = "jdbc:postgresql://localhost:5432/martadb";
    		Properties props = new Properties();
    		props.setProperty("user", "postgres");
    		props.setProperty("password", "cs6310");
    		props.setProperty("ssl", "true");    
    		
			Connection conn = DriverManager.getConnection(url, props);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DROP TABLE riders");
			System.out.println("dropped table riders.");
			conn.close();
			
    	} catch (Exception e) {
            System.err.println("Discovered exception: ");
            System.err.println(e.getMessage());
        }
    }
    
    private static void analyseRiderInfo() {
    	ResultSet rs;
    	String stopName = "Star City";
    	
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
			int riderID;
			
			System.out.println("Riders still waiting :");
			rs = stmt.executeQuery("SELECT * FROM riders WHERE vehicleID IS NULL AND pickupTime IS NULL AND dropTime IS NULL AND onBoardStop = '" + stopName + "'");
			while (rs.next() ) {
				riderID = rs.getInt("riderID");
				System.out.println(Integer.toString(riderID));
			}
			
			System.out.println("Riders on bus :");
			rs = stmt.executeQuery("SELECT * FROM riders WHERE vehicleID IS NOT NULL AND pickupTime IS NOT NULL AND dropTime IS NULL");
			while (rs.next() ) {
				riderID = rs.getInt("riderID");
				System.out.println(Integer.toString(riderID));
			}
			
			System.out.println("Riders dropped off :");
			rs = stmt.executeQuery("SELECT * FROM riders WHERE vehicleID IS NOT NULL AND pickupTime IS NOT NULL AND dropTime IS NOT NULL");
			while (rs.next() ) {
				riderID = rs.getInt("riderID");
				System.out.println(Integer.toString(riderID));
			}		
			
			conn.close();
			
    	} catch (Exception e) {
            System.err.println("Discovered exception: ");
            System.err.println(e.getMessage());
        }
    }
    
    private static void uploadMARTAData() {
        ResultSet rs;
        int recordCounter;

        Integer stopID, routeID;
        String stopName, routeName;
        // String direction;
        Double latitude, longitude;

        // intermediate data structures needed for assembling the routes
        HashMap<Integer, ArrayList<Integer>> routeLists = new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer> targetList;
        ArrayList<Integer> circularRouteList = new ArrayList<Integer>();

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

			// create the stops
        	System.out.print(" extracting and adding the stops: ");
        	recordCounter = 0;
            rs = stmt.executeQuery("SELECT * FROM apcdata_stops");
            while (rs.next()) {
                stopID = rs.getInt("min_stop_id");
                stopName = rs.getString("stop_name");
                latitude = rs.getDouble("latitude");
                longitude = rs.getDouble("longitude");

                martaModel.makeStop(stopID,stopName,0,latitude,longitude);
                recordCounter++;
            }
            System.out.println(Integer.toString(recordCounter) + " added");

            // create the routes
        	System.out.print(" extracting and adding the routes: ");
        	recordCounter = 0;
            rs = stmt.executeQuery("SELECT * FROM apcdata_routes");
            while (rs.next()) {
                routeID = rs.getInt("route");
                routeName = rs.getString("route_name");

                martaModel.makeRoute(routeID, routeID, routeName);
                recordCounter++;

                // initialize the list of stops for the route as needed
                routeLists.putIfAbsent(routeID, new ArrayList<Integer>());
            }
            System.out.println(Integer.toString(recordCounter) + " added");

            // add the stops to all of the routes
        	System.out.print(" extracting and assigning stops to the routes: ");
        	recordCounter = 0;
            rs = stmt.executeQuery("SELECT * FROM apcdata_routelist_oneway");
            while (rs.next()) {
                routeID = rs.getInt("route");
                stopID = rs.getInt("min_stop_id");
                // direction = rs.getString("direction");

                targetList = routeLists.get(routeID);
                if (!targetList.contains(stopID)) {
                    martaModel.appendStopToRoute(routeID, stopID, new Path());
                    recordCounter++;
                    targetList.add(stopID);
                    // if (direction.equals("Clockwise")) { circularRouteList.add(routeID); }
                }
            }

            // add the reverse "route back home" stops for two-way routes
            for (Integer reverseRouteID : routeLists.keySet()) {
                if (!circularRouteList.contains(reverseRouteID)) {
                    targetList = routeLists.get(reverseRouteID);
                    for (int i = targetList.size() - 1; i > 0; i--) {
                        martaModel.appendStopToRoute(reverseRouteID, targetList.get(i), new Path());
                    }
                }
            }
            System.out.println(Integer.toString(recordCounter) + " assigned");

            // create the vehicles and related event(s)
        	System.out.print(" extracting and adding the vehicles and events: ");
        	recordCounter = 0;
            int vehicleID = 0;
            rs = stmt.executeQuery("SELECT * FROM apcdata_bus_distributions");
            while (rs.next()) {
                routeID = rs.getInt("route");
                int minVehicles = rs.getInt("min_buses");
                int avgVehicles  = rs.getInt("avg_buses");
                int maxVehicles = rs.getInt("max_buses");

                int routeLength = martaModel.getRoute(routeID).getLength();
                int suggestedVehicles = randomBiasedValue(minVehicles, avgVehicles, maxVehicles);
                int vehiclesOnRoute = Math.max(1, Math.min(routeLength / 2, suggestedVehicles));

                int startingPosition = 0;
                int skip = Math.max(1, routeLength / vehiclesOnRoute);
                for (int i = 0; i < vehiclesOnRoute; i++) {
                    martaModel.makeVehicle(vehicleID, routeID, startingPosition + i * skip, 0, 10, 1, false);
                    simEngine.addNewEvent(0,"move_vehicle", vehicleID++);
                    recordCounter++;
                }
            }
            System.out.println(Integer.toString(recordCounter) + " added");

            // create the Rider-passenger generator and associated event(s)
        	System.out.print(" extracting and adding the Rider frequency timeslots: ");
        	recordCounter = 0;
            rs = stmt.executeQuery("SELECT * FROM apcdata_rider_distributions");
            while (rs.next()) {
                stopID = rs.getInt("min_stop_id");
                int timeSlot = rs.getInt("time_slot");
                int minOns = rs.getInt("min_ons");
                int avgOns  = rs.getInt("avg_ons");
                int maxOns = rs.getInt("max_ons");
                int minOffs = rs.getInt("min_offs");
                int avgOffs = rs.getInt("avg_offs");
                int maxOffs = rs.getInt("max_offs");

                martaModel.getStop(stopID).addArrivalInfo(timeSlot, minOns, avgOns, maxOns, minOffs, avgOffs, maxOffs);
                recordCounter++;
            }
            System.out.println(Integer.toString(recordCounter) + " added");


        } catch (Exception e) {
            System.err.println("Discovered exception: ");
            System.err.println(e.getMessage());
        }
    }

    private static int randomBiasedValue(int lower, int middle, int upper) {
        int lowerRange = randGenerator.nextInt(middle - lower + 1) + lower;
        int upperRange = randGenerator.nextInt(upper - middle + 1) + middle;
        return (lowerRange + upperRange) /2;
    }

}
