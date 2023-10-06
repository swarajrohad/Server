package com.example.near_ushopkeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class add_product extends AppCompatActivity {

    Button uplaod_product_btn;
    ImageButton img_upload_btn;
    Uri filepath;
    Bitmap bitmap;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte [] file_bytes;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference root;
    Task<Void> root2;
    Task<Void> root3;
    FirebaseStorage storage;
    StorageReference uploader;
    String uid;
    TextView product_name,product_price,product_catogory,pro_desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        img_upload_btn = findViewById(R.id.upload_product_img);
        uplaod_product_btn = findViewById(R.id.upload_product_details);


        img_upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"Select Image File"),1);

            }
        });

        uplaod_product_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload_product();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==1 && resultCode==RESULT_OK){
            filepath = data.getData();
            try {
                InputStream inputStream=getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                img_upload_btn.setImageBitmap(bitmap);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                file_bytes = byteArrayOutputStream.toByteArray();
            } catch (Exception e) {
                Toast.makeText(this, "image"+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void upload_product() {

        product_name = findViewById(R.id.product_name);
        product_price = findViewById(R.id.product_price);
        product_catogory = findViewById(R.id.product_catogory);
        pro_desc = findViewById(R.id.product_desc);

         storage = FirebaseStorage.getInstance();
         uploader = storage.getReference();

        String tproduct_name = product_name.getText().toString().trim();
        String tproduct_price = product_price.getText().toString().trim();
        String tproduct_catogory = product_catogory.getText().toString().trim();
        String tproduct_desc = pro_desc.getText().toString().trim();

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("File Uploader");
        dialog.show();


        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        uploader.child("All Products/").child(tproduct_catogory+"/").child(uid).child(tproduct_name).putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                uploader.child("All Products/").child(tproduct_catogory+"/").child(uid).child(tproduct_name).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        String uri = task.getResult().toString();

                        db = FirebaseDatabase.getInstance();
                        root = db.getReference("Shop_keepers");

                        auth = FirebaseAuth.getInstance();
//                         uid = auth.getCurrentUser().getUid();

                        product_data data = new product_data(tproduct_name,tproduct_price,tproduct_catogory,tproduct_desc,uri);

                        root.child(auth.getCurrentUser().getUid()).child("Product_data").child("Category").child(data.product_catogory).child(data.product_name).setValue(data);
                        root2 = FirebaseDatabase.getInstance().getReference().child("All_products_data").child(auth.getCurrentUser().getUid()).child(data.product_name).setValue(data);
                        root2 = FirebaseDatabase.getInstance().getReference().child("All_products_display").child(data.product_name).setValue(data);

                        product_name.setText("");
                        product_price.setText("");
                        product_catogory.setText("");
                        pro_desc.setText("");
                        img_upload_btn.setImageResource(R.drawable.uplaod_product);
                        dialog.dismiss();

                        try {
                            new serverthread().send(uri);
                        }catch (Exception e){
                            Toast.makeText(add_product.this, "here"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(getApplicationContext(),"Uploaded",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        float percent = (100* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        dialog.setMessage("Uploaded : "+(int)percent+" %");
                    }
                });

    }
    class serverthread implements Runnable{
        Socket client;
        OutputStream out;
        InputStream in;
        String uri;
        @Override
        public void run() {
            try {
                client = new Socket("192.168.1.8",5555);
                out = client.getOutputStream();
                in = client.getInputStream();
                out.write("upload_product".getBytes(StandardCharsets.UTF_8));
                in.read();
                out.write(uri.getBytes(StandardCharsets.UTF_8));
                in.read();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(add_product.this, "connection:"+ String.valueOf(file_bytes.length), Toast.LENGTH_LONG).show();
                    }
                });
                out.write(file_bytes);
                out.write("<END>".getBytes(StandardCharsets.UTF_8));
                out.flush();
                out.close();
                client.close();
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(add_product.this, "erur"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        public void send(String uri){
            this.uri = uri;
            run();
        }
    }
}