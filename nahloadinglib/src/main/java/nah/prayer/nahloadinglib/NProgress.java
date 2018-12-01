/*
 *    Copyright 2015 Kaopiz Software Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package nah.prayer.nahloadinglib;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class NProgress {

    public enum Style {
        SPIN_INDETERMINATE, PIE_DETERMINATE, ANNULAR_DETERMINATE, BAR_DETERMINATE
    }

    // To avoid redundant APIs, all HUD functions will be forward to
    // a custom dialog
    private ProgressDialog mProgressDialog;
    private float mDimAmount;
    private int mWindowColor;
    private float mCornerRadius;
    private boolean mCancellable;
    private Context mContext;

    private int mAnimateSpeed;
    private String mLabel;
    private String mDetailsLabel;
    private String mButtonMsg;

    private int mMaxProgress;
    private boolean mIsAutoDismiss;

    private int baseLayout = R.layout.layout_nprogress;

    public NProgress(Context context) {
        mContext = context;
        mProgressDialog = new ProgressDialog(context);
        mDimAmount = 0.7f;
        //noinspection deprecation
        mWindowColor = Color.TRANSPARENT;
        mAnimateSpeed = 1;
        mCornerRadius = 10;
        mIsAutoDismiss = true;

        setStyle(Style.SPIN_INDETERMINATE);
    }

    /**
     * Create a new HUD. Have the same effect as the constructor.
     * For convenient only.
     * @param context Activity context that the HUD bound to
     * @return An unique HUD instance
     */
    public static NProgress create(Context context) {
        return new NProgress(context);
    }

    /**
     * Specify the HUD style (not needed if you use a custom view)
     * @param style One of the NProgress.Style values
     * @return Current HUD
     */
    public NProgress setStyle(Style style) {
        View view = null;
        switch (style) {
            case SPIN_INDETERMINATE:
                view = new SpinView(mContext);
                break;
            case PIE_DETERMINATE:
                view = new PieView(mContext);
                break;
            case ANNULAR_DETERMINATE:
                view = new AnnularView(mContext);
                break;
            case BAR_DETERMINATE:
                view = new BarView(mContext);
                break;
            // No custom view style here, because view will be added later
        }
        mProgressDialog.setView(view);
        return this;
    }

    /**
     * Specify the dim area around the HUD, like in Dialog
     * @param dimAmount May take value from 0 to 1.
     *                  0 means no dimming, 1 mean darkness
     * @return Current HUD
     */
    public NProgress setDimAmount(float dimAmount) {
        if (dimAmount >= 0 && dimAmount <= 1) {
            mDimAmount = dimAmount;
        }
        return this;
    }

    /**
     * Specify the HUD background color
     * @param color ARGB color
     * @return Current HUD
     */
    public NProgress setWindowColor(int color) {
        mWindowColor = color;
        return this;
    }

    /**
     * Specify corner radius of the HUD (default is 10)
     * @param radius Corner radius in dp
     * @return Current HUD
     */
    public NProgress setCornerRadius(float radius) {
        mCornerRadius = radius;
        return this;
    }

    /**
     * Change animate speed relative to default. Only have effect when use with indeterminate style
     * @param scale 1 is default, 2 means double speed, 0.5 means half speed..etc.
     * @return Current HUD
     */
    public NProgress setAnimationSpeed(int scale) {
        mAnimateSpeed = scale;
        return this;
    }

    /**
     * Optional label to be displayed on the HUD
     * @return Current HUD
     */
    public NProgress setLabel(String label) {
        mLabel = label;
        return this;
    }

    /**
     * Optional detail description to be displayed on the HUD
     * @return Current HUD
     */
    public NProgress setDetailsLabel(String detailsLabel) {
        mDetailsLabel = detailsLabel;
        return this;
    }

    public NProgress setButtonMsg(String buttonMsg, View.OnClickListener mButtonClickListener) {
        mButtonMsg = buttonMsg;
        this.mButtonClickListener = mButtonClickListener;
        return this;
    }

    private View.OnClickListener mButtonClickListener;

    /**
     * Max value for use in one of the determinate styles
     * @return Current HUD
     */
    public NProgress setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
        return this;
    }

    /**
     * Set current progress. Only have effect when use with a determinate style, or a custom
     * view which implements Determinate interface.
     */
    public void setProgress(int progress) {
        mProgressDialog.setProgress(progress);
    }

    /**
     * Provide a custom view to be displayed.
     * @param view Must not be null
     * @return Current HUD
     */
    public NProgress setCustomView(View view) {
        if (view != null) {
            mProgressDialog.setView(view);
        } else {
            throw new RuntimeException("Custom view must not be null!");
        }
        return this;
    }

    public NProgress setCustomLayout(@NonNull int layout) {
        mProgressDialog.setCustomLayout(layout);
        return this;
    }

    /**
     * Specify whether this HUD can be cancelled by using back button (default is false)
     * @return Current HUD
     */
    public NProgress setCancellable(boolean isCancellable) {
        mCancellable = isCancellable;
        return this;
    }

    /**
     * Specify whether this HUD closes itself if progress reaches max. Default is true.
     * @return Current HUD
     */
    public NProgress setAutoDismiss(boolean isAutoDismiss) {
        mIsAutoDismiss = isAutoDismiss;
        return this;
    }

    public NProgress show() {
        if (!isShowing()) {
            mProgressDialog.show();
        }
        return this;
    }

    public boolean isShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    public void dismiss() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private class ProgressDialog extends Dialog {

        private Determinate mDeterminateView;
        private Indeterminate mIndeterminateView;
        private View mView;

        public ProgressDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.layout_nprogress);

            Window window = getWindow();
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.dimAmount = mDimAmount;
            window.setAttributes(layoutParams);

            setCanceledOnTouchOutside(false);
            setCancelable(mCancellable);

            initViews();
        }

        private void initViews() {
            BackgroundLayout background = findViewById(R.id.background);
            background.setBaseColor(mWindowColor);
            background.setCornerRadius(mCornerRadius);

            FrameLayout containerFrame = findViewById(R.id.container);
            int wrapParam = ViewGroup.LayoutParams.MATCH_PARENT;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(wrapParam, wrapParam);
            containerFrame.addView(mView, params);

            if (mDeterminateView != null) {
                mDeterminateView.setMax(mMaxProgress);
            }
            if (mIndeterminateView != null) {
                mIndeterminateView.setAnimationSpeed(mAnimateSpeed);
            }

            if (TextUtils.isEmpty(mLabel)) {
                TextView labelText = findViewById(R.id.label);
                labelText.setText(mLabel);
                labelText.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(mDetailsLabel)) {
                TextView detailsText = findViewById(R.id.details_label);
                detailsText.setText(mDetailsLabel);
                detailsText.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(mButtonMsg)) {
                TextView button = findViewById(R.id.button);
                button.setText(mButtonMsg);
                button.setVisibility(View.VISIBLE);
                if (mButtonClickListener != null) {
                    button.setOnClickListener(mButtonClickListener);
                }
            }

        }

        public void setProgress(int progress) {
            if (mDeterminateView != null) {
                mDeterminateView.setProgress(progress);
                if (mIsAutoDismiss && progress >= mMaxProgress) {
                    dismiss();
                }
            }
        }

        public void setView(View view) {
            if (view != null) {
                if (view instanceof Determinate) {
                    mDeterminateView = (Determinate) view;
                }
                if (view instanceof Indeterminate) {
                    mIndeterminateView = (Indeterminate) view;
                }
                mView = view;
            }
        }
        public void setCustomLayout(int layout) {
            try{
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View childLayout = inflater.inflate(layout, null);
                mView = childLayout;
            }catch (Resources.NotFoundException e){
                e.printStackTrace();
            }

        }
    }
}
