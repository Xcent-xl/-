package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import android.widget.ImageButton;
import android.widget.Toast;

import SQLite.DBManager;
public class Image extends AppCompatActivity {

    ImageButton imgPic,imgAcc;
    private String name="";
    private DBManager mgr;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private static final int CAPTURE_ACCEPT_ACTIVITY_REQUEST_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image);
        imgPic = (ImageButton) findViewById(R.id.picture);
        imgAcc = (ImageButton) findViewById(R.id.accept);
        mgr = new DBManager(this);
        Bundle bundle = this.getIntent().getExtras();
        name = bundle.getString("name");
        showImageButton();
        imgPic.setOnClickListener(new View.OnClickListener() {
            @Override
            //在当前onClick方法中监听点击Button的动作
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Image.this, ImageShow.class);
                intent.putExtra("data", mgr.select(name).getMimage());
                startActivity(intent);
            }
        });

        imgAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            //在当前onClick方法中监听点击Button的动作
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Image.this, ImageShow.class);
                intent.putExtra("data", mgr.select(name).getMaccept());
                startActivity(intent);
            }
        });
    }
    public void pic(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    public void acc(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,CAPTURE_ACCEPT_ACTIVITY_REQUEST_CODE);
    }


    private void updateImage(byte[] data) {mgr.updateImage(name,data);}
    private void updateAccept(byte[] data) {mgr.updateAccept(name,data);}
    private byte[] getPicData(){return mgr.select(name).getMimage();}
    private byte[] getAccData(){return mgr.select(name).getMaccept();}
    private void showImageButton()
    {
        ByteArrayInputStream stream=null;
        if(!(getPicData()==null)) {
            //Toast.makeText(getApplicationContext(), "getPicData()TRUE", Toast.LENGTH_SHORT).show();
            stream = new ByteArrayInputStream(getPicData());
            imgPic.setImageDrawable(Drawable.createFromStream(stream, "img"));
        }
        if(!(getAccData()==null)) {
            //Toast.makeText(getApplicationContext(), "getAccData()TRUE", Toast.LENGTH_SHORT).show();
            stream = new ByteArrayInputStream(getAccData());
            imgAcc.setImageDrawable(Drawable.createFromStream(stream, "img"));
        }
    }
    public byte[] Bitmap2Bytes(Bitmap bm) {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
             return baos.toByteArray();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回数据
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
               //Toast.makeText(getApplicationContext(), "updateImage", Toast.LENGTH_SHORT).show();
                if(data==null) Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                else {
                    //Toast.makeText(getApplicationContext(), "TRUE", Toast.LENGTH_SHORT).show();
                    updateImage(Bitmap2Bytes((Bitmap) data.getExtras().get("data")));
                    imgPic.setImageBitmap((Bitmap) data.getExtras().get("data"));
                }
            }
            if (requestCode == CAPTURE_ACCEPT_ACTIVITY_REQUEST_CODE) {
                if(data==null) Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                else {
                    //Toast.makeText(getApplicationContext(), "TRUE", Toast.LENGTH_SHORT).show();
                    updateAccept(Bitmap2Bytes((Bitmap) data.getExtras().get("data")));
                    imgAcc.setImageBitmap((Bitmap) data.getExtras().get("data"));
                }
            }
        }
    }

}
