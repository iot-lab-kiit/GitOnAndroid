package com.manichord.mgit.ui.dialogs;

import java.util.Objects;

import com.manichord.mgit.ui.fragments.SheimiDialogFragment;
import me.sheimi.sgit.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

import com.manichord.mgit.models.Repo;
import com.manichord.mgit.ui.RepoDetailActivity;

/**
 * Created by sheimi on 8/24/13.
 */

public class CheckoutDialog extends SheimiDialogFragment implements
        View.OnClickListener, DialogInterface.OnClickListener {

    private String mCommit;
    private EditText mBranchName;
    private RepoDetailActivity mActivity;
    public static final String BASE_COMMIT = "base commit";

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (RepoDetailActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        Bundle args = getArguments();
        if (args != null && args.containsKey(BASE_COMMIT)) {
            mCommit = args.getString(BASE_COMMIT);
        } else {
	    mCommit = "";
	}

        Repo mRepo = (Repo) Objects.requireNonNull(args).getSerializable(Repo.TAG);

	String message = getString(R.string.dialog_comfirm_checkout_commit_msg)
	    + " "
	    + Repo.getCommitDisplayName(mCommit);

        builder.setTitle(getString(R.string.dialog_comfirm_checkout_commit_title));
        View view = mActivity.getLayoutInflater().inflate(
                R.layout.dialog_checkout, null);

        builder.setView(view);
        mBranchName = view.findViewById(R.id.newBranchName);

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
	builder.setNeutralButton(R.string.label_anonymous_checkout, this);
        builder.setPositiveButton(R.string.label_checkout,
                new DummyDialogListener());

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BASE_COMMIT, mCommit);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;
	Button positiveButton = dialog
	    .getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	String newBranch = mBranchName.getText().toString().trim();
	mActivity.getRepoDelegate().checkoutCommit(mCommit, newBranch);	
        dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
	mActivity.getRepoDelegate().checkoutCommit(mCommit);	
        dismiss();
    }
}
