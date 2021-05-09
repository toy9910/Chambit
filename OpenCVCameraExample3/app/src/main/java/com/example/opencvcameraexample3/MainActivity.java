package com.example.opencvcameraexample3;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.CAMERA;


public class MainActivity extends AppCompatActivity {
    String TAG = "chambit";
    String IP_ADDRESS = "3.35.105.27";
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
    DataOutputStream dos;

    Button btn_car_list;
    Button btn_register;
    int selected = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tes_result = findViewById(R.id.tess_result);
        name = findViewById(R.id.et_name);
        phone = findViewById(R.id.et_phone);
        dong =  findViewById(R.id.et_dong);
        ho = findViewById(R.id.et_ho);
        btn_car_list = findViewById(R.id.btn_car_list);
        btn_register = findViewById(R.id.btn_register);

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
                                else {
                                    InsertData insertData = new InsertData();
                                    insertData.execute("http://" + IP_ADDRESS + "/chambit_res_insert.php");
                                    Toast.makeText(getApplicationContext(), "입주자 차량이 등록되었습니다.", Toast.LENGTH_LONG).show();
                                    tes_result.setText("");
                                    name.setText("");
                                    phone.setText("");
                                    dong.setText("");
                                    ho.setText("");
                                }
                                break;
                            }
                            case 1: { // 외부차량 등록
                                if(tes_result.getText().toString().equals("")) {
                                    Log.d(TAG, "onClick: 외부차량 번호를 입력하세요!!!!!");
                                    Toast.makeText(getApplicationContext(), "차량 번호를 입력하세요.", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    InsertData insertData = new InsertData();
                                    insertData.execute("http://" + IP_ADDRESS + "/chambit_vis_insert.php");
                                    Toast.makeText(getApplicationContext(), "방문자 차량이 등록되었습니다.", Toast.LENGTH_LONG).show();
                                    tes_result.setText("");
                                    name.setText("");
                                    phone.setText("");
                                    dong.setText("");
                                    ho.setText("");
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
                Intent intent = new Intent(getApplicationContext(), CarListActivity.class);
                startActivity(intent);
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
            String str_dong = dong.getText().toString() + "동";
            String str_ho = ho.getText().toString() + "호";

            String serverURL = strings[0];
            String postParameters = "car_no=" + str_car_no + "&name=" + str_name + "&phone=" + str_phone
                    + "&dong=" + str_dong + "&ho=" + str_ho;

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
                Log.d(TAG, "onClick: clicked");
                doFileUpload();
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

                roi_img = (ImageView)findViewById(R.id.roi_photo);

                roi_data = data.getByteArrayExtra("roi");
                image = BitmapFactory.decodeByteArray(roi_data,0,roi_data.length);
                roi_img.setImageBitmap(image);

                new AsyncTess().execute(image);
            }
        }
    }


    public void doFileUpload() {
        try {
            URL url = new URL("http://" + IP_ADDRESS + ":8080/photo");

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            // open connection
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoInput(true); //input 허용
            con.setDoOutput(true);  // output 허용
            con.setUseCaches(false);   // cache copy를 허용하지 않는다.
            con.setRequestMethod("POST");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            // write data
            DataOutputStream dos =
                    new DataOutputStream(con.getOutputStream());
            Log.i(TAG, "Open OutputStream" );
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            // 파일 전송시 파라메터명은 file1 파일명은 camera.jpg로 설정하여 전송
            dos.writeBytes("Content-Disposition: form-data; name=\"file1\";filename=\"camera.jpg\"" + lineEnd);

            dos.writeBytes(lineEnd);
            dos.write(roi_data,0,roi_data.length);
            Log.i(TAG, roi_data.length+"bytes written" );
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dos.flush(); // finish upload...

        } catch (Exception e) {
            Log.i(TAG, "exception " + e.getMessage());
            // TODO: handle exception
        }
        Log.i(TAG, roi_data.length+"bytes written successed ... finish!!" );
        try { dos.close(); } catch(Exception e){}


    }
}