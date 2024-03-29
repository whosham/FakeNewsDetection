package com.example.fakenewsdetection;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.ViewHolder> {
    private Context mcontext;
    // private List<MyData> my_data;
    private ArrayList<AssessmentData> mmyData;

    private onItemClickListener mListener;

    public interface onItemClickListener {

        void onItemClick(int position ) ;

    }

    public void setOnItemClickListener(onItemClickListener listener){
        mListener=listener ;
    }


    //  public CustomAdapter(Context context, List<MyData> my_data) {
    public AssessmentAdapter(Context context, ArrayList<AssessmentData> myDataList) {
        this.mcontext = context;
        this.mmyData= myDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items,viewGroup,false) ;
        View v = LayoutInflater.from(mcontext).inflate(R.layout.assessment, viewGroup, false);

        return new ViewHolder(v) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        AssessmentData currentItem = mmyData.get(i) ;
        String imageUrl = currentItem.getImage_url();
        String description= currentItem.getDescription();
        String id= currentItem.getId();
        String rating =currentItem.getRating();
        String timestamp = currentItem.getTimestamp();
        String trustworthiness= currentItem.getTrustworthiness();



        //processing the time stamp and convert it to a readable date.
        long ts= Long.parseLong(timestamp);
        Date d = new Date(ts * 1000);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZ");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        String mDate= (df.format(d));


        viewHolder.id.setText(id);
        viewHolder.description.setText(description);
        viewHolder.timestamp.setText("Posted: "+mDate);
        viewHolder.trustworthiness.setText("Trustworthiness: "+ trustworthiness + " Ranking: " + rating );


        if (rating.equals("1")) {
            viewHolder.assessment_downvote.setVisibility(View.INVISIBLE);
        }
        else {
            viewHolder.assessment_upvote.setVisibility(View.INVISIBLE);
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.info_button).override(300,300);
        Glide.with(mcontext)
                .load("http://192.168.3.103/data/"+imageUrl+".jpg")
                .apply(requestOptions)
                .into(viewHolder.imageView)
        ;

    }

    @Override
    public int getItemCount() {
        return mmyData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView description,id,timestamp,trustworthiness;
        public ImageView imageView,assessment_upvote,assessment_downvote ;
        public RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id= itemView.findViewById(R.id.assessment_eventid_tv);
            description =  itemView.findViewById(R.id.assessment_text_tv) ;
            imageView = itemView.findViewById(R.id.assessment_image_iv) ;
            trustworthiness=  itemView.findViewById(R.id.assessment_rank_tv);
            timestamp= itemView.findViewById(R.id.assessment_timestamp_tv) ;
            assessment_upvote=itemView.findViewById(R.id.assessment_upvote_iv);
            assessment_downvote=itemView.findViewById(R.id.assessment_downvote_iv);

           // ratingBar= itemView.findViewById(R.id.event_ratingBar);

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
