package com.example.fakenewsdetection;

public class MyData {

  //  private int id;
    private String description, image_url ;

    public MyData(String mdescription, String mimage_url) {
    //    this.id = id;
        this.description = mdescription;
        this.image_url = mimage_url;
    }


    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }

}
