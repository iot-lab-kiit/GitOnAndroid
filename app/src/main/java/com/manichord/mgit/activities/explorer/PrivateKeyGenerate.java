package com.manichord.mgit.activities.explorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import com.manichord.mgit.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import com.manichord.mgit.ssh.PrivateKeyUtils;

public class PrivateKeyGenerate extends SheimiDialogFragment {

    private EditText mNewFilename;
    private EditText mKeyLength;
    private RadioButton mDSAButton;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
	View view;
	view = inflater.inflate(R.layout.dialog_generate_key, null);
	mNewFilename = view.findViewById(R.id.newFilename);
	mKeyLength = view.findViewById(R.id.key_size);
	mKeyLength.setText("4096");
	mDSAButton = view.findViewById(R.id.radio_dsa);
        RadioButton mRSAButton = view.findViewById(R.id.radio_rsa);
	mRSAButton.setChecked(true);
        builder.setMessage(R.string.label_dialog_generate_key)
	    .setView(view)
	    .setPositiveButton(R.string.label_generate_key, (dialog, id) -> generateKey())
	    .setNegativeButton(R.string.label_cancel, (dialog, id) -> {
        // Nothing to do
        });
        return builder.create();
    }

    private void generateKey() {
	String newFilename = mNewFilename.getText().toString().trim();

	if (newFilename.equals("")) {
            showToastMessage(R.string.alert_new_filename_required);
            mNewFilename
                    .setError(getString(R.string.alert_new_filename_required));
            return;
        }

        if (newFilename.contains("/")) {
            showToastMessage(R.string.alert_filename_format);
            mNewFilename.setError(getString(R.string.alert_filename_format));
            return;
        }

	int key_size = Integer.parseInt(mKeyLength.getText().toString());

	if (key_size < 1024) {
            showToastMessage(R.string.alert_too_short_key_size);
            mNewFilename.setError(getString(R.string.alert_too_short_key_size));
            return;
        }
	if (key_size > 16384) {
            showToastMessage(R.string.alert_too_long_key_size);
            mNewFilename.setError(getString(R.string.alert_too_long_key_size));
            return;
        }
	int type = mDSAButton.isChecked() ? KeyPair.DSA : KeyPair.RSA;
	File newKey = new File(PrivateKeyUtils.getPrivateKeyFolder(), newFilename);
	File newPubKey = new File(PrivateKeyUtils.getPublicKeyFolder(), newFilename);

	try {
	    JSch jsch=new JSch();
	    KeyPair kpair=KeyPair.genKeyPair(jsch, type, key_size);
	    kpair.writePrivateKey(new FileOutputStream(newKey));
	    kpair.writePublicKey(new FileOutputStream(newPubKey), "sgit");
	    kpair.dispose();
	} catch (Exception e) {
	    //TODO 
	    e.printStackTrace();
	}

		((PrivateKeyManageActivity) Objects.requireNonNull(getActivity())).refreshList();
    }
}
