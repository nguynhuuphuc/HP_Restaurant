package com.example.hprestaurant.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hprestaurant.Interfaces.IClickItemTableListener;
import com.example.hprestaurant.Models.MessageEvent;
import com.example.hprestaurant.Models.TableModel;

import com.example.hprestaurant.R;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> implements Filterable {

    private List<TableModel> tableModelList;
    private final List<TableModel> tableModelListOld;
    private IClickItemTableListener iClickItemTableListener;
    private Context context;
    private List<TableModel> tableSaver = new ArrayList<>();


    public TableAdapter(Context context, List<TableModel> tableModelList, IClickItemTableListener listener) {
        this.tableModelListOld = tableModelList;
        this.context = context;
        this.tableModelList = tableModelList;
        iClickItemTableListener = listener;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TableModel tableModel = tableModelList.get(position);
        if(tableModel == null) return;
        holder.nameTable.setText(tableModel.getTable_name());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iClickItemTableListener.onClickItemTableListener(tableModel);
            }
        });

    }


    @Override
    public int getItemCount() {
        if(tableModelList != null) return tableModelList.size();
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String sSearch = constraint.toString();
                if(sSearch.isEmpty()){
                    tableModelList = tableModelListOld;
                }
                else {
                    List<TableModel> list = new ArrayList<>();
                    int locationId = -1;
                    try {
                        locationId = Integer.parseInt(sSearch);
                    }catch (Exception ignored){}
                    if(locationId != -1) {
                        for (TableModel tableModel : tableModelListOld) {
                            if (tableModel.getLocation_id() == Integer.parseInt(sSearch)) {
                                list.add(tableModel);
                            }
                        }
                    }else {
                        JsonObject jsonObject = JsonParser.parseString(sSearch).getAsJsonObject();
                        boolean isOccupied = jsonObject.get("is_occupied").getAsBoolean();
                        int location_id = jsonObject.get("locationId").getAsInt();
                        for (TableModel tableModel : tableModelListOld){
                            if(tableModel.getLocation_id() == location_id){
                                list.add(tableModel);
                            }
                        }

                    }
                    tableModelList = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = tableModelList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                tableModelList = (List<TableModel>) results.values;
                notifyDataSetChanged();

            }
        };
    }

    public void updatingTableInProgress(MessageEvent event){
        if(event.isIn_progress()){
            //removing
            for(TableModel table : tableModelList){
                if(table.getTable_id() == event.getTable_id()){
                    int index = tableModelList.indexOf(table);
                    tableSaver.add(table);
                    tableModelList.remove(table);
                    notifyItemRemoved(index);
                    break;
                }
            }
            return;
        }
        //adding
        for(TableModel tableModel : tableSaver){
            if(tableModel.getTable_id() == event.getTable_id()){
                tableModelList.add(tableModel);
                tableSaver.remove(tableModel);
                tableModelList.sort(Comparator.comparing(TableModel::getTable_id));
                notifyDataSetChanged();
                break;
            }
        }

    }


    public void updateTable(TableModel model){
        for (int i = 0; i < tableModelListOld.size(); i++){
            if(i < tableModelList.size() && tableModelList.get(i).getTable_id() == model.getTable_id()){
                tableModelList.set(i,model);
                notifyItemChanged(i);
            }
            if(tableModelListOld.get(i).getTable_id() == model.getTable_id()){
                tableModelListOld.set(i,model);
               notifyItemChanged(i);
                 break;
            }
        }
    }
    public void updateOldTable(List<TableModel> models){
        for (TableModel model : models){
            for (int i = 0; i < tableModelListOld.size(); i++){
                if(tableModelListOld.get(i).getTable_id() == model.getTable_id()){
                    tableModelListOld.set(i,model);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout tablelabelLl,infotableLl;
        TextView nameTable,status,curDate,currTime,totalPrice;
        MaterialCardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTable = itemView.findViewById(R.id.table_name_text_view);
        }
    }




}
