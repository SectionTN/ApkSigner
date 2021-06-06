package com.light.apksigner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.light.apksigner.ApkSigner;
import com.light.apksigner.utils.IOUtils;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ApkSignerWrapper {
    private static final String TAG = "ASWrapper";
    private static final String FILE_NAME_PAST = "testkey.past";
    private static final String FILE_NAME_PRIVATE_KEY = "testkey.pk8";

    @SuppressLint("StaticFieldLeak")// a p p l i c a t i o n   c o n t e x t
    private static ApkSignerWrapper sInstance;

    private Context mContext;
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ApkSigner mApkSigner;

    public static ApkSignerWrapper getInstance(Context c) {
        return sInstance != null ? sInstance : new ApkSignerWrapper(c);
    }

    private ApkSignerWrapper(Context c) {
        mContext = c.getApplicationContext();
        sInstance = this;
    }

    public interface SignerCallback {
        void onSigningSucceeded(File signedApkFile);

        void onSigningFailed(Exception error);
    }

    public void sign(final File inputApkFile, final File outputSignedApkFile, final SignerCallback callback) {
        mExecutor.execute(new Runnable(){
            @Override
            public void run(){
                try {
                    if (mApkSigner == null)
                        mApkSigner = new ApkSigner("TEST");

                    mApkSigner.sign(inputApkFile, outputSignedApkFile);
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            callback.onSigningSucceeded(outputSignedApkFile);
                        }
                    });
                } catch (final Exception e) {
                    Log.w(TAG, e);
                    mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                callback.onSigningFailed(e);
                            }
                        });
                }
            }
        });
    }

    private File getSigningEnvironmentDir() {
        return new File(mContext.getFilesDir(), "signing");
    }
}
