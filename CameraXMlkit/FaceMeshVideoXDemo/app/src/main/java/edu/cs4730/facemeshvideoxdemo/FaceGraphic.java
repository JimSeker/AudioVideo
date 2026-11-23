package edu.cs4730.facemeshvideoxdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.common.Triangle;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import java.util.List;

public class FaceGraphic extends View {

    private static Paint myColor;

    private static final float STROKE_WIDTH = 5.0f;
    static final String TAG = "FaceGraphic";

    private List<FaceMesh> faces;

    private final Rect dest = new Rect();
    private Context context;
    private CameraSelector lensFacing;


    public FaceGraphic(Context context) {
        super(context);
        this.context = context;
        setup();
    }

    public FaceGraphic(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setup();

    }

    public FaceGraphic(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setup();

    }

    public void setFaces(List<FaceMesh> faces, CameraSelector lensFacing) {
        this.faces = faces;
        this.lensFacing = lensFacing;
        invalidate();
    }

    private void setup() {

        if (myColor == null) {
            myColor = new Paint();
            myColor.setColor(Color.WHITE);
            myColor.setStyle(Paint.Style.STROKE);
            myColor.setStrokeWidth(10);
            myColor.setTextSize(myColor.getTextSize() * 10);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (faces == null) return;

        for (FaceMesh face : faces) {
            Rect bounds = face.getBoundingBox();
            canvas.drawRect(bounds, myColor);
            // Gets all points
//                        List<FaceMeshPoint> faceMeshpoints = face.getAllPoints();
//                        for (FaceMeshPoint faceMeshpoint : faceMeshpoints) {
//                            int index = faceMeshpoint.getIndex();
//                            PointF3D position = faceMeshpoint.getPosition();
//                        }
            //Log.d(TAG, "We have something " + face.size());
            // Gets triangle info
            List<Triangle<FaceMeshPoint>> triangles = face.getAllTriangles();
            for (Triangle<FaceMeshPoint> triangle : triangles) {
                // 3 Points connecting to each other and representing a triangle area.
                List<FaceMeshPoint> faceMeshPoints = triangle.getAllPoints();
                PointF3D point1 = faceMeshPoints.get(0).getPosition();
                PointF3D point2 = faceMeshPoints.get(1).getPosition();
                PointF3D point3 = faceMeshPoints.get(2).getPosition();

                canvas.drawLine(point1.getX(), point1.getY(), point2.getX(), point2.getY(), myColor);
                canvas.drawLine(point2.getX(), point2.getY(), point3.getX(), point3.getY(), myColor);
                canvas.drawLine(point3.getX(), point3.getY(), point1.getX(), point1.getY(), myColor);

            }
        }


    }

}
