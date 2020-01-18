package com.example.homeactivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Filter implements Parcelable {
    private int drawable;
    private int scalingfact;

    Filter(int drawable, int scalingfact) {
        this.drawable = drawable;
        this.scalingfact = scalingfact;
    }


    int getDrawable() {
        return drawable;
    }

    int getScalingfact() {
        return scalingfact;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(drawable);
        out.writeInt(scalingfact);
    }

    public static final Parcelable.Creator<Filter> CREATOR = new Parcelable.Creator<Filter>() {
        public Filter createFromParcel(Parcel in) {
            return new Filter(in);
        }

        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

    private Filter(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        drawable = in.readInt();
        scalingfact = in.readInt();
    }

}
