package com.example.opencvcameraexample3.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.opencvcameraexample3.Class.CarData;
import com.example.opencvcameraexample3.DetailActivity;
import com.example.opencvcameraexample3.MainActivity;
import com.example.opencvcameraexample3.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VisCarAdapter extends RecyclerView.Adapter<VisCarAdapter.ViewHolder> {
    private ArrayList<CarData> arrayList = null;

    private OnItemLongClickListener mListener = null;

    public VisCarAdapter(ArrayList<CarData> list) {
        arrayList = list;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View v, int pos);
    }


    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mListener = listener;
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

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            String current_time = format.format(System.currentTimeMillis());

            Date date_cur = format.parse(current_time);
            Date date_out = format.parse(cd.getOut_time());

            if(date_cur.after(date_out)) {
                holder.car_no.setTextColor(Color.parseColor("#FFAB00"));
                holder.name.setTextColor(Color.parseColor("#FFAB00"));
                holder.phone.setTextColor(Color.parseColor("#FFAB00"));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(mListener != null) {
                        mListener.onItemLongClick(v,getAdapterPosition());
                    }
                    return true;
                }
            });
        }
    }
}