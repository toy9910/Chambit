package com.example.opencvcameraexample3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.opencvcameraexample3.Adapter.CarAdapter;
import com.example.opencvcameraexample3.Adapter.VisCarAdapter;
import com.example.opencvcameraexample3.Class.CarData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Vis_CarListActivity extends AppCompatActivity {
    String TAG = "chambit";
    String IP_ADDRESS = "3.36.237.233";

    String mJsonString;
    String errorString;

    ArrayList<CarData> list = new ArrayList<>();
    VisCarAdapter vis_adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vis__car_list);


        RecyclerView vis_recyclerView = findViewById(R.id.vis_recycler);
        vis_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list.clear();
        vis_adapter = new VisCarAdapter(list);
        vis_recyclerView.setAdapter(vis_adapter);
        vis_adapter.notifyDataSetChanged();

        Vis_CarListActivity.GetVisData vTask = new Vis_CarListActivity.GetVisData();
        vTask.execute("http://"+ IP_ADDRESS + "/chambit_vis_getjson.php","");
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_register : {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }

            case R.id.btn_list:  {
                Intent intent = new Intent(getApplicationContext(), CarListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
    }


    private class GetVisData extends AsyncTask<String,Void,String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "POST response -" + s);

            if(s == null) {
                Log.e(TAG,errorString);
            }
            else {
                mJsonString = s;
                showVisResult();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String serverURL = strings[0];
            String postParameters = "car_no=" + strings[1];

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - "+ responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString().trim();
            } catch (Exception e) {
                Log.d(TAG, "doInBackground Error : " + e);
                errorString = e.toString();
                return e.getMessage();
            }
        }
    }

    public void showVisResult() {
        String TAG_JSON = "chambit_dev";
        String TAG_CAR_NO = "vis_car_no";
        String TAG_NAME = "vis_name";
        String TAG_PHONE = "vis_phone";
        String TAG_HO = "vis_ho";
        String TAG_IN = "vis_in";
        String TAG_OUT = "vis_out";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                String car_no = item.getString(TAG_CAR_NO);
                String name = item.getString(TAG_NAME);
                String phone = item.getString(TAG_PHONE);
                String ho = item.getString(TAG_HO);
                String in_time = item.getString(TAG_IN);
                String out_time = item.getString(TAG_OUT);

                CarData carData = new CarData(car_no,name,phone,ho,in_time,out_time);
                list.add(carData);
                vis_adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult: "+e);
        }
    }
}