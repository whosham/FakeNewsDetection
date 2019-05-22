package com.example.fakenewsdetection;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private Context mcontext;
   // private List<MyData> my_data;

    private ArrayList<MyData> mmyData;

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

      // MyData currentItem = myData.get(i);
      //  String image_url = currentItem.getImage_url() ;

        MyData currentItem = mmyData.get(i) ;
        String imageUrl = currentItem.getImage_url();
        String creatorName = currentItem.getDescription();

        viewHolder.description.setText(creatorName);
        Glide.with(mcontext).load(imageUrl).into(viewHolder.imageView);


       // viewHolder.description.setText(mmyData.get(i).getDescription());
       // Glide.with(mcontext).load(mmyData.get(i).getImage_url()).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return mmyData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView description ;
        public ImageView imageView ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            description =  itemView.findViewById(R.id.event_text) ;
            imageView = itemView.findViewById(R.id.event_image) ;

        }
    }
}
