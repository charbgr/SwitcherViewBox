package com.charbgr.selectionbox.library;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SwitcherView extends TextView {

    private Drawable mIconDrawable;
    private boolean isExpanded = false;
    private static final int MAX_LEVEL = 10000;

    private View mHiddenView;
    private View mNonHiddenView;

    private static final int BOX_EXPAND_ANIM_DURATION = 200;

    public SwitcherView(Context context) {
        super(context);
    }

    public SwitcherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SwitcherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public SwitcherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SwitcherView);
        try {
            int colorResId = typedArray.getColor(R.styleable.SwitcherView_arrowTintColor, Color.BLACK);

            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.arrow);
            if (drawable != null) {
                mIconDrawable = DrawableCompat.wrap(drawable);
                setTintColor(colorResId);

            }
            setCompoundDrawablesWithIntrinsicBounds(null, null, mIconDrawable, null);
        } finally {
            typedArray.recycle();
        }
    }

    public void setTintColor(int colorResId) {
        if (mIconDrawable != null) {
            DrawableCompat.setTint(mIconDrawable, colorResId);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            displayContent();
        }

        return super.onTouchEvent(event);
    }

    private void displayContent() {
        isExpanded = !isExpanded;
        rotateArrow();
        animateContent();
    }

    private void rotateArrow() {

        int start = isExpanded ? 0 : MAX_LEVEL;
        int end = isExpanded ? MAX_LEVEL : 0;

        ObjectAnimator animator = ObjectAnimator.ofInt(mIconDrawable, "level", start, end);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();
    }

    private void animateContent() {

        int hideTranslateY = -mHiddenView.getHeight() / 4; // last 25% of animation
        if (isExpanded && mHiddenView.getTranslationY() == 0) {
            // initial setup
            mHiddenView.setAlpha(0);
            mHiddenView.setTranslationY(hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mNonHiddenView.setVisibility(isExpanded
                        ? View.INVISIBLE : View.VISIBLE);
                mHiddenView.setVisibility(isExpanded
                        ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });

        if (isExpanded) {
            mHiddenView.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mHiddenView, View.ALPHA, 1)
                            .setDuration(BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mHiddenView, View.TRANSLATION_Y, 0)
                            .setDuration(BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    ObjectAnimator.ofFloat(mNonHiddenView, View.ALPHA, 0)
                            .setDuration(BOX_EXPAND_ANIM_DURATION),
                    subSet);
            set.start();
        } else {
            mNonHiddenView.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mHiddenView, View.ALPHA, 0)
                            .setDuration(BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mHiddenView, View.TRANSLATION_Y,
                            hideTranslateY)
                            .setDuration(BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    subSet,
                    ObjectAnimator.ofFloat(mNonHiddenView, View.ALPHA, 1)
                            .setDuration(BOX_EXPAND_ANIM_DURATION));
            set.start();
        }

        set.start();
    }

    public void setHiddenView(View mHiddenView) {
        this.mHiddenView = mHiddenView;
    }

    public void setNonHiddenView(View mNonHiddenView) {
        this.mNonHiddenView = mNonHiddenView;
    }
}
