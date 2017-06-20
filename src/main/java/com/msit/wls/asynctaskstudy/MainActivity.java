package com.msit.wls.asynctaskstudy;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wls.permissions.tool.PermissionTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.get_pic_btn)
    Button getPicBtn;
    @InjectView(R.id.iv)
    ImageView iv;
    @InjectView(R.id.pb)
    ProgressBar pb;
    @InjectView(R.id.upload_btn)
    Button uploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        PermissionTool.getInstance()
                .with(this)
                .requestpermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callBack(call)
                .requestCode(0x123)
                .start();
    }

    private PermissionTool.CallBack call = new PermissionTool.CallBack() {
        @Override
        public void onSuccess(int i, List<String> list) {
            Toast.makeText(MainActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFail(int i, List<String> list) {
            Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
        }
    };

    @OnClick({R.id.get_pic_btn, R.id.upload_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.get_pic_btn:
                String urlpath = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3985607856,3414601056&fm=26&gp=0.jpg";
                new MyAsy5().execute(urlpath);
                break;
            case R.id.upload_btn:
                String url = "http://182.254.146.123:8080/index.jsp";
                new UploadAsy5().execute(url);
                break;
        }
    }

    //文件下载
    private class UploadAsy5 extends AsyncTask<String,Integer,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200){
                    //获取文件的长度
                    int length = conn.getContentLength();

                    InputStream is = conn.getInputStream();
                    File file = new File
                            (hasSdCard(),params[0].substring(params[0].lastIndexOf("/")+1));
                    FileOutputStream fos = new FileOutputStream(file);
                    int sum = 0;//累加每次下载的长度
                    //边读边写
                    int len;
                    byte[] b = new byte[1024];
                    while((len = is.read(b)) !=-1){
                        fos.write(b,0,len);
                        sum+=len;
                        //更新进度条
                        publishProgress((int)((sum*1.0/length)*100));
                    }
                    //关闭流
                    fos.close();
                    is.close();
                    conn.disconnect();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
            pb.setVisibility(View.GONE);
        }
    }

    //获取图片的
    private class MyAsy5 extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream is = conn.getInputStream();
                     bitmap = BitmapFactory.decodeStream(is);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            publishProgress();
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null)
                iv.setImageBitmap(bitmap);

        }
    }

    //获取网络图片
    private class MyAsy extends AsyncTask<String, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection =
                        (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = httpURLConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null)
                iv.setImageBitmap(bitmap);
        }
    }

    //下载
    private class uplaodAsy extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);//显示进度条
        }

        @Override
        protected Void doInBackground(String... params) {
            //执行下载
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                if (connection.getResponseCode() == 200){
                    InputStream inputStream = connection.getInputStream();
                    //获取下载文件的总长度
                    int length = connection.getContentLength();
                    //获取文件的输出对象
                    File file = new File
                            (hasSdCard(),params[0].substring(params[0].lastIndexOf("/")+1));
                    if (file.exists()) file.delete();
                    //获取文件的输出流
                    FileOutputStream fos = new FileOutputStream(file);
                    int len;
                    byte[] bytes = new byte[1024];
                    int sum = 0;//用于接收已下载的总长度
                    //边读边写
                    while((len = inputStream.read(bytes))!=-1){
                        fos.write(bytes,0,len);
                        sum += len;
                        publishProgress((int)((sum*1.0/length)*100));

                    }
                    fos.close();
                    inputStream.close();
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setProgress(values[0]);//设置进度条
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
            pb.setVisibility(View.GONE);
        }
    }




    //判断sd卡是否存在并放回sd卡的根目录
    private File hasSdCard(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
           return Environment.getExternalStorageDirectory();
        }
        else{
            return Environment.getRootDirectory();
        }
    }

}
