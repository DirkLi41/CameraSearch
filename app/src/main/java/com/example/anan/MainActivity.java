package com.example.anan;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity {

    Spinner sp1;
    String[] engine = {"Google", "百度"};
    ArrayAdapter<String> engineList;
    ImageView iv1;
    String[] method = {"相機", "圖片庫"};
    static String IMAGE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        sp1 = (Spinner) findViewById(R.id.spinner);
        engineList = new ArrayAdapter<String>(
                MainActivity.this,
                R.layout.support_simple_spinner_dropdown_item,
                engine
        );
        sp1.setAdapter(engineList);

        iv1 = (ImageView) findViewById(R.id.imageView);

        int perCamera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int perRStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int perWStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (perCamera != PackageManager.PERMISSION_GRANTED |
                perRStorage != PackageManager.PERMISSION_GRANTED |
                perWStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );
        } else {

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0
                    && grantResults[0] == PERMISSION_GRANTED
                    && grantResults[1] == PERMISSION_GRANTED
                    && grantResults[2] == PERMISSION_GRANTED) {
                //取得權限，進行檔案存取

            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0
                );
            }
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("請選擇方式");
        builder.setItems(method, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(getExternalFilesDir("PHOTO"), "myphoto.jpg");
                        Uri photoURI = FileProvider.getUriForFile(
                                MainActivity.this,
                                MainActivity.this.getApplicationContext().getPackageName() + ".my.package.name.provider",
                                f);
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(camera, 1);
                        break;
                    case 1:
                        Intent gallery = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(gallery, 2);
                        break;
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
//                    Bundle extras = data.getExtras();
//                    Bitmap bmp = (Bitmap) extras.get("data");
//                    iv1.setImageBitmap(bmp);

                    File f = new File(getExternalFilesDir("PHOTO"), "myphoto.jpg");
                    Log.d("URI", "照片URI" + Uri.fromFile(f));
                    IMAGE_URL = Uri.fromFile(f).toString();
                    try {
                        InputStream is = new FileInputStream(f);
                        Log.d("BMP", "Can READ:" + is.available());
                        Bitmap bmp = getFitImage(is);
                        iv1.setImageBitmap(bmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Log.d("URI", "照片URI" + selectedImage);
                    IMAGE_URL = selectedImage.toString();
                    Bitmap bmp = null;
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    iv1.setImageBitmap(bmp);
                }
                break;
        }
    }

    public static Bitmap getFitImage(InputStream is) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        byte[] bytes = new byte[0];
        try {
            bytes = readStream(is);
            //BitmapFactory.decodeStream(inputStream, null, options);
            Log.d("BMP", "byte length:" + bytes.length);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            System.gc();
            // Log.d("BMP", "Size:" + bmp.getByteCount());
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public void clickEdit(View v) {

    }

    public void clickSearch(View v) {
        Thread thread = new Thread(mutiThread);
        thread.start();
        try {
            Log.d("ImgURL", String.valueOf(thread));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static String getImgurContent(String clientID) throws Exception {
        URL url;
        url = new URL("https://api.imgur.com/3/image");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        String data = URLEncoder.encode("image", "UTF-8") + "="
                + URLEncoder.encode(IMAGE_URL, "UTF-8");

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Client-ID " + clientID);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");

        conn.connect();
        StringBuilder stb = new StringBuilder();
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        // Get the response
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            stb.append(line).append("\n");
        }
        wr.close();
        rd.close();

        return stb.toString();
    }
    private Runnable mutiThread = new Runnable() {
        public void run() {
            try {
                getImgurContent("92b0cc32a395668");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
