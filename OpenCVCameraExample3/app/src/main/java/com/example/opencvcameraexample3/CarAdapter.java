package com.example.opencvcameraexample3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.ViewHolder> {
    private ArrayList<CarData> arrayList = null;

    public CarAdapter(ArrayList<CarData> list) {
        arrayList = list;
    }

    @NonNull
    @Override
    public CarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_item,parent,false);
        CarAdapter.ViewHolder vh = new CarAdapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull CarAdapter.ViewHolder holder, int position) {
        CarData cd = arrayList.get(position);
        holder.car_no.setText(cd.getCar_no());
        holder.name.setText(cd.getName());
        holder.phone.setText(cd.getPhone());
        holder.address.setText(cd.getAddress());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView car_no;
        TextView name;
        TextView phone;
        TextView address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            car_no = itemView.findViewById(R.id.tv_car_no);
            name = itemView.findViewById(R.id.tv_name);
            phone = itemView.findViewById(R.id.tv_phone);
            address = itemView.findViewById(R.id.tv_address);
        }
    }
}
