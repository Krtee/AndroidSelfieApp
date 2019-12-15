package com.example.homeactivity;


import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class GraphicFaceTracker extends Tracker<Face> {
    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;

    public GraphicFaceTracker(GraphicOverlay overlay, FaceGraphic graphic){
        mFaceGraphic=graphic;
        mOverlay=overlay;
    }

    @Override
    public void onNewItem(int faceId, Face face) {
        mFaceGraphic.setId(faceId);
    }

    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults,
                         Face face) {
        mOverlay.add(mFaceGraphic);
        mFaceGraphic.updateFace(face);
    }

    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mFaceGraphic);
    }

    @Override
    public void onDone() {
        mOverlay.remove(mFaceGraphic);
    }
}
