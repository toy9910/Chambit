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

public class CarListActivity extends AppCompatActivity {
    String TAG = "chambit";
    String IP_ADDRESS = "3.35.105.27";

    String mJsonString;
    String errorString;

    ArrayList<CarData> list = new ArrayList<>();
    CarAdapter res_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        RecyclerView res_recyclerView = findViewById(R.id.res_recycler);
        res_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        res_adapter = new CarAdapter(list);
        res_recyclerView.setAdapter(res_adapter);

        list.clear();
        res_adapter.notifyDataSetChanged();

        GetResData rTask = new GetResData();
        rTask.execute("http://"+ IP_ADDRESS + "/chambit_res_getjson.php","");

    }

    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btn_register : {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.btn_vis_list:  {
                Intent intent = new Intent(getApplicationContext(), Vis_CarListActivity.class);
                startActivity(intent);
                break;
            }
        }
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
        String TAG_HO = "res_ho";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                String car_no = item.getString(TAG_CAR_NO);
                String name = item.getString(TAG_NAME);
                String phone = item.getString(TAG_PHONE);
                String ho = item.getString(TAG_HO);

                CarData carData = new CarData(car_no,name,phone,ho);
                list.add(carData);
                res_adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult: "+e);
        }
    }


}