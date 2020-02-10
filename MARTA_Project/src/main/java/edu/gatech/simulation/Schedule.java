package edu.gatech.simulation;

public class Schedule {
    Integer startTime;
    Integer endTime;
    final int standardStartTime = 5;
    final int standardEndTime = 23;

    public Schedule(){
        this.startTime = standardStartTime;
        this.endTime = standardEndTime;
    }

    public Schedule(int startTime, int endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean shouldVehicleBeRunning(Integer hourOfDay){
        return((hourOfDay < endTime) && (hourOfDay > startTime));
    }


    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }
}
