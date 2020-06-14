package pl.edu.pw.elka.taxiAgents.messages;

import pl.edu.pw.elka.taxiAgents.Position;

import java.io.Serializable;

public class TaxiToCallCenter implements Serializable {

    Position positionTaxiHome;
    String kindOFCar;
    double workingTimeInThisDay;
    double todayEarnings;
    int timeFromLastClient;
    String driverStatus;

    double distanceToClient;
    double timeToPickUpClient;
    double priceForAllDistance;

    String queryID;

    public TaxiToCallCenter(Position positionTaxiHome, String kindOFCar, double workingTimeInThisDay, double todayEarnings, int timeFromLastClient, String driverStatus, double distanceToClient, double timeToPickUpClient, double priceForAllDistance, String queryID) {
        this.positionTaxiHome = positionTaxiHome;
        this.kindOFCar = kindOFCar;
        this.workingTimeInThisDay = workingTimeInThisDay;
        this.todayEarnings = todayEarnings;
        this.timeFromLastClient = timeFromLastClient;
        this.driverStatus = driverStatus;
        this.distanceToClient = distanceToClient;
        this.timeToPickUpClient = timeToPickUpClient;
        this.priceForAllDistance = priceForAllDistance;
        this.queryID = queryID;
    }

    public static TaxiToCallCenter accepts(Position positionTaxiHome, String kindOFCar, double workingTimeInThisDay, double todayEarnings, int timeFromLastClient, String driverStatus, double distanceToClient, double timeToPickUpClient, double priceForAllDistance, String queryID){
        return new TaxiToCallCenter(positionTaxiHome, kindOFCar, workingTimeInThisDay, todayEarnings, timeFromLastClient, driverStatus, distanceToClient, timeToPickUpClient, priceForAllDistance, queryID);
    }

    /*public static TaxiToCallCenter reject(String queryID){
        return  new TaxiToCallCenter(false,-1,-1,queryID);
    }*/

    public Position getPositionTaxiHome() {
        return positionTaxiHome;
    }

    public String getKindOFCar() {
        return kindOFCar;
    }

    public double getWorkingTimeInThisDay() {
        return workingTimeInThisDay;
    }

    public double getTodayEarnings() {
        return todayEarnings;
    }

    public int getTimeFromLastClient() {
        return timeFromLastClient;
    }

    public double getDistanceToClient() {
        return distanceToClient;
    }

    public double getTimeToPickUpClient() {
        return timeToPickUpClient;
    }

    public double getPriceForAllDistance() {
        return priceForAllDistance;
    }

    public String getQueryID() {
        return queryID;
    }

    public String getDriverStatus() {return driverStatus; }
}
