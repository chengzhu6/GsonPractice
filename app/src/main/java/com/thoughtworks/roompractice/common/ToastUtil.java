package com.thoughtworks.roompractice.common;

import android.widget.Toast;

public class ToastUtil {
    public static void showToast(String message) {
        Toast.makeText(MyApplication.getMyContext(), message, Toast.LENGTH_SHORT).show();
    }
}
