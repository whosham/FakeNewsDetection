package com.example.fakenewsdetection;

public class AssessmentData {


    //  private int id;
    private String id,description,image_url,timestamp,trustworthiness,rating;

    public AssessmentData(String mid, String mdescription, String mimage_url,String mtrustworthiness,String mtimestamp, String mrating ) {
        this.id = mid;
        this.description = mdescription;
        this.image_url = mimage_url;
        this.timestamp=mtimestamp;
        this.trustworthiness=mtrustworthiness;
        this.rating=mrating;
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

    public String getTimestamp() {
        return timestamp;
    }

    public String getTrustworthiness() {
        return trustworthiness;
    }

    public String getRating() {
        return rating;
    }
}
