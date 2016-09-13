package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageShow extends AppCompatActivity {

    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageshow);
        image=(ImageView)findViewById(R.id.image);
        //Toast.makeText(getApplicationContext(), "InImageShow", Toast.LENGTH_SHORT).show();
        Bundle bundle = this.getIntent().getExtras();
         /*获取Bundle中的数据，注意类型和key*/
        byte[] data = bundle.getByteArray("data");
        image.setImageBitmap(Bytes2Bimap(data));
    }
    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

}
