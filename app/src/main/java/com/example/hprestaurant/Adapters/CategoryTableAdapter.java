package com.example.hprestaurant.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hprestaurant.Interfaces.IClickItemCategoryTableListener;
import com.example.hprestaurant.Models.LocationModel;
import com.example.hprestaurant.R;


import java.util.List;


public class CategoryTableAdapter extends RecyclerView.Adapter<CategoryTableAdapter.ViewHolder> {

    private IClickItemCategoryTableListener iClickItemListener;
    private List<LocationModel> locationList;
    private Context context;

    int selectedPosition = 0;

    public CategoryTableAdapter(Context context,List<LocationModel> locationList, IClickItemCategoryTableListener iClickItemListener ) {
        this.iClickItemListener = iClickItemListener;
        this.locationList = locationList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_table,parent,false));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        LocationModel location = locationList.get(position);
        if(location == null){
            return;
        }
        if(selectedPosition == position){
            //Selected
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.lightest_yellow_orange));
            holder.categoryNameTv.setTextColor(context.getColor(R.color.text_color));
        }
        else{
            //Not selected
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.white));
            holder.categoryNameTv.setTextColor(context.getColor(R.color.gray));
        }
            holder.categoryNameTv.setText(location.getLocation_name());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selectedPosition == position)
                    {
                        selectedPosition = 0;
                        notifyDataSetChanged();
                        return;
                    }
                    selectedPosition = position;
                    notifyDataSetChanged();
                    iClickItemListener.onClickItemCategoryTableListener(location);
                }
            });
    }

    @Override
    public int getItemCount() {
       return locationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTv;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.itemCategoryCv);
            categoryNameTv = itemView.findViewById(R.id.categoryNameTv);

        }
    }
}
