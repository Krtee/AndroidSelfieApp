package com.example.homeactivity;


import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HelperTest2 {

    @Test
    public void resizeTest1(){
        Bitmap bm = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,1,1);
        int width=1;
        int height=1;
        Bitmap test = Helper.resize(width,height,bm,rect);
        assertEquals(width,test.getWidth());
        assertEquals(height,test.getHeight());
    }
    @Test
    public void resizeTest2(){
        Bitmap bm = Bitmap.createBitmap(2,2,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,1,1);
        int width=1;
        int height=1;
        Bitmap test = Helper.resize(width,height,bm,rect);
        assertEquals(width,test.getWidth());
        assertEquals(height,test.getHeight());
    }
    @Test
    public void resizeTest3(){
        Bitmap bm = Bitmap.createBitmap(1,2,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,1,1);
        int width=1;
        int height=1;
        Bitmap test = Helper.resize(width,height,bm,rect);
        assertEquals(width,test.getWidth());
        assertEquals(height,test.getHeight());
    }
    @Test
    public void resizeTest4(){
        Bitmap bm = Bitmap.createBitmap(1,2,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,1,1);
        int width=2;
        int height=1;
        Bitmap test = Helper.resize(width,height,bm,rect);
        assertEquals(width,test.getWidth());
        assertEquals(height,test.getHeight());
    }
    public void functionReturnsCorrectResult() {
        Bitmap bm = Bitmap.createBitmap(1,2,Bitmap.Config.ARGB_8888);
        Rect rect= new Rect(0,0,1,1);
        int width=-2;
        int height=1;
        Bitmap test = Helper.resize(width,height,bm,rect);
        assertEquals(width,test.getWidth());
        assertEquals(height,test.getHeight());
    }
}
