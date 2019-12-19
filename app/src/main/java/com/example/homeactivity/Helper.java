package com.example.homeactivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Helper {

    public static Bitmap rotateandflipBitmap(File file, float angle) throws IOException {
        Bitmap source;
        source = BitmapFactory.decodeFile(file.getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return rotated;

    }

}
