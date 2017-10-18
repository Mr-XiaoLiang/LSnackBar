package com.liang.lollipop.lsnackbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

/**
 * Created by LiuJ on 2017/3/16.
 * 自定义的模仿qq的土司
 */
public class LToast extends Toast {

    private static LToast lToast;

    /** @hide */
    @IntDef({LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {}

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     */
    public LToast(Context context) {
        super(context);
    }

    public static LToast makeText(Context context, CharSequence text,@Duration int duration){
        return makeText(context,text,Color.WHITE,0x88000000,0,duration);
    }

    /**
     * Make a standard toast that just contains a text view.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     *
     */
    public static LToast makeText(Context context, CharSequence text,int textColor,int bgColor,int iconRes, @Duration int duration) {
        View root;
        if(lToast==null){
            lToast = new LToast(context);
            root = lToast.createRootView(context);
            lToast.setView(root);
            lToast.setDuration(duration);
            int gravity = Gravity.TOP|Gravity.FILL_HORIZONTAL;
            lToast.setGravity(gravity,0,0);
        }
        root = lToast.getView();
        //设置文字
        TextView tv = (TextView)root.findViewById(R.id.toast_alert_l_msg);
        ImageView iv = (ImageView) root.findViewById(R.id.toast_alert_l_icon);
        View bg = root.findViewById(R.id.toast_alert_l);
        if(tv==null){
            root = lToast.createRootView(context);
            lToast.setView(root);
            tv = (TextView)root.findViewById(R.id.toast_alert_l_msg);
            iv = (ImageView) root.findViewById(R.id.toast_alert_l_icon);
            bg = root.findViewById(R.id.toast_alert_l);
        }
        if(iconRes==0){
            iv.setVisibility(View.GONE);
        }else{
            iv.setVisibility(View.VISIBLE);
        }
        iv.setImageResource(iconRes);
        tv.setText(text);
        tv.setTextColor(textColor);
        bg.setBackgroundColor(bgColor);
        return lToast;
    }

    private View createRootView(Context context){
        LayoutInflater inflate = LayoutInflater.from(context);
        View root = inflate.inflate(R.layout.toast_alert_l,null);
        View body = root.findViewById(R.id.toast_alert_l);
        body.setMinimumHeight(ScreenUtil.getActionBarHeight(context));
        return root;
    }
}
