package com.liang.lollipop.demo;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.liang.lollipop.lsnackbar.LSnackBar;
import com.liang.lollipop.lsnackbar.LToastUtil;

public class MainActivity extends AppCompatActivity implements CheckBox.OnCheckedChangeListener,View.OnClickListener {

    private boolean local = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        CheckBox checkBox = (CheckBox) findViewById(R.id.local);
        checkBox.setOnCheckedChangeListener(this);
        findViewById(R.id.snack_bar1).setOnClickListener(this);
        findViewById(R.id.snack_bar2).setOnClickListener(this);
        findViewById(R.id.snack_bar_action).setOnClickListener(this);
        findViewById(R.id.toast).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.local:
                local = b;
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        LSnackBar snackBar;
        if(local){
            snackBar = LSnackBar.makeLocal(view,"这是测试数据",LSnackBar.LENGTH_SHORT);
        }else{
            snackBar = LSnackBar.make(view,"这是测试数据",LSnackBar.LENGTH_SHORT);
        }
        switch (view.getId()){
            case R.id.snack_bar1:
                snackBar.setBackgroundColor(0x88000000).show();
                break;
            case R.id.snack_bar2:
                snackBar.setLogo(R.mipmap.ic_launcher_round)
                        .setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary))
                        .show();
                break;
            case R.id.snack_bar_action:
                snackBar.setAction("Action", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LSnackBar.make(view,"点击了Action",LSnackBar.LENGTH_SHORT).show();
                    }
                }).show();
                break;
            case R.id.toast:
                LToastUtil.T(this,"Toast 显示",0x88000000,R.mipmap.ic_launcher);
                break;
            default:
                break;
        }
    }
}
