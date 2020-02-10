package edu.gatech.simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


public class MartaSystem {
    private HashMap<Integer, Stop> stops;
    private HashMap<Integer, MartaRoute> routes;
    private HashMap<Integer, Vehicle> vehicles;
    public HashMap<String, Queue<Rider>> matchPassenToStop = new HashMap<String, Queue<Rider>>();
	private Schedule schedule;

    public MartaSystem() {
        stops = new HashMap<Integer, Stop>();
        routes = new HashMap<Integer, MartaRoute>();
        vehicles = new HashMap<Integer, Vehicle>();
        schedule = new Schedule();
    }
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

    public Stop getStop(int stopID) {
        if (stops.containsKey(stopID)) { return stops.get(stopID); }
        return null;
    }

    public MartaRoute getRoute(int routeID) {
        if (routes.containsKey(routeID)) { return routes.get(routeID); }
        return null;
    }

    public Vehicle getVehicle(int vehicleID) {
        if (vehicles.containsKey(vehicleID)) { return vehicles.get(vehicleID); }
        return null;
    }

    public HashMap<Integer, Vehicle> getVehicles(){
        return vehicles;
    }

    public int makeStop(int uniqueID, String inputName, int inputRiders, double inputXCoord, double inputYCoord) {
        // int uniqueID = stops.size();
        stops.put(uniqueID, new Stop(uniqueID, inputName, inputRiders, inputXCoord, inputYCoord));
        return uniqueID;
    }

    public int makeRoute(int uniqueID, int inputNumber, String inputName) {
        // int uniqueID = routes.size();
        routes.put(uniqueID, new MartaRoute(uniqueID, inputNumber, inputName));
        return uniqueID;
    }

    public int makeVehicle(int uniqueID, int inputRoute, int inputLocation, int inputPassengers, int inputCapacity, int inputSpeed, boolean isTrain) {
        // int uniqueID = vehicles.size();
        Vehicle vehicle;
        if (isTrain) {
            vehicle = new Train(uniqueID, inputRoute, inputLocation, inputPassengers, inputCapacity, inputSpeed);
        }
        else {
            vehicle = new Bus(uniqueID, inputRoute, inputLocation, inputPassengers, inputCapacity, inputSpeed);
        }
        vehicles.put(uniqueID, vehicle);
        return uniqueID;
    }

    public boolean checkIfBusRoute(int routeID) {
    	boolean isBus = true;

    	for (int i : vehicles.keySet()) {
			Vehicle currVehicle = vehicles.get(i);
			if ((currVehicle.getRouteID() == routeID) && (currVehicle.getVehicleType() == "Train")) {
				isBus = false;
    			break;
			}
		}

		return isBus;
	}

    public boolean validateFinalStop(int onBoardStop, int destStop, ArrayList<Integer> routesChecked, ArrayList<Integer> routesToBeTaken, ArrayList<Integer> stopInRoutes) {
    	boolean valid = false;
    	int arrayIndex = 0;
    	
    	ArrayList<Integer> routesToBeChecked = new ArrayList<Integer>();    	
    	HashMap<Integer, Integer> stopList = new HashMap<Integer, Integer>();
    	
    	// Iterate through all routes.
    	for (int index : routes.keySet()) {
    		// Check if this route has onBoardStop.
    		if (routes.get(index).getStopsOnRoute().containsValue(onBoardStop)) {
    			// This route has both stops.
    			if (routes.get(index).getStopsOnRoute().containsValue(destStop)) {
    				
    				// System.out.println("Route index: " + Integer.toString(index) + " has onBoardStop: " + Integer.toString(onBoardStop) + " and destStop: " + Integer.toString(destStop));
    				valid = true;
    				routesToBeTaken.add(index);
    				stopInRoutes.add(destStop);
    				break;
    			} else {
    				// System.out.println("Route index: " + Integer.toString(index) + " has onBoardStop: " + Integer.toString(onBoardStop) + " but not destStop: " + Integer.toString(destStop));
        			// Add it to routesChecked
        			routesToBeChecked.add(index);    				
    			}
    		}
    	}
    	
    	// Route doesn't have start and final stop.
    	if (valid == false) {
    		// Iterate through routes to be checked. These are routes where onBoardStop is present, but destStop is not.
    		for (int index : routesToBeChecked) {
    			
    			// System.out.println("Checking Route index: " + Integer.toString(index));
    			// If this route is already checked, don't check again.
    			if (routesChecked.contains(index) == true) {
    				// System.out.println("Route index already checked: " + Integer.toString(index));
    				continue;
    			}
    			// This route is being checked. So add to list.
    			routesChecked.add(index);
    			// Iterate through all the stops in this route.
    			stopList = routes.get(index).getStopsOnRoute();
    			for (int stopIndex : stopList.keySet()) {
    				if (stopList.get(stopIndex) == onBoardStop) { 
    					continue; 
    				} else {
    					// Call validateFinalStop recursively based on different currStop (which we already know is reachable from original onBoardStop.)
    					// System.out.println("Route index: " + Integer.toString(index) + " checking stop: " + Integer.toString(stopList.get(stopIndex)) + " with destStop: " + Integer.toString(destStop));
    					arrayIndex = routesToBeTaken.size();
    					routesToBeTaken.add(index);
    					stopInRoutes.add(stopList.get(stopIndex));
    					valid = validateFinalStop(stopList.get(stopIndex), destStop, routesChecked, routesToBeTaken, stopInRoutes);    					
    				}
    				
    				if (valid == true) {
    					// System.out.println("Route index: " + Integer.toString(index) + " stop: " + Integer.toString(stopList.get(stopIndex)) + " connects with destStop: " + Integer.toString(destStop));
    					break;
    				} else {    					
    					routesToBeTaken.remove(arrayIndex);
    					stopInRoutes.remove(arrayIndex);    					
    				}
    			}
    			
    			if (valid == false) {
    				// System.out.println("Route index: " + Integer.toString(index) + " doesn't connect.");
    			} else {
    				break;
    			}
    		}
    	}
    	
    	return valid;
    }
    
    //ADD EXCEPTION IF NO STOP CREATED
    //MAP RIDER TO STOP ONCE RIDER CREATED
    //public void makeRider(int numOfRiders)
    public void makeRiders(int numOfRiders)
    {
    	Stop rndOnBrdStop = new Stop();
    	Integer rndOnBrdStopId;
    	Stop rndDstStop = new Stop();
    	Stop nextStop = new Stop();
    	Integer rndDstStopId;
    	MartaRoute rndPassRoute;
    	Random rand = new Random();
    	int[] intendBus = new int[3];  	
    	int riderNum = 100;    	
    	int rndIndex = 0;
    	int counter; 
    	boolean validCombo = false;
    	String nextRoutes = "";
    	
    	try {
    		// connect to the local database system
    		String url = "jdbc:postgresql://localhost:5432/martadb";
    		Properties props = new Properties();
    		props.setProperty("user", "postgres");
    		props.setProperty("password", "cs6310");
    		props.setProperty("ssl", "true");    
    		
    		Connection conn = DriverManager.getConnection(url, props);
    		Statement stmt = conn.createStatement();
    		ResultSet rs;
			// Create table if it doesn't exist.
    		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS riders (riderID integer PRIMARY KEY, onBoardStop character varying(125), destStop character varying(125), finalDestStop character varying(125), routeID integer, nextRoutes character varying(2048), onVehicle boolean, pickupTime integer, dropTime integer, finalDrop boolean)");
    		rs = stmt.executeQuery("SELECT COUNT(*) FROM riders");
			rs.next();
			// Find the number of riders already in table and initialize riderID for new allocations. 
			riderNum = rs.getInt("count") + 100;
			
			for (int i = 1; i <= numOfRiders; i++)
			{   		
				ArrayList<Integer> finalRoutesToBeTaken = new ArrayList<Integer>();
				ArrayList<Integer> finalStopInRoutes = new ArrayList<Integer>();
				
				nextRoutes = "";
    		
				do {
					// Loop until valid onBoard and dest stops are got.
					ArrayList<Integer> routesChecked = new ArrayList<Integer>();
                	ArrayList<Integer> routesToBeTaken = new ArrayList<Integer>();
                	ArrayList<Integer> stopInRoutes = new ArrayList<Integer>();
					
                	// Get a random onBoardStop.
                	counter = rand.nextInt(stops.size());				
                	for (int index : stops.keySet()) {
                		rndIndex = index;
                		if (counter == 0) { break; }
                		counter--;
                	}				
                	rndOnBrdStopId = stops.get(rndIndex).getID();    		   		
                	
                	// Loop until a random destStop is got which is not same as onBoardStop.
                	do
                	{    		
                		counter = rand.nextInt(stops.size());
                		for (int index : stops.keySet()) {
                			rndIndex = index;
                			if (counter == 0) { break; }
                			counter--;
                		}
                		rndDstStopId = stops.get(rndIndex).getID();	    		
                	}while (rndDstStopId == rndOnBrdStopId);
    		
                	// System.out.println("Start stop : " + Integer.toString(rndOnBrdStopId) + ", Dest stop : " + Integer.toString(rndDstStopId));
                	
                	// Validate and find routes to finally reach destStop.
                	validCombo = validateFinalStop(rndOnBrdStopId, rndDstStopId, routesChecked, routesToBeTaken, stopInRoutes);
                	
                	if (validCombo == true) {
                		finalRoutesToBeTaken = routesToBeTaken;
                		finalStopInRoutes = stopInRoutes;
                	}
				
                	// System.out.println("Start stop : " + Integer.toString(rndOnBrdStopId) + ", Dest stop : " + Integer.toString(rndDstStopId) + ". Valid : " + Boolean.toString(validCombo));
                } while (validCombo == false);				
				
				int nextStopID = finalStopInRoutes.get(0);
				
				// Get the stop info.
				for(int k : stops.keySet())
				{
					if(stops.get(k).getID() == rndOnBrdStopId)
					{
						rndOnBrdStop = stops.get(k);
					}
    			
					if (stops.get(k).getID() == rndDstStopId)
					{
						rndDstStop = stops.get(k);
					}
					
					if (stops.get(k).getID() == nextStopID)
					{
						nextStop = stops.get(k);
					}
				}
				
				for (int index = 1; index < finalRoutesToBeTaken.size(); index++) {
					nextRoutes += Integer.toString(finalRoutesToBeTaken.get(index)) + "," + Integer.toString(finalStopInRoutes.get(index)) + ",";
            	}

				// Update rider info in the DB.    		
				String values = "VALUES(" + Integer.toString(riderNum) + ", '" + rndOnBrdStop.getName() + "', '" + nextStop.getName() + "', '";
				values += rndDstStop.getName() + "', " + Integer.toString(finalRoutesToBeTaken.get(0)) + ", '" + nextRoutes + "', FALSE, FALSE)";
				String query = "INSERT INTO riders(riderId, onBoardStop, destStop, finalDestStop, routeID, nextRoutes, onVehicle, finalDrop) " + values;
			
				// System.out.println(query);
				stmt.executeUpdate(query);
    		
				riderNum++;
			}
    	} catch (Exception e) {
            System.err.println("Discovered exception: ");
            System.err.println(e.getMessage());
        }
    }


    public void appendStopToRoute(int routeID, int nextStopID, Path pathInfo) { routes.get(routeID).addNewStop(nextStopID, pathInfo); }

    public void updatePathInRoute(int routeID, int stopIndex, double traffic, int maxSpeed, int distance) {
        routes.get(routeID).updatePathForStop(stopIndex, traffic, maxSpeed, distance);
    }

    public HashMap<Integer, Stop> getStops() { return stops; }

    public HashMap<Integer, MartaRoute> getRoutes() { return routes; }

    public HashMap<Integer, Vehicle> getvehicles() { return vehicles; }

    public void displayModel() {
    	ArrayList<MiniPair> vehicleNodes, stopNodes;
    	MiniPairComparator compareEngine = new MiniPairComparator();

    	int[] colorScale = new int[] {9, 29, 69, 89, 101};
    	String[] colorName = new String[] {"#000077", "#0000FF", "#000000", "#770000", "#FF0000"};
    	Integer colorSelector, colorCount, colorTotal;

    	try{
            // create new file access path
            String path="./mts_digraph.dot";
            File file = new File(path);

            // create the file if it doesn't exist
            if (!file.exists()) { file.createNewFile();}

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("digraph G\n");
            bw.write("{\n");

            vehicleNodes = new ArrayList<MiniPair>();
            for (Vehicle b: vehicles.values()) { vehicleNodes.add(new MiniPair(b.getID(), b.getPassengers())); }
            Collections.sort(vehicleNodes, compareEngine);

            colorSelector = 0;
            colorCount = 0;
            colorTotal = vehicleNodes.size();
            for (MiniPair c: vehicleNodes) {
            	if (((int) (colorCount++ * 100.0 / colorTotal)) > colorScale[colorSelector]) { colorSelector++; }
            	bw.write("  vehicle" + c.getID() + " [ label=\"vehicle#" + c.getID() + " | " + c.getValue() + " riding\", color=\"" + colorName[colorSelector] + "\"];\n");
            }
            bw.newLine();

            stopNodes = new ArrayList<MiniPair>();
            for (Stop s: stops.values()) { stopNodes.add(new MiniPair(s.getID(), s.getWaiting())); }
            Collections.sort(stopNodes, compareEngine);

            colorSelector = 0;
            colorCount = 0;
            colorTotal = stopNodes.size();
            for (MiniPair t: stopNodes) {
            	if (((int) (colorCount++ * 100.0 / colorTotal)) > colorScale[colorSelector]) { colorSelector++; }
            	bw.write("  stop" + t.getID() + " [ label=\"stop#" + t.getID() + " | " + t.getValue() + " waiting\", color=\"" + colorName[colorSelector] + "\"];\n");
            }
            bw.newLine();

            for (Vehicle m: vehicles.values()) {
            	Integer prevStop = routes.get(m.getRouteID()).getStopID(m.getPastLocation());
            	Integer nextStop = routes.get(m.getRouteID()).getStopID(m.getLocation());
            	bw.write("  stop" + Integer.toString(prevStop) + " -> vehicle" + Integer.toString(m.getID()) + " [ label=\" dep\" ];\n");
            	bw.write("  vehicle" + Integer.toString(m.getID()) + " -> stop" + Integer.toString(nextStop) + " [ label=\" arr\" ];\n");
            }

            bw.write("}\n");
            bw.close();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    private static void uploadRiderInfo(Integer riderID, String onBoardStop, String destStop, Integer routeID) {
    	
    	try {
    		// connect to the local database system
    		String url = "jdbc:postgresql://localhost:5432/martadb";
    		Properties props = new Properties();
    		props.setProperty("user", "postgres");
    		props.setProperty("password", "cs6310");
    		props.setProperty("ssl", "true");    
    		
			Connection conn = DriverManager.getConnection(url, props);
			Statement stmt = conn.createStatement();
			
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS riders (riderID integer PRIMARY KEY, onBoardStop character varying(125), destStop character varying(125), routeID integer, vehicleID integer, pickupTime integer, dropTime integer)");
			
			String values = "VALUES(" + Integer.toString(riderID) + ", '" + onBoardStop + "', '" + destStop + "', " + Integer.toString(routeID) + ")";
			String query = "INSERT INTO riders(riderId, onBoardStop, destStop, routeID) " + values;
			
			// System.out.println(query);
			stmt.executeUpdate(query);
			conn.close();
			
    	} catch (Exception e) {
            System.err.println("Discovered exception: ");
            System.err.println(e.getMessage());
        }
    }  
}
