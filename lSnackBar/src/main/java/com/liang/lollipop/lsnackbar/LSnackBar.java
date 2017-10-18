package com.liang.lollipop.lsnackbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by LiuJ on 2017/07/26.
 * 一个显示在屏幕顶部的SnackBar
 */
public class LSnackBar {

    public static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    public static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    public static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    public static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();
    public static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    /**
     * Callback class for {@link LSnackBar} instances.
     *
     * @see LSnackBar#setCallback(Callback)
     */
    public static abstract class Callback {
        /**
         * Indicates that the LSnackBar was dismissed via a swipe.
         */
        public static final int DISMISS_EVENT_SWIPE = 0;
        /**
         * Indicates that the LSnackBar was dismissed via an action click.
         */
        public static final int DISMISS_EVENT_ACTION = 1;
        /**
         * Indicates that the LSnackBar was dismissed via a timeout.
         */
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        /**
         * Indicates that the LSnackBar was dismissed via a call to {@link #dismiss()}.
         */
        public static final int DISMISS_EVENT_MANUAL = 3;
        /**
         * Indicates that the LSnackBar was dismissed from a new Snackbar being shown.
         */
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;


        @IntDef({DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent {
        }

        /**
         * Called when the given {@link LSnackBar} has been dismissed, either through a time-out,
         * having been manually dismissed, or an action being clicked.
         *
         * @param lSnackbar The snackbar which has been dismissed.
         * @param event        The event which caused the dismissal. One of either:
         *                     {@link #DISMISS_EVENT_SWIPE}, {@link #DISMISS_EVENT_ACTION},
         *                     {@link #DISMISS_EVENT_TIMEOUT}, {@link #DISMISS_EVENT_MANUAL} or
         *                     {@link #DISMISS_EVENT_CONSECUTIVE}.
         * @see LSnackBar#dismiss()
         */
        public void onDismissed(LSnackBar lSnackbar, @DismissEvent int event) {
        }

        /**
         * Called when the given {@link LSnackBar} is visible.
         *
         * @param lSnackbar The snackbar which is now visible.
         * @see LSnackBar#show()
         */
        public void onShown(LSnackBar lSnackbar) {

        }
    }

    /**
     * Show the LSnackBar from top to down.
     */
    public static final int APPEAR_FROM_TOP_TO_DOWN = 0;

    /**
     * Show the LSnackBar from top to down.
     */
    public static final int APPEAR_FROM_BOTTOM_TO_TOP = 1;

    /**
     * Show the LSnackBar indefinitely. This means that the LSnackBar will be displayed from the time
     * that is {@link #show() shown} until either it is dismissed, or another LSnackBar is shown.
     *
     * @see #setDuration
     */
    public static final int LENGTH_INDEFINITE = -2;

    /**
     * Show the LSnackBar for a short period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = -1;

    /**
     * Show the LSnackBar for a long period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_LONG = 0;

    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }


    @IntDef({APPEAR_FROM_TOP_TO_DOWN, APPEAR_FROM_BOTTOM_TO_TOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OverSnackAppearDirection {
    }

    private static final int ANIMATION_DURATION = 250;
    private static final int ANIMATION_FADE_DURATION = 180;

    private static final Handler sHandler;
    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;

    private
    @OverSnackAppearDirection
    int appearDirection = APPEAR_FROM_TOP_TO_DOWN;

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((LSnackBar) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((LSnackBar) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    private final ViewGroup mParent;
    private final Context mContext;
    private final SnackbarLayout mView;
    private int mDuration;
    private Callback mCallback;

    private final AccessibilityManager mAccessibilityManager;

    private final SnackBarManager.Callback mManagerCallback = new SnackBarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, LSnackBar.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, LSnackBar.this));
        }
    };

    private LSnackBar(ViewGroup parent) {
        this(parent,APPEAR_FROM_TOP_TO_DOWN);

    }

    private LSnackBar(ViewGroup parent, @OverSnackAppearDirection int appearDirection) {
        this.appearDirection = appearDirection;
        mParent = parent;
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if(appearDirection == APPEAR_FROM_BOTTOM_TO_TOP){
            mView = (SnackbarLayout) inflater.inflate(R.layout.view_lsnackbar_layout_bottom, mParent, false);
        }else{
            mView = (SnackbarLayout) inflater.inflate(R.layout.view_lsnackbar_layout_top, mParent, false);
        }
        mAccessibilityManager = (AccessibilityManager)
                mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if(appearDirection == APPEAR_FROM_TOP_TO_DOWN) {
            setMinHeight(0,0);
        }
    }

    /**
     *
     * @param stateBarHeight
     * @param actionBarHeight
     * @return
     */
    public LSnackBar setMinHeight(int stateBarHeight,int actionBarHeight){
        if(appearDirection == APPEAR_FROM_TOP_TO_DOWN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(stateBarHeight>0 || actionBarHeight>0){
                    mView.setPadding(0, stateBarHeight, 0, 0);
                    mView.setMinimumHeight(stateBarHeight+actionBarHeight);
                }else{
                    int[] size = new int[2];
                    int stateSize = ScreenUtil.getStatusHeight(mContext);
                    int paddingTop = 0;
                    mParent.getLocationOnScreen(size);
                    //如果父类有内补白，那么累加起来，用于判断是否在状态栏以内
                    size[1] += mParent.getPaddingTop();
                    if(size[1]>=stateSize){
                        paddingTop = 0;
                    }else if(size[1] == 0){
                        paddingTop = stateSize;
                    }else{
                        paddingTop = stateSize-size[1];
                        if(paddingTop<0)
                            paddingTop = 0;
                    }
                    mView.setPadding(0, paddingTop, 0, 0);
                    mView.setMinimumHeight(ScreenUtil.getActionBarHeight(mContext)+paddingTop);
                }
            }else{
                if(stateBarHeight>0 || actionBarHeight>0){
                    mView.setMinimumHeight(actionBarHeight);
                    ScreenUtil.setMargins(mView,0,stateBarHeight,0,0);
                }else{
                    mView.setMinimumHeight(ScreenUtil.getActionBarHeight(mContext));
                    ScreenUtil.setMargins(mView,0,ScreenUtil.getStatusHeight(mContext),0,0);
                }
            }
        }
        return this;
    }

    /**
     * Make a LSnackBar to display a message
     * <p/>
     * <p>LSnackBar will try and find a parent view to hold LSnackBar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p/>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows LSnackBar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static LSnackBar make(@NonNull View view, @NonNull CharSequence text, @Duration int duration) {
        return make(view,text,duration,APPEAR_FROM_TOP_TO_DOWN);
    }

    @NonNull
    public static LSnackBar make(@NonNull View view, @NonNull CharSequence text, @Duration int duration, @OverSnackAppearDirection int appearDirection) {
        LSnackBar lSnackBar = new LSnackBar(findSuitableParent(view), appearDirection);
        lSnackBar.setText(text);
        lSnackBar.setDuration(duration);
        return lSnackBar;
    }
    @NonNull
    public static LSnackBar makeLocal(@NonNull View view, @NonNull CharSequence text, @Duration int duration){
        return makeLocal(view,text,duration,APPEAR_FROM_TOP_TO_DOWN);
    }

    @NonNull
    public static LSnackBar makeLocal(@NonNull View view, @NonNull CharSequence text, @Duration int duration, @OverSnackAppearDirection int appearDirection) {
        LSnackBar lSnackBar = new LSnackBar(findSuitableParent(view,true), appearDirection);
        lSnackBar.setText(text);
        lSnackBar.setDuration(duration);
        return lSnackBar;
    }

    /**
     * Make a LSnackBar to display a message.
     * <p/>
     * <p>LSnackBar will try and find a parent view to hold LSnackBar's view from the value given
     * to {@code view}. LSnackBar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p/>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows LSnackBar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param resId    The resource id of the string resource to use. Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static LSnackBar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    private static ViewGroup findSuitableParent(View view){
        return findSuitableParent(view,false);
    }

    private static ViewGroup findSuitableParent(View view,boolean closest) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if(closest&&fallback!=null){
                //如果要求是最接近的，那么就直接返回最近的一个
                return fallback;
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
        //return (ViewGroup) view;
    }

    /**
     *
     * @param resource_id
     * @return
     */
    public LSnackBar addIcon(int resource_id) {
        final TextView tv = mView.getMessageView();
        tv.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext,resource_id), null, null, null);
        return this;
    }

    public LSnackBar addView(View view,int index) {
        mView.addView(view,index);
        return this;
    }

    public LSnackBar addView(View view) {
        return addView(view,mView.getChildCount());
    }

    /**
     *
     * @param resource_id image id
     * @param width image width
     * @param height image height
     * @return
     */
    public LSnackBar addIcon(int resource_id, int width, int height) {
        final TextView tv = mView.getMessageView();
        if(width>0 || height>0){
            tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(mView.getResources(),Bitmap.createScaledBitmap(((BitmapDrawable) (ContextCompat.getDrawable(mContext,resource_id))).getBitmap(), width, height, true)), null, null, null);
        }else{
            addIcon(resource_id);
        }
        return this;
    }

    /**
     *
     * @param color
     * @return
     */
    public LSnackBar setBackgroundColor(int color) {
        mView.setBackgroundColor(color);
        return this;
    }

    /**
     * Set the action to be displayed in this {@link LSnackBar}.
     *
     * @param resId    String resource to display
     * @param listener callback to be invoked when the action is clicked
     */
    @NonNull
    public LSnackBar setAction(@StringRes int resId, View.OnClickListener listener) {
        return setAction(mContext.getText(resId), listener);
    }

    /**
     * Set the action to be displayed in this {@link LSnackBar}.
     *
     * @param text     Text to display
     * @param listener callback to be invoked when the action is clicked
     */
    @NonNull
    public LSnackBar setAction(CharSequence text, final View.OnClickListener listener) {
        final TextView tv = mView.getActionView();
        if (TextUtils.isEmpty(text) || listener == null) {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                    dispatchDismiss(Callback.DISMISS_EVENT_ACTION);
                }
            });
        }
        return this;
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public LSnackBar setActionTextColor(ColorStateList colors) {
        final Button btn = mView.getActionView();
        btn.setTextColor(colors);
        return this;
    }

    /**
     * Sets the text color of the action specified in
     *
     */
    @NonNull
    public LSnackBar setActionTextSize(int size) {
        final Button btn = mView.getActionView();
        btn.setTextSize(size);
        return this;
    }

    /**
     * Sets the text color of the action specified in
     *
     */
    @NonNull
    public LSnackBar setMessageTextSize( int size) {
        final TextView tv = mView.getMessageView();
        tv.setTextSize(size);
        return this;
    }

    /**
     * Sets the text color of the action specified in
     *
     */
    @NonNull
    public LSnackBar setActionTextColor(@ColorInt int color) {
        return setActionTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text color of the action specified in
     *
     */
    @NonNull
    public LSnackBar setTextColor(@ColorInt int color) {
        return setTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public LSnackBar setTextColor(ColorStateList colors) {
        setActionTextColor(colors);
        setMessageTextColor(colors);
        return this;
    }

    public LSnackBar setGravity(int gravity){
        final TextView tv = mView.getMessageView();
        tv.setGravity(gravity);
        return this;
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public LSnackBar setMessageTextColor(@ColorInt int color) {
        return setMessageTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public LSnackBar setMessageTextColor(ColorStateList colors) {
        final TextView tv = mView.getMessageView();
        tv.setTextColor(colors);
        return this;
    }

    /**
     * Update the text in this {@link LSnackBar}.
     *
     * @param message The new text for the Toast.
     */
    @NonNull
    public LSnackBar setText(@NonNull CharSequence message) {
        final TextView tv = mView.getMessageView();
        tv.setText(message);
        return this;
    }

    public LSnackBar setLogo(Bitmap logo){
        final ImageView imageView = mView.getLogoView();
        if(logo==null){
            imageView.setVisibility(View.GONE);
            return this;
        }
        if(imageView.getVisibility()!=View.VISIBLE)
            imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(logo);
        return this;
    }

    public LSnackBar setLogo(int logo){
        final ImageView imageView = mView.getLogoView();
        if(logo==0){
            imageView.setVisibility(View.GONE);
            return this;
        }
        if(imageView.getVisibility()!=View.VISIBLE)
            imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(logo);
        return this;
    }

    public LSnackBar setLogo(Drawable logo){
        final ImageView imageView = mView.getLogoView();
        if(logo==null){
            imageView.setVisibility(View.GONE);
            return this;
        }
        if(imageView.getVisibility()!=View.VISIBLE)
            imageView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(logo);
        return this;
    }

    /**
     * Update the text in this {@link LSnackBar}.
     *
     * @param resId The new text for the Toast.
     */
    @NonNull
    public LSnackBar setText(@StringRes int resId) {
        return setText(mContext.getText(resId));
    }

    /**
     * Set how long to show the view for.
     *
     * @param duration either be one of the predefined lengths:
     *                 {@link #LENGTH_SHORT}, {@link #LENGTH_LONG}, or a custom duration
     *                 in milliseconds.
     */
    @NonNull
    public LSnackBar setDuration(@Duration int duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Return the duration.
     *
     * @see #setDuration
     */
    @Duration
    public int getDuration() {
        return mDuration;
    }

    /**
     * Returns the {@link LSnackBar}'s view.
     */

    @NonNull
    public View getView() {
        return mView;
    }

    /**
     * Show the {@link LSnackBar}.
     */
    public void show() {
        SnackBarManager.getInstance().show(mDuration, mManagerCallback);
    }

    /**
     * Dismiss the {@link LSnackBar}.
     */
    public void dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    private void dispatchDismiss(@Callback.DismissEvent int event) {
        SnackBarManager.getInstance().dismiss(mManagerCallback, event);
    }

    /**
     * Set a callback to be called when this the visibility of this {@link LSnackBar} changes.
     */
    @NonNull
    public LSnackBar setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    /**
     * Return whether this {@link LSnackBar} is currently being shown.
     */
    public boolean isShown() {
        return SnackBarManager.getInstance().isCurrent(mManagerCallback);

    }

    /**
     * Returns whether this {@link LSnackBar} is currently being shown, or is queued to be
     * shown next.
     */
    public boolean isShownOrQueued() {
        return SnackBarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    final void showView() {
        if (mView.getParent() == null) {
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();
            if (lp instanceof CoordinatorLayout.LayoutParams) {
                // If our LayoutParams are from a CoordinatorLayout, we'll setup our Behavior

                final Behavior behavior = new Behavior();
                behavior.setStartAlphaSwipeDistance(0.1f);
                behavior.setEndAlphaSwipeDistance(0.6f);
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        view.setVisibility(View.GONE);
                        dispatchDismiss(Callback.DISMISS_EVENT_SWIPE);
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                        switch (state) {
                            case SwipeDismissBehavior.STATE_DRAGGING:
                            case SwipeDismissBehavior.STATE_SETTLING:

                                SnackBarManager.getInstance().cancelTimeout(mManagerCallback);
                                break;
                            case SwipeDismissBehavior.STATE_IDLE:

                                SnackBarManager.getInstance().restoreTimeout(mManagerCallback);
                                break;
                        }
                    }
                });
                ((CoordinatorLayout.LayoutParams) lp).setBehavior(behavior);
                int[] size = new int[2];
                int stateSize = ScreenUtil.getStatusHeight(mContext);
                int marginTop = 0;
                mParent.getLocationOnScreen(size);
                if(size[1]>stateSize){
                    marginTop = stateSize*-1;
                }else if(size[1] == 0){
                    marginTop = 0;
                }else{
                    marginTop = (size[1]+stateSize)*-1;
                }
                ((CoordinatorLayout.LayoutParams) lp).setMargins(0, marginTop, 0, 0);
            }
            mParent.addView(mView);
        }
        if (ViewCompat.isLaidOut(mView)) {
            animateViewIn();
        } else {

            mView.setOnLayoutChangeListener(new SnackbarLayout.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangeListener(null);
                }
            });
        }

        mView.setOnAttachStateChangeListener(new SnackbarLayout.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShownOrQueued()) {
                    // If we haven't already been dismissed then this event is coming from a
                    // non-user initiated action. Hence we need to make sure that we callback
                    // and keep our state up to date. We need to post the call since removeView()
                    // will call through to onDetachedFromWindow and thus overflow.
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onViewHidden(Callback.DISMISS_EVENT_MANUAL);
                        }
                    });
                }
            }
        });

        if (ViewCompat.isLaidOut(mView)) {
            if (shouldAnimate()) {
                // If animations are enabled, animate it in
                animateViewIn();
            } else {
                // Else if anims are disabled just call back now
                onViewShown();
            }
        } else {
            // Otherwise, add one of our layout change listeners and show it in when laid out
            mView.setOnLayoutChangeListener(new SnackbarLayout.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    mView.setOnLayoutChangeListener(null);

                    if (shouldAnimate()) {
                        // If animations are enabled, animate it in
                        animateViewIn();
                    } else {
                        // Else if anims are disabled just call back now
                        onViewShown();
                    }
                }
            });
        }
    }

    private void animateViewIn() {
        Animation anim;
        if (appearDirection == APPEAR_FROM_TOP_TO_DOWN) {
            anim = getAnimationInFromTopToDown();
        } else {
            anim = getAnimationInFromBottomToTop();
        }
        anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        anim.setDuration(ANIMATION_DURATION);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                onViewShown();
            }

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mView.startAnimation(anim);
    }

    private void animateViewOut(final int event) {
        Animation anim;

        if (appearDirection == APPEAR_FROM_TOP_TO_DOWN) {
            anim = getAnimationOutFromTopToDown();
        } else {
            anim = getAnimationOutFromBottomToTop();
        }
        anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        anim.setDuration(ANIMATION_DURATION);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                onViewHidden(event);
            }

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mView.startAnimation(anim);
    }

    private Animation getAnimationInFromTopToDown() {
        return AnimationUtils.loadAnimation(mView.getContext(), R.anim.top_in);
    }

    private Animation getAnimationInFromBottomToTop() {
        return AnimationUtils.loadAnimation(mView.getContext(), R.anim.design_snackbar_in);
    }
    private Animation getAnimationOutFromTopToDown() {
        return AnimationUtils.loadAnimation(mView.getContext(), R.anim.top_out);
    }

    private Animation getAnimationOutFromBottomToTop() {
        return AnimationUtils.loadAnimation(mView.getContext(), R.anim.design_snackbar_out);
    }

    final void hideView(@Callback.DismissEvent final int event) {
        if (shouldAnimate() && mView.getVisibility() == View.VISIBLE) {
            animateViewOut(event);
        } else {
            // If anims are disabled or the view isn't visible, just call back now
            onViewHidden(event);
        }
    }
    private void onViewShown() {
        SnackBarManager.getInstance().onShown(mManagerCallback);
        if (mCallback != null) {
            mCallback.onShown(this);
        }
    }

    private void onViewHidden(int event) {
        // First tell the SnackbarManager that it has been dismissed
        SnackBarManager.getInstance().onDismissed(mManagerCallback);
        // Now call the dismiss listener (if available)
        if (mCallback != null) {
            mCallback.onDismissed(this, event);
        }
        // Lastly, remove the view from the parent (if attached)
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }
    /**
     * Returns true if we should animate the Snackbar view in/out.
     */
    private boolean shouldAnimate() {
        return !mAccessibilityManager.isEnabled();
    }

    /**
     * @return if the view is being being dragged or settled by {@link SwipeDismissBehavior}.
     */
    private boolean isBeingDragged() {
        final ViewGroup.LayoutParams lp = mView.getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {
            final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) lp;
            final CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
            if (behavior instanceof SwipeDismissBehavior) {
                return ((SwipeDismissBehavior) behavior).getDragState() != SwipeDismissBehavior.STATE_IDLE;
            }
        }
        return false;
    }

    final class Behavior extends SwipeDismissBehavior<SnackbarLayout> {
        @Override
        public boolean canSwipeDismissView(View child) {
            return child instanceof SnackbarLayout;
        }

        @Override
        public boolean onInterceptTouchEvent(CoordinatorLayout parent, SnackbarLayout child,
                                             MotionEvent event) {
            // We want to make sure that we disable any Snackbar timeouts if the user is
            // currently touching the Snackbar. We restore the timeout when complete
            if (parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY())) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        SnackBarManager.getInstance().cancelTimeout(mManagerCallback);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        SnackBarManager.getInstance().restoreTimeout(mManagerCallback);
                        break;
                }
            }

            return super.onInterceptTouchEvent(parent, child, event);
        }
    }

    public static class SnackbarLayout extends LinearLayout {
        private TextView mMessageView;
        private Button mActionView;
        private ImageView mLogoView;

        private int mMaxWidth;
        private int mMaxInlineActionWidth;

        interface OnLayoutChangeListener {
            void onLayoutChange(View view, int left, int top, int right, int bottom);
        }

        interface OnAttachStateChangeListener {
            void onViewAttachedToWindow(View v);
            void onViewDetachedFromWindow(View v);
        }

        private OnLayoutChangeListener mOnLayoutChangeListener;
        private OnAttachStateChangeListener mOnAttachStateChangeListener;

        public SnackbarLayout(Context context) {
            this(context, null);
        }

        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
            mMaxInlineActionWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
            if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this, a.getDimensionPixelSize(
                        R.styleable.SnackbarLayout_elevation, 0));
            }
            a.recycle();
            setClickable(true);

            // Now inflate our content. We need to do this manually rather than using an <include>
            // in the layout since older versions of the Android do not inflate includes with
            // the correct Context.
            LayoutInflater.from(context).inflate(R.layout.view_lsnackbar_layout_include, this);
            ViewCompat.setAccessibilityLiveRegion(this,
                    ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
            ViewCompat.setImportantForAccessibility(this,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            mMessageView = (TextView) findViewById(R.id.snackbar_text);
            mActionView = (Button) findViewById(R.id.snackbar_action);
            mLogoView = (ImageView) findViewById(R.id.snackbar_logo);
        }

        TextView getMessageView() {
            return mMessageView;
        }

        Button getActionView() {
            return mActionView;
        }

        public ImageView getLogoView() {
            return mLogoView;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (mMaxWidth > 0 && getMeasuredWidth() > mMaxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
            final int multiLineVPadding = getResources().getDimensionPixelSize(
                    R.dimen.design_snackbar_padding_vertical_2lines);
            final int singleLineVPadding = getResources().getDimensionPixelSize(
                    R.dimen.design_snackbar_padding_vertical);
            final boolean isMultiLine = mMessageView.getLayout().getLineCount() > 1;
            boolean remeasure = false;
            if (isMultiLine && mMaxInlineActionWidth > 0
                    && mActionView.getMeasuredWidth() > mMaxInlineActionWidth) {
                if (updateViewsWithinLayout(VERTICAL, multiLineVPadding,
                        multiLineVPadding - singleLineVPadding)) {
                    remeasure = true;
                }
            } else {
                final int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
                if (updateViewsWithinLayout(HORIZONTAL, messagePadding, messagePadding)) {
                    remeasure = true;
                }
            }
            if (remeasure) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        void animateChildrenIn(int delay, int duration) {
            ViewCompat.setAlpha(mMessageView, 0f);
            ViewCompat.animate(mMessageView).alpha(1f).setDuration(duration)
                    .setStartDelay(delay).start();
            if (mActionView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mActionView, 0f);
                ViewCompat.animate(mActionView).alpha(1f).setDuration(duration)
                        .setStartDelay(delay).start();
            }
        }

        void animateChildrenOut(int delay, int duration) {
            ViewCompat.setAlpha(mMessageView, 1f);
            ViewCompat.animate(mMessageView).alpha(0f).setDuration(duration)
                    .setStartDelay(delay).start();
            if (mActionView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mActionView, 1f);
                ViewCompat.animate(mActionView).alpha(0f).setDuration(duration)
                        .setStartDelay(delay).start();
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (changed && mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }

        private boolean updateViewsWithinLayout(final int orientation,
                                                final int messagePadTop, final int messagePadBottom) {
            boolean changed = false;
            if (orientation != getOrientation()) {
                setOrientation(orientation);
                changed = true;
            }
            if (mMessageView.getPaddingTop() != messagePadTop
                    || mMessageView.getPaddingBottom() != messagePadBottom) {
                updateTopBottomPadding(mMessageView, messagePadTop, messagePadBottom);
                changed = true;
            }
            return changed;
        }

        private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
            if (ViewCompat.isPaddingRelative(view)) {
                ViewCompat.setPaddingRelative(view,
                        ViewCompat.getPaddingStart(view), topPadding,
                        ViewCompat.getPaddingEnd(view), bottomPadding);
            } else {
                view.setPadding(view.getPaddingLeft(), topPadding,
                        view.getPaddingRight(), bottomPadding);
            }
        }
    }
}
