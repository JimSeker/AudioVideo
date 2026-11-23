/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cs4730.posevideoxdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;
import java.util.Locale;

/**
 * Draw the detected pose in preview.
 */
public class PoseGraphic extends View {

    private static final float DOT_RADIUS = 8.0f;
    private static final float IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f;
    private boolean showInFrameLikelihood = true;
    private Pose pose;
    private Paint leftPaint;
    private Paint rightPaint;
    private Paint whitePaint;
    private CameraSelector lensFacing;

    public PoseGraphic(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    public PoseGraphic(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public PoseGraphic(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public PoseGraphic(Context context) {
        super(context);
        setup();
    }

    public void setPose(Pose pose, CameraSelector lensFacing, boolean showInFrameLikelihood) {
        this.pose = pose;
        this.showInFrameLikelihood = showInFrameLikelihood;
        this.lensFacing = lensFacing;
        invalidate();
    }


    /**
     * Sets up the pose graphic.
     */
    public void setup() {
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(IN_FRAME_LIKELIHOOD_TEXT_SIZE);
        leftPaint = new Paint();
        leftPaint.setColor(Color.GREEN);
        leftPaint.setStrokeWidth(5);
        rightPaint = new Paint();
        rightPaint.setColor(Color.YELLOW);
        rightPaint.setStrokeWidth(5);
    }


    /*
    * there has to be a better way to do this, but the AI does have any better ideas than me.
    * so correctleft is "temp" fix.
     */
    public int correctLeft(int poseLandmark) {
        if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            return poseLandmark;
        } else {
            return switch (poseLandmark) {
                case PoseLandmark.RIGHT_SHOULDER -> PoseLandmark.LEFT_SHOULDER;
                case PoseLandmark.LEFT_SHOULDER -> PoseLandmark.RIGHT_SHOULDER;
                case PoseLandmark.RIGHT_ELBOW -> PoseLandmark.LEFT_ELBOW;
                case PoseLandmark.LEFT_ELBOW -> PoseLandmark.RIGHT_ELBOW;
                case PoseLandmark.LEFT_WRIST -> PoseLandmark.RIGHT_WRIST;
                case PoseLandmark.RIGHT_WRIST -> PoseLandmark.LEFT_WRIST;
                case PoseLandmark.LEFT_HIP -> PoseLandmark.RIGHT_HIP;
                case PoseLandmark.RIGHT_HIP -> PoseLandmark.LEFT_HIP;
                case PoseLandmark.LEFT_KNEE -> PoseLandmark.RIGHT_KNEE;
                case PoseLandmark.RIGHT_KNEE -> PoseLandmark.LEFT_KNEE;
                case PoseLandmark.RIGHT_ANKLE -> PoseLandmark.LEFT_ANKLE;
                case PoseLandmark.LEFT_ANKLE -> PoseLandmark.RIGHT_ANKLE;
                case PoseLandmark.LEFT_PINKY -> PoseLandmark.RIGHT_PINKY;
                case PoseLandmark.RIGHT_PINKY -> PoseLandmark.LEFT_PINKY;
                case PoseLandmark.LEFT_INDEX -> PoseLandmark.RIGHT_INDEX;
                case PoseLandmark.RIGHT_INDEX -> PoseLandmark.LEFT_INDEX;
                case PoseLandmark.LEFT_THUMB -> PoseLandmark.RIGHT_THUMB;
                case PoseLandmark.RIGHT_THUMB -> PoseLandmark.LEFT_THUMB;
                case PoseLandmark.LEFT_HEEL -> PoseLandmark.RIGHT_HEEL;
                case PoseLandmark.RIGHT_HEEL -> PoseLandmark.LEFT_HEEL;
                case PoseLandmark.LEFT_FOOT_INDEX -> PoseLandmark.RIGHT_FOOT_INDEX;
                case PoseLandmark.RIGHT_FOOT_INDEX -> PoseLandmark.LEFT_FOOT_INDEX;
                case PoseLandmark.LEFT_EYE_INNER -> PoseLandmark.RIGHT_EYE_INNER;
                case PoseLandmark.RIGHT_EYE_INNER -> PoseLandmark.LEFT_EYE_INNER;
                case PoseLandmark.LEFT_EYE -> PoseLandmark.RIGHT_EYE;
                case PoseLandmark.RIGHT_EYE -> PoseLandmark.LEFT_EYE;
                case PoseLandmark.LEFT_EYE_OUTER -> PoseLandmark.RIGHT_EYE_OUTER;
                case PoseLandmark.RIGHT_EYE_OUTER -> PoseLandmark.LEFT_EYE_OUTER;
                case PoseLandmark.LEFT_EAR -> PoseLandmark.RIGHT_EAR;
                case PoseLandmark.RIGHT_EAR -> PoseLandmark.LEFT_EAR;
                case PoseLandmark.LEFT_MOUTH -> PoseLandmark.RIGHT_MOUTH;
                case PoseLandmark.RIGHT_MOUTH -> PoseLandmark.LEFT_MOUTH;
                default -> poseLandmark;
            };
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (pose == null) return;

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) {
            return;
        }


        // Draw all the points
        for (PoseLandmark landmark : landmarks) {
            drawPoint(canvas, landmark.getPosition(), leftPaint);
            if (showInFrameLikelihood) {
                canvas.drawText(
                    String.format(Locale.US, "%.2f", landmark.getInFrameLikelihood()),
                    landmark.getPosition().x,
                    landmark.getPosition().y,
                    whitePaint);
            }
        }

        PoseLandmark nose = pose.getPoseLandmark(correctLeft(PoseLandmark.NOSE));
        PoseLandmark lefyEyeInner = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_EYE_INNER));
        PoseLandmark lefyEye = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_EYE));
        PoseLandmark leftEyeOuter = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_EYE_OUTER));
        PoseLandmark rightEyeInner = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_EYE_INNER));
        PoseLandmark rightEye = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_EYE));
        PoseLandmark rightEyeOuter = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_EYE_OUTER));
        PoseLandmark leftEar = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_EAR));
        PoseLandmark rightEar = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_EAR));
        PoseLandmark leftMouth = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_MOUTH));
        PoseLandmark rightMouth = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_MOUTH));


        PoseLandmark leftShoulder = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_SHOULDER));
        PoseLandmark rightShoulder = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_SHOULDER));
        PoseLandmark leftElbow = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_ELBOW));
        PoseLandmark rightElbow = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_ELBOW));
        PoseLandmark leftWrist = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_WRIST));
        PoseLandmark rightWrist = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_WRIST));
        PoseLandmark leftHip = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_HIP));
        PoseLandmark rightHip = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_HIP));
        PoseLandmark leftKnee = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_KNEE));
        PoseLandmark rightKnee = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_KNEE));
        PoseLandmark leftAnkle = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_ANKLE));
        PoseLandmark rightAnkle = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_ANKLE));

        PoseLandmark leftPinky = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_PINKY));
        PoseLandmark rightPinky = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_PINKY));
        PoseLandmark leftIndex = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_INDEX));
        PoseLandmark rightIndex = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_INDEX));
        PoseLandmark leftThumb = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_THUMB));
        PoseLandmark rightThumb = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_THUMB));
        PoseLandmark leftHeel = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_HEEL));
        PoseLandmark rightHeel = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_HEEL));
        PoseLandmark leftFootIndex = pose.getPoseLandmark(correctLeft(PoseLandmark.LEFT_FOOT_INDEX));
        PoseLandmark rightFootIndex = pose.getPoseLandmark(correctLeft(PoseLandmark.RIGHT_FOOT_INDEX));

        // Face
        drawLine(canvas, nose.getPosition(), lefyEyeInner.getPosition(), whitePaint);
        drawLine(canvas, lefyEyeInner.getPosition(), lefyEye.getPosition(), whitePaint);
        drawLine(canvas, lefyEye.getPosition(), leftEyeOuter.getPosition(), whitePaint);
        drawLine(canvas, leftEyeOuter.getPosition(), leftEar.getPosition(), whitePaint);
        drawLine(canvas, nose.getPosition(), rightEyeInner.getPosition(), whitePaint);
        drawLine(canvas, rightEyeInner.getPosition(), rightEye.getPosition(), whitePaint);
        drawLine(canvas, rightEye.getPosition(), rightEyeOuter.getPosition(), whitePaint);
        drawLine(canvas, rightEyeOuter.getPosition(), rightEar.getPosition(), whitePaint);
        drawLine(canvas, leftMouth.getPosition(), rightMouth.getPosition(), whitePaint);


        drawLine(canvas, leftShoulder.getPosition(), rightShoulder.getPosition(), leftPaint);
        drawLine(canvas, leftHip.getPosition(), rightHip.getPosition(), leftPaint);

        // Left body
        drawLine(canvas, leftShoulder.getPosition(), leftElbow.getPosition(), leftPaint);
        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), leftPaint);
        drawLine(canvas, leftShoulder.getPosition(), leftHip.getPosition(), leftPaint);
        drawLine(canvas, leftHip.getPosition(), leftKnee.getPosition(), leftPaint);
        drawLine(canvas, leftKnee.getPosition(), leftAnkle.getPosition(), leftPaint);
        drawLine(canvas, leftWrist.getPosition(), leftThumb.getPosition(), leftPaint);
        drawLine(canvas, leftWrist.getPosition(), leftPinky.getPosition(), leftPaint);
        drawLine(canvas, leftWrist.getPosition(), leftIndex.getPosition(), leftPaint);
        drawLine(canvas, leftPinky.getPosition(), leftIndex.getPosition(), leftPaint);
        drawLine(canvas, leftAnkle.getPosition(), leftHeel.getPosition(), leftPaint);
        drawLine(canvas, leftHeel.getPosition(), leftFootIndex.getPosition(), leftPaint);
        drawLine(canvas, leftAnkle.getPosition(), leftFootIndex.getPosition(), leftPaint);

        // Right body
        drawLine(canvas, rightShoulder.getPosition(), rightElbow.getPosition(), leftPaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), leftPaint);
        drawLine(canvas, rightShoulder.getPosition(), rightHip.getPosition(), leftPaint);
        drawLine(canvas, rightHip.getPosition(), rightKnee.getPosition(), leftPaint);
        drawLine(canvas, rightKnee.getPosition(), rightAnkle.getPosition(), leftPaint);
        drawLine(canvas, rightWrist.getPosition(), rightThumb.getPosition(), leftPaint);
        drawLine(canvas, rightWrist.getPosition(), rightPinky.getPosition(), leftPaint);
        drawLine(canvas, rightWrist.getPosition(), rightIndex.getPosition(), leftPaint);
        drawLine(canvas, rightPinky.getPosition(), rightIndex.getPosition(), leftPaint);
        drawLine(canvas, rightAnkle.getPosition(), rightHeel.getPosition(), leftPaint);
        drawLine(canvas, rightHeel.getPosition(), rightFootIndex.getPosition(), leftPaint);
        drawLine(canvas, rightAnkle.getPosition(), rightFootIndex.getPosition(), leftPaint);
    }

    void drawPoint(Canvas canvas, @Nullable PointF point, Paint paint) {
        if (point == null) {
            return;
        }
        canvas.drawCircle(point.x, point.y, DOT_RADIUS, paint);
    }

    void drawLine(Canvas canvas, @Nullable PointF start, @Nullable PointF end, Paint paint) {
        if (start == null || end == null) {
            return;
        }
        canvas.drawLine(
            start.x, start.y, end.x, end.y, paint);
    }
}
