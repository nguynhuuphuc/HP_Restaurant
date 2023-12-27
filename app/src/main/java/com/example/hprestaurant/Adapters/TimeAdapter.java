package com.example.hprestaurant.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hprestaurant.Models.TimeFilterModel;
import com.example.hprestaurant.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<LocalTime> times;
    private List<LocalTime> originalTimes;
    private onClickItemListener onClicked;
    private int selectedPosition = -1;
    private LocalTime timeAvailable;



    public interface onClickItemListener{
        void onClick(LocalTime position);
    }

    public TimeAdapter(Context context, List<LocalTime> times) {
        this.context = context;
        this.times = times;
        this.originalTimes = times;
    }
    public void setData( List<LocalTime> times){
        this.times = times;
        this.originalTimes = times;
        notifyDataSetChanged();
    }

    public void setOnClickItemListener(onClickItemListener onClicked){
        this.onClicked = onClicked;
    }

    @NonNull
    @Override
    public TimeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull TimeAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LocalTime time = times.get(position);
        if(time == null) return;
        if(selectedPosition == position){
            //Selected
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.lightest_yellow_orange));
            holder.timeTv.setTextColor(context.getColor(R.color.text_color));
        }
        else{
            //Not selected
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.white));
            holder.timeTv.setTextColor(context.getColor(R.color.gray));
        }

        String sTime = time.format(DateTimeFormatter.ofPattern("HH:mm"));
        holder.timeTv.setText(sTime);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedPosition == position)
                {
                    selectedPosition = 0;
                    notifyDataSetChanged();
                    return;
                }
                selectedPosition = position;
                notifyDataSetChanged();
                onClicked.onClick(time);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(times != null) return times.size();
        return 0;
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String time_available = constraint.toString();
                if(time_available.isEmpty()){
                    times = originalTimes;
                }else {
                    // Chuyển đổi chuỗi thành đối tượng LocalTime
                    List<LocalTime> list = new ArrayList<>();
                    LocalTime currentTime = LocalTime.now();
                    try {
                        timeAvailable = LocalTime.parse(time_available);
                        for(LocalTime time : originalTimes){
                            if(time.equals(timeAvailable) || time.isAfter(timeAvailable)){
                                list.add(time);
                            }
                        }

                    }catch (Exception e){
                        if(timeAvailable != null)
                            for(LocalTime time : originalTimes){
                                if(time.isAfter(currentTime) && (time.isAfter(timeAvailable) || time.equals(timeAvailable))){
                                    list.add(time);
                                }
                            }
                        else{
                            for(LocalTime time : originalTimes){
                                if(time.isAfter(currentTime)){
                                    list.add(time);
                                }
                            }
                        }
                    }

                    times = list;
                }


                FilterResults results = new FilterResults();
                results.values = times;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                times = (List<LocalTime>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView timeTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTv = itemView.findViewById(R.id.time_tv);
            cardView = itemView.findViewById(R.id.itemTimeCv);
        }
    }
}
