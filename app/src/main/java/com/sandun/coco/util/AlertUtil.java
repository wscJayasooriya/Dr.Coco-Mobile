package com.sandun.coco.util;

import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.sandun.coco.Register;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AlertUtil {
    public static void showAlert(AppCompatActivity activity,String title, String message,int alertType) {
        showSweetAlertDialog(activity, title, message, alertType);
    }

    public static void showSweetAlertDialog(AppCompatActivity activity, String title, String content, int alertType) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(activity, alertType);
        sweetAlertDialog.setTitleText(title);
        sweetAlertDialog.setContentText(content);
        sweetAlertDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sweetAlertDialog.isShowing()) {
                    sweetAlertDialog.dismissWithAnimation();
                }
            }
        }, 5000); // 5 seconds delay
    }
}
