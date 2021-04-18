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
import android.widget.ImageView;
import android.widget.TextView;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.CAMERA;


public class MainActivity extends AppCompatActivity {
    String TAG = "toy";

    ImageView roi_img;
    Bitmap image;
    TextView tes_result;

    static TessBaseAPI sTess;
    String lang;
    String datapath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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

    private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {

        @Override
        protected String doInBackground(Bitmap... mRelativeParams) {
            //Tesseract OCR 수행
            sTess.setImage(image);

            return sTess.getUTF8Text();
        }

        protected void onPostExecute(String result) {
            //완료 후 버튼 속성 변경 및 결과 출력
            tes_result = (TextView)findViewById(R.id.tess_result);
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

                byte[] arr = data.getByteArrayExtra("roi");
                image = BitmapFactory.decodeByteArray(arr,0,arr.length);
                roi_img.setImageBitmap(image);

                new AsyncTess().execute(image);
            }
        }
    }
}