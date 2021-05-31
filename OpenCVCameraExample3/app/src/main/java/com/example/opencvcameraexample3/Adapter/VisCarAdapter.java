package com.example.opencvcameraexample3.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.opencvcameraexample3.Class.CarData;
import com.example.opencvcameraexample3.DetailActivity;
import com.example.opencvcameraexample3.R;

import java.util.ArrayList;

public class VisCarAdapter extends RecyclerView.Adapter<VisCarAdapter.ViewHolder> {
    private ArrayList<CarData> arrayList = null;

    public VisCarAdapter(ArrayList<CarData> list) {
        arrayList = list;
    }

    @NonNull
    @Override
    public VisCarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_vis_item,parent,false);
        VisCarAdapter.ViewHolder vh = new VisCarAdapter.ViewHolder(view);

        return vh;
    }



    @Override
    public void onBindViewHolder(@NonNull VisCarAdapter.ViewHolder holder, int position) {
        CarData cd = arrayList.get(position);
        holder.car_no.setText(cd.getCar_no());
        holder.name.setText(cd.getName());
        holder.phone.setText(cd.getPhone());

        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("car_no",holder.car_no.getText().toString());
                ContextCompat.startActivity(holder.itemView.getContext(),intent,null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView car_no;
        TextView name;
        TextView phone;
        Button btn_detail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            car_no = itemView.findViewById(R.id.tv_car_no);
            name = itemView.findViewById(R.id.tv_name);
            phone = itemView.findViewById(R.id.tv_phone);
            btn_detail = itemView.findViewById(R.id.btn_detail);
        }
    }
}