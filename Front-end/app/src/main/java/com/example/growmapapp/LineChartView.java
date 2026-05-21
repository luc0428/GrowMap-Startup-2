package com.example.growmapapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {

    private Paint linePaint;
    private Paint pointPaint;
    private Paint axisPaint;
    private Paint fillPaint;
    private Paint gridPaint;
    private Path linePath = new Path();
    private Path fillPath = new Path();
    private List<Float> dataPoints = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private float maxValue = 100f;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#06B6D4"));
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.parseColor("#94A3B8"));
        axisPaint.setTextSize(24f);
        axisPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(2f);
        gridPaint.setAlpha(50);
    }

    public void setData(List<Float> data, List<String> labels) {
        this.dataPoints = data;
        this.labels = labels;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints == null || dataPoints.size() < 2) return;

        float paddingLeft = 60f;
        float paddingBottom = 60f;
        float paddingTop = 40f;
        float paddingRight = 40f;

        float width = getWidth() - paddingLeft - paddingRight;
        float height = getHeight() - paddingTop - paddingBottom;
        float stepX = width / (dataPoints.size() - 1);

        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = paddingTop + height - (i * (height / gridLines));
            canvas.drawLine(paddingLeft, y, paddingLeft + width, y, gridPaint);
            
            String label = String.valueOf((int)(maxValue / gridLines * i));
            canvas.drawText(label, paddingLeft - 30f, y + 10f, axisPaint);
        }

        linePath.reset();
        fillPath.reset();

        float firstX = 0, firstY = 0, lastX = 0;

        for (int i = 0; i < dataPoints.size(); i++) {
            float x = paddingLeft + (i * stepX);
            float y = paddingTop + height - (dataPoints.get(i) / maxValue * height);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, paddingTop + height);
                fillPath.lineTo(x, y);
                firstX = x;
                firstY = y;
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
            lastX = x;

            if (labels != null && i < labels.size()) {
                canvas.drawText(labels.get(i), x, paddingTop + height + 40f, axisPaint);
            }
        }

        fillPath.lineTo(lastX, paddingTop + height);
        fillPath.close();
        if (fillPaint.getShader() == null) {
            fillPaint.setShader(new android.graphics.LinearGradient(0, paddingTop, 0, paddingTop + height,
                    Color.parseColor("#3306B6D4"), Color.TRANSPARENT, android.graphics.Shader.TileMode.CLAMP));
        }
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
        for (int i = 0; i < dataPoints.size(); i++) {
            float x = paddingLeft + (i * stepX);
            float y = paddingTop + height - (dataPoints.get(i) / maxValue * height);
            
            canvas.drawCircle(x, y, 8f, pointPaint);
            
            Paint borderPaint = new Paint(); 
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(3f);
            borderPaint.setColor(linePaint.getColor());
            canvas.drawCircle(x, y, 8f, borderPaint);
            canvas.drawText(String.format("%.0f", dataPoints.get(i)), x, y - 20f, axisPaint);
        }
    }
}
