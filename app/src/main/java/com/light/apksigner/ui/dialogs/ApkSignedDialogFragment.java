package com.light.apksigner.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import com.light.apksigner.R;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import com.light.apksigner.BuildConfig;
import android.content.DialogInterface;

public class ApkSignedDialogFragment extends DialogFragment {
    public static final String EXTRA_APK_FILE = "apk_file";

    private File mApkFile;

    public static ApkSignedDialogFragment newInstance(File apkFile) {
        ApkSignedDialogFragment fragment = new ApkSignedDialogFragment();

        Bundle args = new Bundle();
        args.putString(EXTRA_APK_FILE, apkFile.getAbsolutePath());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null)
            return;
        mApkFile = new File(args.getString(EXTRA_APK_FILE));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.signer_apk_signed)
                .setMessage(getString(R.string.signer_apk_signed_desc, mApkFile.getPath()))
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }})
                .setNegativeButton(R.string.signer_install, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                    Intent installApkIntent = new Intent(Intent.ACTION_VIEW);
                    installApkIntent.setDataAndType(FileProvider.getUriForFile(getContext(), "com.light.apksigner.provider", mApkFile), "application/vnd.android.package-archive");
                    installApkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(installApkIntent);
                    dialog.dismiss();
                }})
                .create();

        return dialog;
    }
}
