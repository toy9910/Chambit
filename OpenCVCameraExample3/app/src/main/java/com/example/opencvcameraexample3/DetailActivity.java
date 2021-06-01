package com.example.opencvcameraexample3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.opencvcameraexample3.Class.CarData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {
    String TAG = "chambit";
    String IP_ADDRESS = "3.36.237.233";

    String mJsonString;
    String errorString;

    TextView tv_car_no;
    TextView tv_name;
    TextView tv_phone;
    TextView tv_ho;
    TextView tv_out;

    ImageView iv_car;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tv_car_no = findViewById(R.id.txtCarNum);
        tv_name = findViewById(R.id.txtName);
        tv_phone = findViewById(R.id.txtPhoneNum);
        tv_ho = findViewById(R.id.txtAddress);
        tv_out = findViewById(R.id.textExitDate);
        iv_car = findViewById(R.id.car_imageview);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReferenceFromUrl("gs://chambit-da2c6.appspot.com");

        Intent intent = getIntent();
        String car_no = intent.getStringExtra("car_no");
        Log.d(TAG, "onCreate: "+car_no);

        GetVisData getVisData = new GetVisData();
        getVisData.execute("http://"+ IP_ADDRESS + "/chambit_vis_query.php",car_no);
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
                String out = item.getString(TAG_OUT);

                tv_car_no.setText(car_no);
                tv_name.setText(name);
                tv_phone.setText(phone);
                tv_ho.setText(ho);
                tv_out.setText(out);


                // 파이어베이스에서 이미지 다운로드
                storageReference.child("car_img/" + phone + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext()).load(uri).into(iv_car);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult: "+e);
        }
    }
}