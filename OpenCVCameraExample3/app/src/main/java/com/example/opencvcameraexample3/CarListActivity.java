package com.example.opencvcameraexample3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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

public class CarListActivity extends AppCompatActivity {
    String TAG = "chambit";
    String IP_ADDRESS = "3.35.105.27";

    String mJsonString;
    String errorString;

    ArrayList<CarData> list = new ArrayList<>();
    CarAdapter res_adapter;
    CarAdapter vis_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);


        RecyclerView res_recyclerView = findViewById(R.id.res_recycler);
        res_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView vis_recyclerView = findViewById(R.id.vis_recycler);
        vis_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        res_adapter = new CarAdapter(list);
        res_recyclerView.setAdapter(res_adapter);

        list.clear();
        res_adapter.notifyDataSetChanged();

        GetResData rTask = new GetResData();
        rTask.execute("http://"+ IP_ADDRESS + "/chambit_res_getjson.php","");


        list.clear();
        vis_adapter = new CarAdapter(list);
        vis_recyclerView.setAdapter(vis_adapter);
        vis_adapter.notifyDataSetChanged();

        GetVisData vTask = new GetVisData();
        vTask.execute("http://"+ IP_ADDRESS + "/chambit_vis_getjson.php","");
    }

    private class GetResData extends AsyncTask<String,Void,String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "POST response -" + s);

            if(s == null) {
                Log.e(TAG,errorString);
            }
            else {
                mJsonString = s;
                showResResult();
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

    public void showResResult() {
        String TAG_JSON = "chambit_dev";
        String TAG_CAR_NO = "res_car_no";
        String TAG_NAME = "res_name";
        String TAG_PHONE = "res_phone";
        String TAG_DONG = "res_dong";
        String TAG_HO = "res_ho";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                String car_no = item.getString(TAG_CAR_NO);
                String name = item.getString(TAG_NAME);
                String phone = item.getString(TAG_PHONE);
                String dong = item.getString(TAG_DONG);
                String ho = item.getString(TAG_HO);
                String address = dong + " " + ho;

                CarData carData = new CarData(car_no,name,phone,address);
                list.add(carData);
                res_adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult: "+e);
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
        String TAG_DONG = "vis_dong";
        String TAG_HO = "vis_ho";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                String car_no = item.getString(TAG_CAR_NO);
                String name = item.getString(TAG_NAME);
                String phone = item.getString(TAG_PHONE);
                String dong = item.getString(TAG_DONG);
                String ho = item.getString(TAG_HO);
                String address = dong + " " + ho;

                CarData carData = new CarData(car_no,name,phone,address);
                list.add(carData);
                vis_adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult: "+e);
        }
    }
}