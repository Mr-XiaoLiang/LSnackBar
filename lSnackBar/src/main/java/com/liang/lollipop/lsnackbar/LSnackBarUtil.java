package com.liang.lollipop.lsnackbar;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by LiuJ on 2017/07/26.
 * LSnackBar的封装工具
 */
public class LSnackBarUtil {

    private static LSnackBar lSnackBar;

    public static LSnackBar makeSnack(@NonNull View view,@NonNull String msg, String btnName, View.OnClickListener btnClick,@LSnackBar.Duration int duration, @LSnackBar.OverSnackAppearDirection int type){
        return LSnackBar.make(view,msg,duration,type).setAction(btnName,btnClick);
    }

    public static void showToast(@NonNull View view,@NonNull String msg,String btnName, View.OnClickListener btnClick,int logoId,int bgColor,int msgColor,int actionColor){
        makeSnack(view,msg,btnName,btnClick,LSnackBar.LENGTH_SHORT,LSnackBar.APPEAR_FROM_TOP_TO_DOWN)
                .setLogo(logoId).setBackgroundColor(bgColor)
                .setMessageTextColor(msgColor).setActionTextColor(actionColor).show();
//        if(lSnackBar==null)
//            lSnackBar = makeSnack(view,msg,btnName,btnClick,LSnackBar.LENGTH_LONG,LSnackBar.APPEAR_FROM_TOP_TO_DOWN).setLogo(logoId).setBackgroundColor(bgColor);
//        else
//            lSnackBar.setText(msg).setAction(btnName,btnClick).setLogo(logoId).setBackgroundColor(bgColor);
//        lSnackBar.show();
    }

    public static void showToast(@NonNull View view,@NonNull String msg,String btnName, View.OnClickListener btnClick,int logoId,int bgColor){
        showToast(view,msg,btnName,btnClick,logoId,bgColor, Color.WHITE,Color.WHITE);
//        if(lSnackBar==null)
//            lSnackBar = makeSnack(view,msg,btnName,btnClick,LSnackBar.LENGTH_LONG,LSnackBar.APPEAR_FROM_TOP_TO_DOWN).setLogo(logoId).setBackgroundColor(bgColor);
//        else
//            lSnackBar.setText(msg).setAction(btnName,btnClick).setLogo(logoId).setBackgroundColor(bgColor);
//        lSnackBar.show();
    }

    public static void showToast(@NonNull View view,@NonNull String msg,int logoId,int bgColor){
        showToast(view,msg,"",null,logoId,bgColor, Color.WHITE,Color.WHITE);
//        if(lSnackBar==null)
//            lSnackBar = makeSnack(view,msg,null,null,LSnackBar.LENGTH_LONG,LSnackBar.APPEAR_FROM_TOP_TO_DOWN).setLogo(logoId).setBackgroundColor(bgColor);
//        else
//            lSnackBar.setText(msg).setLogo(logoId).setBackgroundColor(bgColor);
//        lSnackBar.show();
    }

}
