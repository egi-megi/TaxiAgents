package pl.edu.pw.elka.taxiAgents;

public class Position {
    int longitude;
    int latitude;

    public Position(int longitude, int latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public int getLatitude() {
        return latitude;
    }
}
