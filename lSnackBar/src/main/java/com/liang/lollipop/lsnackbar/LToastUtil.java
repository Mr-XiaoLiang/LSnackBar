package com.liang.lollipop.lsnackbar;

import android.content.Context;
import android.graphics.Color;

/**
 * Created by liuj on 2016/10/6.
 * 土司工具
 */

public class LToastUtil {
    public static void T(Context context,String t,int textColor,int bgColor,int logo){
        if(context!=null){
            LToast.makeText(context,t,textColor,bgColor,logo,LToast.LENGTH_SHORT).show();
        }
    }

    public static void T(Context context,String t,int bgColor,int logo){
        if(context!=null){
            LToast.makeText(context,t, Color.WHITE,bgColor,logo,LToast.LENGTH_SHORT).show();
        }
    }

}
