package edu.cs4730.barcodevideoxdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

public class BarCodeGraphic extends View {
    Paint myColor;

    List<Barcode> barcodes;

    public BarCodeGraphic(Context context) {
        super(context);
        //set up the paint object.
        myColor = new Paint();
        myColor.setColor(Color.RED);
        myColor.setStyle(Paint.Style.STROKE);
        myColor.setStrokeWidth(10);
        myColor.setTextSize(myColor.getTextSize() * 5);

    }

    public BarCodeGraphic(Context context, AttributeSet attrs) {
        super(context, attrs);
        //set up the paint object.
        myColor = new Paint();
        myColor.setColor(Color.RED);
        myColor.setStyle(Paint.Style.STROKE);
        myColor.setStrokeWidth(10);
        myColor.setTextSize(myColor.getTextSize() * 5);

    }

    public BarCodeGraphic(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //set up the paint object.
        myColor = new Paint();
        myColor.setColor(Color.RED);
        myColor.setStyle(Paint.Style.STROKE);
        myColor.setStrokeWidth(10);
        myColor.setTextSize(myColor.getTextSize() * 5);

    }

    public void setBarCodes(List<Barcode> barcodes) {
        this.barcodes = barcodes;
        invalidate();
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        if (barcodes == null) return;
        for (Barcode barcode : barcodes) {
            Rect rect = barcode.getBoundingBox();
            canvas.drawRect(rect, myColor);
            canvas.drawText(barcode.getDisplayValue(), rect.left, rect.bottom, myColor);
        }
    }
}
