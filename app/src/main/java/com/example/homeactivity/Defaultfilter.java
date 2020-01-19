package com.example.homeactivity;

public class Defaultfilter {
    private int drawable;
    private int scalingfact;

    Defaultfilter(int drawable, int scalingfact) {
        this.drawable = drawable;
        this.scalingfact = scalingfact;
    }


    int getDrawable() {
        return drawable;
    }

    int getScalingfact() {
        return scalingfact;
    }
}
