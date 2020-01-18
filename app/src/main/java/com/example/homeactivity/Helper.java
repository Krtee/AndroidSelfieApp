package com.example.homeactivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class Helper {
    private Uri fileURL;

    static Bitmap rotateandflipBitmap(File file, float angle) throws IOException {
        Bitmap source;
        source = BitmapFactory.decodeFile(file.getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return rotated;

    }
    static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, Rect rect) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, null,rect, null);
        return bmOverlay;
    }

    static Bitmap resize(int width,int height, Bitmap bmp2, Rect rect) {
        Bitmap bmOverlay = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp2, null,rect, null);
        return bmOverlay;
    }


    static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    static File BitmaptoFile(Bitmap map, File file) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        map.compress(Bitmap.CompressFormat.JPEG, 90 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return file;
    }

    Bitmap createPic(Context context,Bitmap pic,FaceOverlay view, Rect frame,Rect rect, File file){
        Bitmap filter = loadBitmapFromView(view);
        Bitmap filter2= resize(frame.width(),frame.height(),filter,rect);
        Bitmap newPic = Helper.overlay(pic, filter2 ,frame);

        try{
            File newfile = Helper.BitmaptoFile(newPic,file);
        }
        catch (IOException e){
            Toast.makeText(context,"Couldn#t put filter on Pic.",Toast.LENGTH_LONG).show();
        }

        fileURL = insertImage(context.getContentResolver(),newPic,file.getName(),file.toString());
        return newPic;
    }


    static List<Filter> getAllFilters(Context context){
        List<Filter> filters = new ArrayList<>();
        try {
            Resources res = context.getResources();
            InputStreamReader in_s = new InputStreamReader(res.openRawResource(R.raw.pics));

            BufferedReader reader= new BufferedReader(in_s);
            String line;
            String[] infos;

            while((line=reader.readLine())!=null &&line.length()!=0){
                infos=line.split(",");
                int resID =context.getResources().getIdentifier(infos[0],"drawable",context.getPackageName());
                try{
                    filters.add(new Filter(resID,Integer.parseInt(infos[1])));
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return filters;
    }


    static Uri insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }


        return url;
    }

    Uri getFileURL() {
        return fileURL;
    }
}
