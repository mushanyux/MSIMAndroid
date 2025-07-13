package com.chat.base.utils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.base.MSBaseApplication;
import com.chat.base.R;


public class MSToastUtils {
    private MSToastUtils() {
    }

    private static class ToastUtilsBinder {
        private static final MSToastUtils utils = new MSToastUtils();
    }

    public static MSToastUtils getInstance() {
        return ToastUtilsBinder.utils;
    }


    public void showToastSuccess(String msg) {
        showToast(msg, 1);
    }

    public void showToastFail(String msg) {
        showToast(msg, 2);
    }

    public void showToastNormal(String msg) {
        showToast(msg, 3);
    }

    /**
     * 显示一个toast
     *
     * @param msg  显示内容
     * @param type 1：成功，2：失败，3：普通
     */
    private void showToast(String msg, int type) {
        Toast toast = Toast.makeText(MSBaseApplication.getInstance().getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 400);
        toast.show();
    }

    public void showToast(String msg) {
        Toast toast = Toast.makeText(MSBaseApplication.getInstance().getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 400);
        toast.show();
    }

    private View getToastView(String content, int type) {
        View view = LayoutInflater.from(MSBaseApplication.getInstance().getContext()).inflate(R.layout.ms_toast_layout, null);
        TextView toastTv = view.findViewById(R.id.toastTv);
        toastTv.setText(content);
        ImageView toastIv = view.findViewById(R.id.toastIv);

        toastIv.setImageResource(R.mipmap.icon_info);
        view.setBackgroundResource(R.drawable.toast_bg_normal);
        return view;
    }
}
