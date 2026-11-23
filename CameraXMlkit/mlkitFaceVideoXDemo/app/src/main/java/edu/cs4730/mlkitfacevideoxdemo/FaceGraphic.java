package edu.cs4730.mlkitfacevideoxdemo;

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

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.List;

public class FaceGraphic extends View {


    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 80.0f;
    private static final float ID_X_OFFSET = -70.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static Paint facePositionPaint;
    private static Paint idPaint;
    private static Paint boxPaint;
    private static Paint RedPaint;
    private static Paint GreenPaint;

    private static final float STROKE_WIDTH = 5.0f;
    static final String TAG = "FaceGraphic";
    private List<Face> faces;

    private static Bitmap hatBMP;
    private static Rect hatBox;
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
      public void setFaces(List<Face> faces, CameraSelector lensFacing) {
        this.faces = faces;
        this.lensFacing = lensFacing;
        invalidate();
    }

    private void setup() {

        if (facePositionPaint == null) {
            final int selectedColor = Color.WHITE;

            facePositionPaint = new Paint();
            facePositionPaint.setColor(selectedColor);

            idPaint = new Paint();
            idPaint.setColor(selectedColor);
            idPaint.setTextSize(ID_TEXT_SIZE);

            boxPaint = new Paint();
            boxPaint.setColor(selectedColor);
            boxPaint.setStyle(Paint.Style.STROKE);
            boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

            GreenPaint = new Paint();
            GreenPaint.setColor(Color.GREEN);
            GreenPaint.setStyle(Paint.Style.STROKE);
            GreenPaint.setStrokeWidth(STROKE_WIDTH);

            RedPaint = new Paint();
            RedPaint.setColor(Color.RED);
            RedPaint.setStyle(Paint.Style.STROKE);
            RedPaint.setStrokeWidth(STROKE_WIDTH);

            hatBMP = BitmapFactory.decodeResource(context.getResources(), R.drawable.partyhat2);
            hatBox = new Rect(0, 0, hatBMP.getWidth(), hatBMP.getHeight());
        }
    }

    //For landmarks use this code constants.
        /* FaceLandmark.MOUTH_BOTTOM,
            FaceLandmark.MOUTH_RIGHT,
            FaceLandmark.MOUTH_LEFT,
            FaceLandmark.RIGHT_EYE,
            FaceLandmark.LEFT_EYE,
            FaceLandmark.RIGHT_EAR,
            FaceLandmark.LEFT_EAR,
            FaceLandmark.RIGHT_CHEEK,
            FaceLandmark.LEFT_CHEEK,
            FaceLandmark.NOSE_BASE
         */

    public int correctLeft(int faceLandmark) {
        if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            return faceLandmark;
        } else {
            return switch (faceLandmark) {
                case FaceLandmark.LEFT_EYE -> FaceLandmark.RIGHT_EYE;
                case FaceLandmark.RIGHT_EYE -> FaceLandmark.LEFT_EYE;
                case FaceLandmark.LEFT_EAR -> FaceLandmark.RIGHT_EAR;
                case FaceLandmark.RIGHT_EAR -> FaceLandmark.LEFT_EAR;
                case FaceLandmark.LEFT_CHEEK -> FaceLandmark.RIGHT_CHEEK;
                case FaceLandmark.RIGHT_CHEEK -> FaceLandmark.LEFT_CHEEK;
                default -> faceLandmark;
            };
        }
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (faces == null) return;

        for (Face face : faces) {
            // Draws a circle at the position of the detected face, with the face's track id below.
            float x = face.getBoundingBox().centerX();
            float y = face.getBoundingBox().centerY();
            canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
            canvas.drawText("id: " + face.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);

            // Draws a bounding box around the face.
            float xOffset = face.getBoundingBox().width() / 2.0f;
            float yOffset = face.getBoundingBox().height() / 2.0f;
            float left = x - xOffset;
            float top = y - yOffset;
            float right = x + xOffset;
            float bottom = y + yOffset;
            canvas.drawRect(left, top, right, bottom, boxPaint);

            float cx, cy;
            float lx = 0f, ly = 0f, mx = 0, my = 0, rx = 0, ry = 0;
            int xl = 10;  //how big the x is.

            FaceLandmark leftEye = face.getLandmark(correctLeft(FaceLandmark.LEFT_EYE));
            if (leftEye != null && leftEye.getPosition() != null) {
                //        Log.d(TAG, "LEFT EYE");
                cx = leftEye.getPosition().x;
                cy = leftEye.getPosition().y;
                if (face.getLeftEyeOpenProbability() != null) {
                    if (face.getLeftEyeOpenProbability() > .75) {  //open, so show circle
                        canvas.drawCircle(cx, cy, 10, GreenPaint);
                    } else { //closed, so show x.
                        canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                        canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                    }
                }
            }

            FaceLandmark rightEye = face.getLandmark(correctLeft(FaceLandmark.RIGHT_EYE));
            if (rightEye != null && rightEye.getPosition() != null) {
                // Log.d(TAG, "RIGHT EYE");
                cx = rightEye.getPosition().x;
                cy = rightEye.getPosition().y;
                if (face.getRightEyeOpenProbability() != null) {
                    if (face.getRightEyeOpenProbability() > .75) {  //open, so show circle
                        canvas.drawCircle(cx, cy, 10, GreenPaint);
                    } else { //closed, so show x.
                        canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                        canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                    }
                }
            }

            FaceLandmark leftmouth = face.getLandmark(correctLeft(FaceLandmark.MOUTH_LEFT));
            FaceLandmark rightmouth = face.getLandmark(correctLeft(FaceLandmark.MOUTH_RIGHT));
            FaceLandmark bottommouth = face.getLandmark(correctLeft(FaceLandmark.MOUTH_BOTTOM));
            if (leftmouth != null && rightmouth != null && bottommouth != null &&
                leftmouth.getPosition() != null && rightmouth.getPosition() != null && bottommouth.getPosition() != null) {
                //     Log.d(TAG, "LEFT MOUTH");
                lx = leftmouth.getPosition().x;
                ly = leftmouth.getPosition().y;

                //      Log.d(TAG, "RIGHT MOUTH");
                rx = rightmouth.getPosition().x;
                ry = rightmouth.getPosition().y;

                //      Log.d(TAG, "BOTTOM MOUTH");
                mx = bottommouth.getPosition().x;
                my = bottommouth.getPosition().y;


                //now draw the mouth.   First check if all points exists
                if (lx > 0.0f && rx > 0.0f && mx > 0.0f) {
                    //so first if one side of the mouth is higher, use that one.
                    //          Log.v(TAG, "Drawing mouth");
                    if (ly < ry)  //left side is higher
                        ry = ly;
                    else
                        ly = ry;
                    //okay, the points exist, so lets draw
                    if (face.getSmilingProbability() != null) {
                        if (face.getSmilingProbability() > .75) { //smiling draw rec
                            canvas.drawRect(lx, ly, rx, my, GreenPaint);
                        } else {  //no smiling draw x
                            canvas.drawLine(lx, ly, rx, my, RedPaint);
                            canvas.drawLine(lx, my, rx, ry, RedPaint);
                        }
                    }
                }
            }



            FaceLandmark rightear = face.getLandmark(correctLeft(FaceLandmark.RIGHT_EAR));
            if (rightear != null && rightear.getPosition() != null) {
                canvas.drawCircle(
                    rightear.getPosition().x,
                    rightear.getPosition().y,
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
            }
            FaceLandmark leftear = face.getLandmark(correctLeft(FaceLandmark.LEFT_EAR));
            if (leftear != null && leftear.getPosition() != null) {
                canvas.drawCircle(
                    leftear.getPosition().x,
                    leftear.getPosition().y,
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
            }

            FaceLandmark leftCheek = face.getLandmark(correctLeft(FaceLandmark.LEFT_CHEEK));
            if (leftCheek != null && leftCheek.getPosition() != null) {
                canvas.drawCircle(
                    leftCheek.getPosition().x,
                    leftCheek.getPosition().y,
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
            }
            FaceLandmark rightCheek = face.getLandmark(correctLeft(FaceLandmark.RIGHT_CHEEK));
            if (rightCheek != null && rightCheek.getPosition() != null) {
                canvas.drawCircle(
                    rightCheek.getPosition().x,
                    rightCheek.getPosition().y,
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
            }

            //draw a hat
            if (leftear != null && rightear != null && hatBMP != null) {
              // Log.e(TAG, "drawing hat!");
                float scale = (rightear.getPosition().x - leftear.getPosition().x) / hatBox.width();

                dest.left = (int) leftear.getPosition().x;
                dest.right = (int) rightear.getPosition().x;

                dest.bottom = (int) (rightear.getPosition().y - (100 * scale));
                dest.top = (int) ((leftear.getPosition().y - (100 * scale)) - hatBox.height() * scale);
//                Log.e(TAG, "dest.top: " + dest.top);
//                Log.e(TAG, "dest.bottom: " + dest.bottom);
//                Log.e(TAG, "dest.left: " + dest.left);
//                Log.e(TAG, "dest.right: " + dest.right);
//                Log.e(TAG, "dest.width: " + dest.width());
//                Log.e(TAG, "dest.height: " + dest.height());
//                Log.e(TAG, "Scale: " + scale);
                canvas.drawBitmap(hatBMP, hatBox, dest, facePositionPaint);
            }
        }
    }

}
