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
    String IP_ADDRESS = "3.36.237.233";

    String mJsonString;
    String errorString;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

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

//    private class DeleteData extends AsyncTask<String,Void,String> {
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            Log.d("chambit", "POST response -" + s);
//
//            if(s == null) {
//                Log.e("chambit",errorString);
//            }
//            else {
//                mJsonString = s;
//                showVisResult();
//            }
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            String serverURL = strings[0];
//            String postParameters = "car_no=" + strings[1];
//
//            try {
//                URL url = new URL(serverURL);
//                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
//
//                httpURLConnection.setReadTimeout(5000);
//                httpURLConnection.setConnectTimeout(5000);
//                httpURLConnection.setRequestMethod("POST");
//                httpURLConnection.connect();
//
//                OutputStream outputStream = httpURLConnection.getOutputStream();
//                outputStream.write(postParameters.getBytes("UTF-8"));
//                outputStream.flush();
//                outputStream.close();
//
//                int responseStatusCode = httpURLConnection.getResponseCode();
//                Log.d("chambit", "POST response code - "+ responseStatusCode);
//
//                InputStream inputStream;
//                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
//                    inputStream = httpURLConnection.getInputStream();
//                }
//                else {
//                    inputStream = httpURLConnection.getErrorStream();
//                }
//
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder sb = new StringBuilder();
//                String line;
//
//                while((line = bufferedReader.readLine()) != null) {
//                    sb.append(line);
//                }
//
//                bufferedReader.close();
//                return sb.toString().trim();
//            } catch (Exception e) {
//                Log.d("chambit", "doInBackground Error : " + e);
//                errorString = e.toString();
//                return e.getMessage();
//            }
//        }
//    }

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

//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder( v.getContext() );
//                    builder.setTitle("삭제");
//                    builder.setMessage("삭제를 하시겠습니까?");
//                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            DeleteData deleteData = new DeleteData();
//                            deleteData.execute("http://" + IP_ADDRESS + "/chambit_vis_delete.php",car_no.getText().toString());
//                        }
//                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                }
//            });
        }
    }
}