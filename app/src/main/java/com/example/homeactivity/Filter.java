package com.example.homeactivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
    private Uri path;
    private int scalingfact;

    Filter(Uri path, int scalingfact) {
        this.path = path;
        this.scalingfact = scalingfact;
    }

    void setScalingfact(int scalingfact){
        this.scalingfact=scalingfact;
    }


    Uri getPic() {
        return path;
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
        out.writeString(path.toString());
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
        path = Uri.parse(in.readString());
        scalingfact = in.readInt();
    }

}
