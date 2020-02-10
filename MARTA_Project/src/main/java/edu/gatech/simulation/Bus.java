package edu.gatech.simulation;

public class Bus extends Vehicle {

  public Bus() {
      super();
  }

  public Bus(int uniqueValue) {
      super(uniqueValue);
  }

  public Bus(int uniqueValue, int inputRoute, int inputLocation, int inputPassengers, int inputCapacity, int inputSpeed) {
      super(uniqueValue, inputRoute, inputLocation, inputPassengers, inputCapacity, inputSpeed);
 }

  public String getVehicleType() {
    return "Bus";
  }
}

