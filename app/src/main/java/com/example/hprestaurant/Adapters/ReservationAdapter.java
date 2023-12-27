package com.example.hprestaurant.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hprestaurant.Models.ReservationModel;
import com.example.hprestaurant.R;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    private Context context;
    private List<ReservationModel> reservationModels;
    private OnClickItemListener onClickItemListener;

    public ReservationAdapter(Context context, List<ReservationModel> reservationModels) {
        this.context = context;
        this.reservationModels = reservationModels;
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener){
        this.onClickItemListener = onClickItemListener;
    }
    public void addItem(ReservationModel reservationModel){
        reservationModels.add(reservationModel);
        notifyItemInserted(reservationModels.indexOf(reservationModel));
    }

    public void updateItem(ReservationModel reservation){
        boolean isUpdate =false;
        for(ReservationModel model : reservationModels){
            if(model.getId() == reservation.getId()){
                int index = reservationModels.indexOf(model);
                reservationModels.set(index,reservation);
                notifyItemChanged(index);
                isUpdate = true;
                break;
            }
        }
        if(!isUpdate){
            addItem(reservation);
        }
    }

    public interface OnClickItemListener{
        void onClick(int position);
    }

    @NonNull
    @Override
    public ReservationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ReservationModel model = reservationModels.get(position);
        if(model == null) return;
        if(model.getTable_id() == -1){
            holder.tableName.setText("Chưa có bàn");
        }else{
            holder.tableName.setText(model.getTable_name());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime dateTime = model.getReservation_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        holder.date.setText(formatter.format(dateTime));
        String sTime = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        holder.time.setText(sTime);
        if(model.getStatus().equals("Chờ xác nhận")){
            holder.status.setText(model.getStatus());
            holder.status.setTextColor(context.getColor(R.color.red));
        }else {
            holder.status.setText(model.getStatus());
            holder.status.setTextColor(context.getColor(R.color.green));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickItemListener != null){
                    onClickItemListener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (reservationModels != null) return reservationModels.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private  TextView tableName,date,time,status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tableName = itemView.findViewById(R.id.table_name_text_view);
            date = itemView.findViewById(R.id.date_text_view);
            time = itemView.findViewById(R.id.time_tv);
            status = itemView.findViewById(R.id.status_tv);
        }
    }
}
