package cuiliang.quicker.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import cuiliang.quicker.R;
import cuiliang.quicker.network.NetWorkManager;
import okhttp3.Response;

/**
 * Created by Void on 2020/4/12 16:23
 */
public class ShareDialog implements
        RadioGroup.OnCheckedChangeListener,
        View.OnClickListener,
        NetWorkManager.RequestCallback {
    private Handler handler = new Handler(Looper.getMainLooper());
    private AlertDialog dialog;
    private TextView inputHintTv;
    private EditText inputContentEt;
    private Context context;
    private int contentType = 1;

    public ShareDialog(Context context) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.layout_input_user_share_data, null);
        RadioGroup radioGroup = view.findViewById(R.id.inputType);
        inputHintTv = view.findViewById(R.id.inputHint);
        inputContentEt = view.findViewById(R.id.inputContent);
        view.findViewById(R.id.acceptBtn).setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);
        dialog = new AlertDialog.Builder(context).setView(view).create();
    }

    public void showShareDialog() {
        dialog.show();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.inputRB:
                inputHintTv.setVisibility(View.GONE);
                contentType = 0;
                break;
            case R.id.pasteRB:
                inputHintTv.setVisibility(View.VISIBLE);
                inputHintTv.setText("注：请确认电脑鼠标的焦点在编辑状态");
                contentType = 1;
                break;
            case R.id.actionRB:
                inputHintTv.setVisibility(View.VISIBLE);
                inputHintTv.setText("注：使用动作名时，需保证没有重名的动作");
                contentType = 2;
                break;
            default:
                inputHintTv.setVisibility(View.GONE);
                contentType = -1;
                ToastUtils.showShort(context, "选择分享类型异常！contentType：" + contentType);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.acceptBtn || contentType <= -1) return;
        String tmp = inputContentEt.getText().toString();
        if (TextUtils.isEmpty(tmp)) {
            ToastUtils.showShort(context, "不允许发送空内容！");
        } else {
            switch (contentType) {
                case 0:
                    ShareDataToPCManager.getInstant().shareTextToPc(tmp, this);
                    break;
                case 1:
                    ShareDataToPCManager.getInstant().shareTextToPc_(tmp, this);
                    break;
                case 2:
                    ShareDataToPCManager.getInstant().shareActionToPc(tmp, this);
                    break;
            }
        }
        dialog.dismiss();
    }

    @Override
    public void onSuccess(@NotNull Response response) {
        KLog.e("code:" + response.code() + ";message:" + response.message());
        handler.post(() -> ToastUtils.showShort(context, "发送成功"));
    }

    @Override
    public void onError(@NotNull IOException e, @Nullable String errorMessage) {
        handler.post(() -> ToastUtils.showShort(context, "分享失败！"));
    }
}
