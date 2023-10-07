package com.example.near_ucustomer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class search_product extends AppCompatActivity {

    int finalNRead;
    ImageButton img;
    Button src_btn;
    EditText editText;
    Uri filepath;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();//might be erroneous
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        img = findViewById(R.id.search_product_img);
        src_btn = findViewById(R.id.search_img_btn);
        editText = findViewById(R.id.editTextText);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"Select Image File"),1);

            }
        });
    src_btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {

                new search_thread().start_send_img();
            }
            catch (Exception e){
                Toast.makeText(search_product.this, "btn:"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==1 && resultCode==RESULT_OK){
            assert data != null;
            filepath = data.getData();
            try {
                InputStream inputStream=getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                img.setImageBitmap(bitmap);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);

            } catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    class search_thread implements Runnable{
        Socket client;
        InputStream in;
        OutputStream out;
        byte [] data ;
        @Override
        public void run() {
            try {
                client = new Socket("192.168.1.10",5555);
                in = client.getInputStream();
                out = client.getOutputStream();
                data = new byte[1024];

                out.write("search_image".getBytes(StandardCharsets.UTF_8));
                in.read();
                out.write(byteArrayOutputStream.toByteArray());
                out.write("<END>".getBytes(StandardCharsets.UTF_8));

                //uri1
                int nRead =0;
                nRead = in.read(data);
                String  uri1 = new String(data,0,nRead);
                out.write("1".getBytes(StandardCharsets.UTF_8));
                //uri2
                nRead = in.read(data,0,data.length);
                String  uri2 = new String(data,0,nRead);
                out.write("1".getBytes(StandardCharsets.UTF_8));
                //uri3
                nRead = in.read(data,0,data.length);
                String  uri3 = new String(data,0,nRead);
                out.write("1".getBytes(StandardCharsets.UTF_8));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editText.setText(uri1);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(search_product.this, "th:"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        void start_send_img(){
            run();
        }
    }
}