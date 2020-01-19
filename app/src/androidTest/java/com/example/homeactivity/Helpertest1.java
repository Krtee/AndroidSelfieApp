package com.example.homeactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Helpertest1 {
    @Test
    public void overlayTest_FirstisBigger(){
        Bitmap bmp1= Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,60,60);
        Bitmap fin= Helper.overlay(bmp1,bmp2,rect);
        assertEquals(bmp1.getHeight(),fin.getHeight());
        assertEquals(bmp1.getWidth(),fin.getWidth());
    }
    @Test
    public void overlayTest_SecondisBigger(){
        Bitmap bmp1= Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,60,60);
        Bitmap fin= Helper.overlay(bmp1,bmp2,rect);
        assertEquals(bmp1.getHeight(),fin.getHeight());
        assertEquals(bmp1.getWidth(),fin.getWidth());
    }
    @Test
    public void overlayTest_RectisSmaller(){
        Bitmap bmp1= Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,20,20);
        Bitmap fin= Helper.overlay(bmp1,bmp2,rect);
        assertEquals(bmp1.getHeight(),fin.getHeight());
        assertEquals(bmp1.getWidth(),fin.getWidth());
    }
    @Test
    public void overlayTest_RectisSmaller2(){
        Bitmap bmp1= Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,20,20);
        Bitmap fin= Helper.overlay(bmp1,bmp2,rect);
        assertEquals(bmp1.getHeight(),fin.getHeight());
        assertEquals(bmp1.getWidth(),fin.getWidth());
    }
    @Test
    public void overlayTest_RectisBigger(){
        Bitmap bmp1= Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,200,200);
        Bitmap fin= Helper.overlay(bmp1,bmp2,rect);
        assertEquals(bmp1.getHeight(),fin.getHeight());
        assertEquals(bmp1.getWidth(),fin.getWidth());
    }
}
