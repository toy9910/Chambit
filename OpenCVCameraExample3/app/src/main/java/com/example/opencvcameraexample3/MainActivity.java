package com.example.opencvcameraexample3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.CAMERA;


public class MainActivity extends AppCompatActivity {
    String TAG = "chambit";
    String IP_ADDRESS = "3.36.237.233";
    final String[] items = new String[]{"내부 차량","외부 차량"};

    ImageView roi_img;
    Bitmap image;
    EditText tes_result;
    EditText name;
    EditText phone;
    EditText dong;
    EditText ho;

    static TessBaseAPI sTess;
    String lang;
    String datapath;
    byte[] roi_data;

    Button btn_car_list;
    Button btn_register;
    int selected = -1;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    File imgFile;

    String[] permission_list = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roi_img = (ImageView)findViewById(R.id.roi_photo);
        tes_result = findViewById(R.id.tess_result);
        name = findViewById(R.id.et_name);
        phone = findViewById(R.id.et_phone);
        dong =  findViewById(R.id.et_dong);
        ho = findViewById(R.id.et_ho);
        btn_car_list = findViewById(R.id.btn_car_list);
        btn_register = findViewById(R.id.btn_register);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReferenceFromUrl("gs://chambit-da2c6.appspot.com");

        checkPermission();

        // 입주자 차량 등록 버튼
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 차량 선택 다이얼로그 생성
                new AlertDialog.Builder(MainActivity.this).setTitle("선택")
                        .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: "+items[which]);
                        selected = which;
                        Toast.makeText(getApplicationContext(),items[which],Toast.LENGTH_SHORT).show();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (selected) {
                            case 0: { // 내부차량 등록
                                if(tes_result.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 내부차량 번호를 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "차량 번호를 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else if(name.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 차주를 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "차주를 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else if(phone.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 전화 번호를 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "전화 번호를 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else if(dong.getText().toString().equals("") || ho.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 주소를 제대로 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "주소를 제대로 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    InsertData insertData = new InsertData();
                                    insertData.execute("http://" + IP_ADDRESS + "/chambit_res_insert.php","0");
                                    Toast.makeText(getApplicationContext(), "입주자 차량이 등록되었습니다.", Toast.LENGTH_LONG).show();

                                    // 파이어베이스에 업로드
                                    Uri uri = Uri.fromFile(imgFile);

                                    StringBuffer stringBuffer = new StringBuffer();
                                    stringBuffer.append(phone.getText().toString());
                                    stringBuffer.insert(3,"-");
                                    stringBuffer.insert(8,"-");
                                    String imgTitle = stringBuffer.toString();
                                    StorageReference rivRef = storageReference.child("car_img/"+ imgTitle + ".png");
                                    UploadTask uploadTask1 = rivRef.putFile(uri);
                                    Log.d(TAG, "onActivityResult: "+storageReference.toString());
                                    uploadTask1.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            Log.d(TAG, "onSuccess: 이미지 업로드 완료");
                                            Toast.makeText(getApplicationContext(),"이미지 업로드 완료.",Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    tes_result.setText("");
                                    name.setText("");
                                    phone.setText("");
                                    dong.setText("");
                                    ho.setText("");
                                    roi_img.setImageBitmap(null);
                                }
                                break;
                            }
                            case 1: { // 외부차량 등록
                                if(tes_result.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 외부차량 번호를 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "차량 번호를 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else if(name.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 차주를 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "차주를 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else if(phone.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 전화 번호를 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "전화 번호를 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else if(dong.getText().toString().equals("") || ho.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 주소를 제대로 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "주소를 제대로 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    final EditText txtEdit = new EditText( MainActivity.this );

                                    AlertDialog.Builder clsBuilder = new AlertDialog.Builder( MainActivity.this );
                                    clsBuilder.setTitle( "출차 예상 시간" );
                                    clsBuilder.setView( txtEdit );
                                    clsBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String strText = txtEdit.getText().toString();

                                            InsertData insertData = new InsertData();
                                            insertData.execute("http://" + IP_ADDRESS + "/chambit_vis_insert.php","1",strText);
                                            Toast.makeText(getApplicationContext(), "방문자 차량이 등록되었습니다.", Toast.LENGTH_LONG).show();

                                            // 파이어베이스에 업로드
                                            Uri uri = Uri.fromFile(imgFile);
                                            StringBuffer stringBuffer = new StringBuffer();
                                            stringBuffer.append(phone.getText().toString());
                                            stringBuffer.insert(3,"-");
                                            stringBuffer.insert(8,"-");
                                            String imgTitle = stringBuffer.toString();
                                            StorageReference rivRef = storageReference.child("car_img/"+ imgTitle + ".png");
                                            UploadTask uploadTask1 = rivRef.putFile(uri);
                                            Log.d(TAG, "onActivityResult: "+storageReference.toString());
                                            uploadTask1.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    Log.d(TAG, "onSuccess: 이미지 업로드 완료");
                                                    Toast.makeText(getApplicationContext(),"이미지 업로드 완료.",Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                            tes_result.setText("");
                                            name.setText("");
                                            phone.setText("");
                                            dong.setText("");
                                            ho.setText("");
                                            roi_img.setImageBitmap(null);
                                        }
                                    });
                                    clsBuilder.setNegativeButton("취소",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                    clsBuilder.show();


                                }
                                break;
                            }
                        }
                    }
                }).show();
            }
        });

        // 차량 조회 버튼
        btn_car_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] arr = {"내부차량 리스트", "외부차량 리스트"};
                new AlertDialog.Builder(MainActivity.this).setTitle("차량 리스트 조회").setItems(arr, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0 : {
                                Intent intent = new Intent(getApplicationContext(), CarListActivity.class);
                                startActivity(intent);
                                break;
                            }
                            case 1 : {
                                Intent intent = new Intent(getApplicationContext(), Vis_CarListActivity.class);
                                startActivity(intent);
                                break;
                            }
                        }
                    }
                }).setNeutralButton("닫기", null).setPositiveButton("확인", null).show();

            }
        });
    }

    boolean checkFile(File dir)
    {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
        return true;
    }

    void copyFiles()
    {
        AssetManager assetMgr = this.getAssets();

        InputStream is = null;
        OutputStream os = null;

        try {
            is = assetMgr.open("tessdata/"+lang+".traineddata");

            String destFile = datapath + "/tessdata/" + lang + ".traineddata";

            os = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            is.close();
            os.flush();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class InsertData extends AsyncTask<String,Void,String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "POST response -" + s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String str_car_no = tes_result.getText().toString();
            String str_name = name.getText().toString();
            String str_phone = phone.getText().toString();
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str_phone);
            stringBuffer.insert(3,"-");
            stringBuffer.insert(8,"-");
            str_phone = stringBuffer.toString();
            String str_dong = dong.getText().toString() + "동 ";
            String str_ho = ho.getText().toString() + "호";

            String serverURL = strings[0];
            String postParameters;
            if(strings[1].equals("0")) {
                postParameters = "car_no=" + str_car_no + "&name=" + str_name + "&phone=" + str_phone
                        + "&ho=" + str_dong + str_ho;
            }
            else {
                String out_time = strings[2];
                postParameters = "car_no=" + str_car_no + "&name=" + str_name + "&phone=" + str_phone
                        + "&ho=" + str_dong + str_ho + "&outtime=" + out_time;
            }

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
                return e.getMessage();
            }
        }
    }

    private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {

        @Override
        protected String doInBackground(Bitmap... mRelativeParams) {
            //Tesseract OCR 수행
            sTess.setImage(image);

            return sTess.getUTF8Text();
        }

        protected void onPostExecute(String result) {
            //완료 후 버튼 속성 변경 및 결과 출력
            Log.d(TAG, "onPostExecute: "+result);
            String result2 = result;
            tes_result.setText(result2);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_capture: {
                Intent intent = new Intent(MainActivity.this,CaptureActivity.class);
                startActivityForResult(intent,0);
                break;
            }
            case R.id.btn_car_search: {

                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0) {
            if(resultCode==RESULT_OK) {
                sTess = new TessBaseAPI();
                lang = "kor";
                datapath = getFilesDir() + "/tesseract";

                if(checkFile(new File(datapath+"/tessdata")))
                {
                    sTess.init(datapath, lang);
                }

                // CaptureActivity로 부터 roi 부분만 캡처한 것 가져오기
                roi_data = data.getByteArrayExtra("roi");
                image = BitmapFactory.decodeByteArray(roi_data,0,roi_data.length);
                roi_img.setImageBitmap(image);

                // CaptureActivity에서 보낸 경로에서 원본 사진 가져오기
                String title = data.getStringExtra("title");
                File storage = getCacheDir();
                imgFile = new File(storage,title+".png");


                //new AsyncTess().execute(image);
            }
        }
    }


    public void checkPermission(){
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        for(String permission : permission_list){
            //권한 허용 여부를 확인한다.
            int chk = checkCallingOrSelfPermission(permission);

            if(chk == PackageManager.PERMISSION_DENIED){
                //권한 허용을여부를 확인하는 창을 띄운다
                requestPermissions(permission_list,0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0)
        {
            for(int i=0; i<grantResults.length; i++)
            {
                //허용됬다면
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                }
                else {
                    Toast.makeText(getApplicationContext(),"앱권한설정하세요",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

}