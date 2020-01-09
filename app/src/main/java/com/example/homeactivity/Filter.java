package com.example.homeactivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Filter {
    private int drawable;
    private int scalingfact;

    public Filter(int drawable, int scalingfact) {
        this.drawable = drawable;
        this.scalingfact = scalingfact;
    }

    public static List<Filter> getAllFilters(Context context){
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
                    filters.add(new Filter(resID,Integer.getInteger(infos[1])));
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

    public int getDrawable() {
        return drawable;
    }

    public int getScalingfact() {
        return scalingfact;
    }

}
