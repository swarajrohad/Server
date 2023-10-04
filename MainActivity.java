package com.trial.nearu;
//package com.example.myapplication;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    Bitmap bitmap;
    String ip;
    volatile String str;
    int a=0;
    ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
    Button button;
    TextView textView;
    EditText editText;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        ActivityCompat.requestPermissions(this,
//                new String[]{READ_MEDIA_IMAGES, WRITE_EXTERNAL_STORAGE},
//                PackageManager.PERMISSION_GRANTED);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editTextText);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = editText.getText().toString();
                if (bitmap != null && ip!=null) {
                    try {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        new Thread(new javaclient()).start();
                    }
                    catch (Exception e){
                        Toast.makeText(MainActivity.this, "inbutton "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Select image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ActivityResultLauncher<Intent> activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            Uri uri = data.getData();
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                                imageView.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });

    }

    private class javaclient implements Runnable {
        private Socket client;
        private InputStream is;
        private OutputStream out;
        private boolean done;

        @Override
        public void run() {
            try {

                byte [] data = new byte [1024];
                int nRead;
                client = new Socket(ip, 5555);
                out = client.getOutputStream();
                is = new BufferedInputStream(client.getInputStream());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                out.write("sending image".getBytes(StandardCharsets.UTF_8));
//                out.write("some bytes which is the image bytes".getBytes(StandardCharsets.UTF_8));
                out.write(byteArrayOutputStream.toByteArray());
                out.write("<END>".getBytes(StandardCharsets.UTF_8));

//product_id
                nRead = is.read(data,0,data.length);
                buffer.write(data, 0, nRead);
                String product_id = new String(buffer.toByteArray());
//                System.out.println(product_id);
                out.write("product id receieved".getBytes());

                buffer.close();
                buffer = new ByteArrayOutputStream();

//seller id
                nRead = is.read(data,0,data.length);
                buffer.write(data, 0, nRead);
                String seller_id = new String(buffer.toByteArray());
//                System.out.println(seller_id);
                out.write("product name receieved".getBytes());

                buffer.flush();
                buffer.close();
                buffer = new ByteArrayOutputStream( );

//image size
                nRead = is.read(data,0,data.length);
                buffer.write(data, 0, nRead);
                String img_size = new String(buffer.toByteArray());
                System.out.println(img_size);
                out.write(img_size.getBytes());

                buffer.reset();

//image
                Thread.sleep(1000);
                while(true){
                    if(is.available()>1024){

                        nRead = is.read(data, 0, data.length);
                        buffer.write(data, 0, nRead);
                    }
                    else{
                        nRead = is.read(data, 0, is.available());
                        buffer.write(data, 0, data.length);
                        break;
                    }
                }

                byte [] filedata = buffer.toByteArray();
                str = String.valueOf(filedata.length);
                out.write("image receieved successfully".getBytes());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                        textView.setText(str);
                        Bitmap bmp = BitmapFactory.decodeByteArray(filedata, 0, filedata.length);
                        imageView.setImageBitmap(bmp);
                    }
                });
                client.close();
            }catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Error :"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }
}