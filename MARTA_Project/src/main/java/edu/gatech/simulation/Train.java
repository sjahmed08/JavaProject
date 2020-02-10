package edu.gatech.simulation;

public class Train extends Vehicle {
  public Train() {
      super();
  }

  public Train(int uniqueValue) {
      super(uniqueValue);
  }

  public Train(int uniqueValue, int inputRoute, int inputLocation, int inputPassengers, int inputCapacity, int inputSpeed) {
      super(uniqueValue, inputRoute, inputLocation, inputPassengers, inputCapacity, inputSpeed);
 }

  public String getVehicleType() {
    return "Train";
  }
}
