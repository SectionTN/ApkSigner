package com.light.apksigner;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.view.View;

import com.light.apksigner.ui.dialogs.ApkSignedDialogFragment;
import com.light.apksigner.ui.dialogs.FilePickerDialogFragment;
import com.light.apksigner.utils.AlertsUtils;
import com.light.apksigner.utils.PermissionsUtils;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import android.view.View.OnClickListener;
import androidx.lifecycle.*;
import com.light.apksigner.utils.*;

public class MainActivity extends AppCompatActivity implements FilePickerDialogFragment.OnFilesSelectedListener {

    private Button mSignButton;
    private SignerViewModel mSignerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSignButton = findViewById(R.id.button_sign);
        
        mSignButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    checkPermissionsAndPickFiles();
                }
            });
        
        mSignerViewModel = ViewModelProviders.of(this).get(SignerViewModel.class);
        mSignerViewModel.getState().observe(this, new Observer<SignerViewModel.State>(){
			@Override
			public void onChanged(SignerViewModel.State state){
				switch (state) {
					case IDLE:
						mSignButton.setText(R.string.signer_sign);
						mSignButton.setEnabled(true);
						break;
					case SIGNING:
						mSignButton.setText(R.string.signer_signing);
						mSignButton.setEnabled(false);
						break;
				}
			}
		});
        
        mSignerViewModel.getEvents().observe(this, new Observer<Event<String[]>>() {
			public void onChange(Event<String[]> event){
            if (event.isConsumed())
                return;

            String[] eventData = event.consume();
            switch (eventData[0]) {
                case SignerViewModel.EVENT_SIGNING_SUCCEED:
                    ApkSignedDialogFragment.newInstance(new File(eventData[1])).show(getSupportFragmentManager(), "apk_signed");
                    break;
                case SignerViewModel.EVENT_SIGNING_FAILED:
                    AlertsUtils.showAlert(MainActivity.this, getString(R.string.common_error), getString(R.string.signer_signing_failed, eventData[1]));
                    break;
            }
		}
        });
    }

    private void checkPermissionsAndPickFiles() {
        if (!PermissionsUtils.checkAndRequestStoragePermissions(this))
            return;

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.offset = Environment.getExternalStorageDirectory();
        properties.extensions = new String[]{"apk"};

        FilePickerDialogFragment.newInstance(null, getString(R.string.signer_pick_apks), properties).show(getSupportFragmentManager(), "dialog_files_picker");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionsUtils.REQUEST_CODE_STORAGE_PERMISSIONS) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED)
                AlertsUtils.showAlert(this, R.string.common_error, R.string.signer_permissions_denied);
            else
                checkPermissionsAndPickFiles();
        }
    }

    @Override
    public void onFilesSelected(String tag, List<File> files) {
        mSignerViewModel.sign(files.get(0));
    }
}
