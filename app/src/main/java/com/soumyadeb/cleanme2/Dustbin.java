package com.soumyadeb.cleanme2;

/**
 * Created by Soumya Deb on 05-12-2017.
 */

public class Dustbin {
    private String id, latitude, longitude, city, locality, last_clean, status, municipality;

    public Dustbin(String id, String latitude, String longitude, String city, String locality, String last_clean, String status, String municipality) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.locality = locality;
        this.last_clean = last_clean;
        this.status = status;
        this.municipality = municipality;
    }


    public String getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getCity() {
        return city;
    }

    public String getLocality() {
        return locality;
    }

    public String getLast_clean() {
        return last_clean;
    }

    public String getStatus() {
        return status;
    }

    public String getMunicipality() {
        return municipality;
    }
}
