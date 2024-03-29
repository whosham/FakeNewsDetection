package com.example.fakenewsdetection;

public class MyData {

  //  private int id;
    private String id,description,image_url,timestamp,cityName,trustworthiness;
    private double latitude,longitude;


    public MyData(String mid, String mdescription, String mimage_url, double mlatitude, double mlongitude,String mcityName,String mtimestamp,String mtrustworthiness) {
        this.id = mid;
        this.description = mdescription;
        this.image_url = mimage_url;
        this.latitude= mlatitude;
        this.longitude= mlongitude;
        this.cityName=mcityName;
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

    public String getTrustworthiness() {
        return trustworthiness;
    }

    public String getCityName() {
        return cityName;
    }
}
