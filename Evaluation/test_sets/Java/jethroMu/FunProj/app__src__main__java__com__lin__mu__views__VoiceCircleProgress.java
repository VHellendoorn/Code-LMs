package com.lin.mu.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import com.lin.mu.R;


/**
 * Created by lin on 2016/7/29.
 */
public class VoiceCircleProgress extends ProgressBar {

    private static final int PROGRESS_DEFAULT_COLOR = 0xFFd3d6da;//默认圆(边框)的颜色
    private static final int PROGRESS_REACHED_COLOR = 0XFFFC00D1;//进度条的颜色
    private static final float PROGRESS_REACHED_HEIGHT = 2.0f;//进度条的高度
    private static final float PROGRESS_DEFAULT_HEIGHT = 0.5f;//默认圆的高度
    private static final int PROGRESS_RADIUS = 14;//圆的半径

    //View的当前状态，默认为未开始
    private Status mStatus = Status.End;
    private Paint mPaint;
    private int mReachedColor = PROGRESS_REACHED_COLOR;
    private int mReachedHeight = dp2px(Integer.valueOf((int) PROGRESS_REACHED_HEIGHT));
    private int mDefaultColor = PROGRESS_DEFAULT_COLOR;
    private int mDefaultHeight = dp2px(Integer.valueOf((int) PROGRESS_DEFAULT_HEIGHT));
    //圆的半径
    private int mRadius = dp2px(PROGRESS_RADIUS);
    //通过path路径去绘制三角形
    private Path mPath;
    //三角形的边长
    private int triangleLength;


    public enum Status {
        End,
        Starting
    }

    public VoiceCircleProgress(Context context) {
        this(context, null);
    }

    public VoiceCircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceCircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义属性的值
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CustomCircleProgress);
        //默认圆的颜色
        mDefaultColor = array.getColor(R.styleable.CustomCircleProgress_progress_default_color, PROGRESS_DEFAULT_COLOR);
        //进度条的颜色
        mReachedColor = array.getColor(R.styleable.CustomCircleProgress_progress_reached_color, PROGRESS_REACHED_COLOR);
        //默认圆的高度
        mDefaultHeight = (int) array.getDimension(R.styleable.CustomCircleProgress_progress_default_height, mDefaultHeight);
        //进度条的高度
        mReachedHeight = (int) array.getDimension(R.styleable.CustomCircleProgress_progress_reached_height, mReachedHeight);
        //圆的半径
        mRadius = (int) array.getDimension(R.styleable.CustomCircleProgress_circle_radius, mRadius);
        array.recycle();

        setPaint();

        //通过path路径绘制三角形
        mPath = new Path();
        triangleLength = mRadius;
        float firstX = (float) ((mRadius * 2 - Math.sqrt(3.0) / 2 * mRadius) / 2);//左上角第一个点的横坐标，根据勾三股四弦五定律,Math.sqrt(3.0)表示对3开方
        float mFirstX = (float) (firstX + firstX * 0.2);
        float firstY = mRadius - triangleLength / 2.5f;
        float secondX = mFirstX;
        float secondY = (float) (mRadius + triangleLength / 2.5);
        float thirdX = (float) (mFirstX + Math.sqrt(3.0) / 2.5 * mRadius);
        float thirdY = mRadius;
        mPath.moveTo(mFirstX, firstY);
        mPath.lineTo(secondX, secondY);
        mPath.lineTo(thirdX, thirdY);
        mPath.lineTo(mFirstX, firstY);
    }

    private void setPaint() {
        mPaint = new Paint();
        //下面是设置画笔的一些属性
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setDither(true);//防抖动，绘制出来的图要更加柔和清晰
        mPaint.setStyle(Paint.Style.STROKE);//设置填充样式
        /**
         *  Paint.Style.FILL    :填充内部
         *  Paint.Style.FILL_AND_STROKE  ：填充内部和描边
         *  Paint.Style.STROKE  ：仅描边
         */
        mPaint.setStrokeCap(Paint.Cap.ROUND);//设置画笔笔刷类型
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int paintHeight = Math.max(mReachedHeight, mDefaultHeight);//比较两数，取最大值

        if (heightMode != MeasureSpec.EXACTLY) {
            int exceptHeight = getPaddingTop() + getPaddingBottom() + mRadius * 2 + paintHeight;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(exceptHeight, MeasureSpec.EXACTLY);
        }
        if (widthMode != MeasureSpec.EXACTLY) {
            int exceptWidth = getPaddingLeft() + getPaddingRight() + mRadius * 2 + paintHeight;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(exceptWidth, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(getPaddingLeft(), getPaddingTop());
        mPaint.setStyle(Paint.Style.STROKE);
        //画默认圆(边框)的一些设置
        mPaint.setColor(mDefaultColor);
        mPaint.setStrokeWidth(mDefaultHeight);
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);

        //画进度条
        mPaint.setColor(mReachedColor);
        mPaint.setStrokeWidth(mReachedHeight);
        float sweepAngle = getProgress() * 1.0f / getMax() * 360;
        canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), -90, sweepAngle, false, mPaint);//drawArc：绘制圆弧

        if (mStatus == Status.End) {//未开始状态，画笔填充三角形
            mPaint.setStyle(Paint.Style.FILL);
            //设置颜色
            mPaint.setColor(Color.parseColor("#01A1EB"));
            //画三角形
            canvas.drawPath(mPath, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(dp2px(2));
            mPaint.setColor(Color.parseColor("#01A1EB"));
            canvas.drawLine(mRadius * 2 / 2.5f, mRadius * 2 / 3.2f, mRadius * 2 / 2.5f, 2 * mRadius * 2 / 3.2f, mPaint);
            canvas.drawLine(2 * mRadius - (mRadius * 2 / 2.5f), mRadius * 2 / 3.2f, 2 * mRadius - (mRadius * 2 / 2.5f), 2 * mRadius * 2 / 3.2f, mPaint);
        }
        canvas.save();

    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }


    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
        invalidate();
    }

}
