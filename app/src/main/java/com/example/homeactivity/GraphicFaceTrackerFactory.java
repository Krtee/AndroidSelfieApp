package com.example.homeactivity;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class GraphicFaceTrackerFactory  implements MultiProcessor.Factory<Face> {
    private GraphicOverlay mGraphicOverlay;

    @Override
    public Tracker<Face> create(Face face) {
        FaceGraphic graphic = new FaceGraphic(mGraphicOverlay);
        return new GraphicFaceTracker(mGraphicOverlay,graphic);
    }
}
