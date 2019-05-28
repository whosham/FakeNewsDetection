package com.example.fakenewsdetection;

public class MyData {

  //  private int id;
    private String description, image_url ;

    public MyData(String mdescription, String mimage_url) {
    //    this.id = id;
        this.description = mdescription;
        this.image_url = mimage_url;
    }

//    public int getId() {
//        return id;
//    }

//    public void setId(int id) {
//        this.id = id;
//    }

    public String getDescription() {
        return description;
    }

//  //  public void setDescription(String description) {
//        this.description = description;
//    }

    public String getImage_url() {
        return image_url;
    }

//    public void setImage_url(String image_url) {
//        this.image_url = image_url;
//    }
}
