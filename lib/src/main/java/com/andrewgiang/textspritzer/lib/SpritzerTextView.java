package com.andrewgiang.textspritzer.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by andrewgiang on 3/3/14.
 */
public class SpritzerTextView extends TextView implements View.OnClickListener {
    /**
     * Interface definition for a callback to be invoked when the
     * clickControls are enabled and the view is clicked
     */
    public static interface OnClickControlListener {
        /**
         * Called when the spritzer pauses upon click
         */
        void onPause();

        /**
         * Called when the spritzer plays upon clicked
         */
        void onPlay();
    }

    public static final String TAG = SpritzerTextView.class.getName();
    public static final int PAINT_WIDTH_DP = 2;          // thickness of spritz guide bars in dp
    // For optimal drawing should be an even number

    private Spritzer mSpritzer;
    private Paint mPaintGuides;
    private float mPaintWidthPx;
    private String mTestString;
    private boolean mDefaultClickListener = false;
    private int mAdditonalPadding;
    private int mHeightOffset;
    private int mWidthOffset;

    /**
     * Register a callback for when the view has been clicked
     * <p/>
     * Note: it is mandatory to use the clickControls
     *
     * @param listener
     */
    public void setOnClickControlListener(OnClickControlListener listener) {
        mClickControlListener = listener;
    }

    private OnClickControlListener mClickControlListener;


    public SpritzerTextView(Context context) {
        super(context);
        init();
    }

    public SpritzerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SpritzerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setAdditionalPadding(attrs);
        final TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SpritzerTextView, 0, 0);
        try {
            mDefaultClickListener = a.getBoolean(R.styleable.SpritzerTextView_clickControls, false);
        } finally {
            a.recycle();
        }
        init();

    }

    private void setAdditionalPadding(AttributeSet attrs) {
        //check padding attributes
        int[] attributes = new int[]{android.R.attr.padding, android.R.attr.paddingTop,
                android.R.attr.paddingBottom};

        final TypedArray paddingArray = getContext().obtainStyledAttributes(attrs, attributes);
        try {
            final int padding = paddingArray.getDimensionPixelOffset(0, 0);
            final int paddingTop = paddingArray.getDimensionPixelOffset(1, 0);
            final int paddingBottom = paddingArray.getDimensionPixelOffset(2, 0);
            mAdditonalPadding = Math.max(padding, Math.max(paddingTop, paddingBottom));
            Log.w(TAG, "Additional Padding " + mAdditonalPadding);
        } finally {
            paddingArray.recycle();
        }
    }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        mPaintWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PAINT_WIDTH_DP, getResources().getDisplayMetrics());
        mSpritzer = new Spritzer(this);
        mPaintGuides = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGuides.setColor(getCurrentTextColor());
        mPaintGuides.setStrokeWidth(mPaintWidthPx);

        mHeightOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mWidthOffset  = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        mPaintGuides.setAlpha(128);
        if (mDefaultClickListener) {
            this.setOnClickListener(this);
        }

    }

    public void setReticleLineColor(int reticleLineColor) {
        mPaintGuides.setColor(reticleLineColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Measurements for top & bottom guide line
        int beginX = mWidthOffset;
        int endX = getMeasuredWidth() - mWidthOffset;
        int topY = mHeightOffset;
        int bottomY = getMeasuredHeight() - mHeightOffset;
        // Paint the top guide and bottom guide bars
        canvas.drawLine(beginX, topY, endX, topY, mPaintGuides);
        canvas.drawLine(beginX, bottomY, endX, bottomY, mPaintGuides);

        // Measurements for pivot indicator
        float centerX = calculatePivotXOffset() + getPaddingLeft();
        final int pivotIndicatorLength = getPivotIndicatorLength();

        // Paint the pivot indicator
        canvas.drawLine(centerX, topY + (mPaintWidthPx / 2), centerX, topY + (mPaintWidthPx / 2) + pivotIndicatorLength, mPaintGuides); //line through center of circle
        canvas.drawLine(centerX, bottomY - (mPaintWidthPx / 2), centerX, bottomY - (mPaintWidthPx / 2) - pivotIndicatorLength, mPaintGuides);
    }

    private int getPivotPadding() {
        return getPivotIndicatorLength() * 2 + mAdditonalPadding;
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        Rect textBound = new Rect();
        getPaint().getTextBounds("Ag", 0, 2, textBound);
        getLayoutParams().height = (mHeightOffset * 2) + (int)(getPivotPadding() * 1.6) + (int)(mPaintWidthPx * 3) + (textBound.height());
    }

    private int getPivotIndicatorLength() {

        return getPaint().getFontMetricsInt().bottom;
    }

    private float calculatePivotXOffset() {
        // Craft a test String of precise length
        // to reach pivot character
        if (mTestString == null) {
            // Spritzer requires monospace font so character is irrelevant
            mTestString = "a";
        }
        float textSize = (getPaint().measureText(mTestString, 0, 1));
        Spritzer.MAX_WORD_LENGTH = getMeasuredWidth() / (int)textSize;
        Spritzer.CHARS_LEFT_OF_PIVOT = (Spritzer.MAX_WORD_LENGTH/2) - 3;

        // Measure the rendered distance of CHARS_LEFT_OF_PIVOT chars
        // plus half the pivot character
        return (textSize * (Spritzer.CHARS_LEFT_OF_PIVOT + .50f));
    }

    /**
     * This determines the words per minute the sprizter will read at
     *
     * @param wpm the number of words per minute
     */
    public void setWpm(int wpm) {
        mSpritzer.setWpm(wpm);
    }


    /**
     * Set a custom spritzer
     *
     * @param spritzer
     */
    public void setSpritzer(Spritzer spritzer) {
        mSpritzer = spritzer;
        mSpritzer.swapTextView(this);
    }

    /**
     * Pass input text to spritzer object
     *
     * @param input
     */
    public void setSpritzText(String input) {
        mSpritzer.setText(input);
    }

    /**
     * If true, this view will automatically pause or play spritz text upon view clicks
     * <p/>
     * If false, the callback OnClickControls are not invoked and
     *
     * @param useDefaultClickControls
     */
    public void setUseClickControls(boolean useDefaultClickControls) {
        mDefaultClickListener = useDefaultClickControls;
    }

    /**
     * Will play the spritz text that was set in setSpritzText
     */
    public void play() {
        mSpritzer.start();
    }

    public void pause() {
        mSpritzer.pause();
    }

    public int getWpm() {
        return mSpritzer.getWpm();
    }

    public void attachProgressBar(ProgressBar bar) {
        mSpritzer.attachProgressBar(bar);

    }

    public void setOnCompletionListener(Spritzer.OnCompletionListener listener) {

        mSpritzer.setOnCompletionListener(listener);
    }

    /**
     * @param strategy @see {@link com.andrewgiang.textspritzer.lib.DelayStrategy#delayMultiplier(String)}
     */
    public void setDelayStrategy(DelayStrategy strategy) {
        mSpritzer.setDelayStrategy(strategy);
    }

    public Spritzer getSpritzer() {
        return mSpritzer;
    }

    @Override
    public void onClick(View v) {
        if (mSpritzer.isPlaying()) {
            if (mClickControlListener != null) {
                mClickControlListener.onPause();
            }
            pause();
        } else {
            if (mClickControlListener != null) {
                mClickControlListener.onPlay();
            }
            play();
        }

    }

    public int getCurrentWordIndex() {
        return mSpritzer.mCurWordIdx;
    }

    public int getMinutesRemainingInQueue() {
        return mSpritzer.getMinutesRemainingInQueue();
    }
}