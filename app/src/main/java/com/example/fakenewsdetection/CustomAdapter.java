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

import java.util.List;

class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private Context context;
    private List<MyData> my_data;

    public CustomAdapter(Context context, List<MyData> my_data) {
        this.context = context;
        this.my_data= my_data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items,viewGroup,false) ;
        return new ViewHolder(itemView) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.description.setText(my_data.get(i).getDescription());
        Glide.with(context).load(my_data.get(i).getImage_link()).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return my_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView description ;
        public ImageView imageView ;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            description =  itemView.findViewById(R.id.event_text) ;
            imageView = itemView.findViewById(R.id.event_image) ;

        }
    }
}
