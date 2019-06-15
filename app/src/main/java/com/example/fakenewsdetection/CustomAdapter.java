package com.example.fakenewsdetection;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private Context mcontext;
   // private List<MyData> my_data;
    private ArrayList<MyData> mmyData;

    private onItemClickListener mListener;

    public interface onItemClickListener {

        void onItemClick(int position ) ;

    }

    public void setOnItemClickListener(onItemClickListener listener){
        mListener=listener ;
    }


   //  public CustomAdapter(Context context, List<MyData> my_data) {
   public CustomAdapter(Context context, ArrayList<MyData> myDataList) {
        this.mcontext = context;
        this.mmyData= myDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items,viewGroup,false) ;
        View v = LayoutInflater.from(mcontext).inflate(R.layout.items, viewGroup, false);

        return new ViewHolder(v) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        MyData currentItem = mmyData.get(i) ;
        String imageUrl = currentItem.getImage_url();
        String description= currentItem.getDescription();
        String id= currentItem.getId();
        String timestamp = currentItem.getTimestamp();
        String trustworthiness= currentItem.getTrustworthiness();
        double latitude = currentItem.getLatitude();
        double longitude= currentItem.getLongitude();
        String cityName= currentItem.getCityName();

//        //processing the time stamp and convert it to a readable date.
//        long ts= Long.parseLong(timestamp);
//        Date d = new Date(ts * 1000);
//        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZ");
//      //  df.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
//        String mDate= (df.format(d));



        viewHolder.id.setText(id);
        viewHolder.description.setText(description);
        viewHolder.timestamp.setText("Posted: "+timestamp);
        viewHolder.location.setText("Location: " +cityName+" " +latitude + "/" + longitude);
        viewHolder.trustworthiness.setText("Trustworthiness: "+ trustworthiness );
        Glide.with(mcontext).load("http://192.168.3.103/data/"+imageUrl+".jpg").into(viewHolder.imageView);

        //Rating bar
        float rating = Float.parseFloat(trustworthiness);

        if (rating <= 0) {
           //0star
            viewHolder.ratingBar.setRating(0);
        }
        else if ( rating > 0 && rating <5 ) {
            //one star
            viewHolder.ratingBar.setRating(1);
        }
        else if (rating >= 5 && rating <10 ) {
            //two stars
            viewHolder.ratingBar.setRating(2);
        }
        else if (rating >= 10 && rating <15 ) {
            //3 stars
            viewHolder.ratingBar.setRating(3);
        }
        else if (rating >= 15 && rating < 20 ) {
            //4 stars
            viewHolder.ratingBar.setRating(4);
        }
        else if (rating >= 20 ) {
            //4 stars
            viewHolder.ratingBar.setRating(5);
        }



    }

    @Override
    public int getItemCount() {
        return mmyData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView description,id,location,timestamp,trustworthiness;
        public ImageView imageView ;
        public RatingBar ratingBar;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id= itemView.findViewById(R.id.event_eventid_tv);
            description =  itemView.findViewById(R.id.event_text) ;
            imageView = itemView.findViewById(R.id.event_image) ;
            location= itemView.findViewById(R.id.event_location_tv);
            trustworthiness=  itemView.findViewById(R.id.event_rank_tv);
            timestamp= itemView.findViewById(R.id.event_timestamp_tv) ;
            ratingBar=itemView.findViewById(R.id.event_ratingBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(mListener != null ){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            }
            );
        }


    }
}
