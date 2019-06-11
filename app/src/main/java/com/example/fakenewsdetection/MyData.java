package com.example.fakenewsdetection;

public class MyData {

  //  private int id;
    private String id,description,image_url,timestamp;
    private double latitude,longitude,trustworthiness;


    public MyData(String mid, String mdescription, String mimage_url, double mlatitude, double mlongitude,String mtimestamp,double mtrustworthiness) {
        this.id = mid;
        this.description = mdescription;
        this.image_url = mimage_url;
        this.latitude= mlatitude;
        this.longitude= mlongitude;
        this.timestamp=mtimestamp;
        this.trustworthiness=mtrustworthiness;
    }


    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getTrustworthiness() {
        return trustworthiness;
    }
}
