package com.manichord.mgit.ui.delegate.actions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

import com.manichord.mgit.ui.fragments.SheimiDialogFragment;
import me.sheimi.sgit.R;
import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.ui.dialogs.DummyDialogListener;
import timber.log.Timber;

public class RemoveRemoteAction extends RepoAction {

    public RemoveRemoteAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        Set<String> remotes = mRepo.getRemotes();
        if (remotes == null || remotes.isEmpty()) {
            mActivity.showToastMessage(R.string.alert_please_add_a_remote);
            return;
        }

        RemoveRemoteDialog dialog = new RemoveRemoteDialog();
        dialog.setArguments(mRepo.getBundle());
        dialog.show(mActivity.getSupportFragmentManager(), "remove-remote-dialog");
        mActivity.closeOperationDrawer();
    }

    public static void removeRemote(Repo repo, RepoDetailActivity activity, String remote) throws IOException {
        repo.removeRemote(remote);
        activity.showToastMessage(R.string.success_remote_removed);
    }

    public static class RemoveRemoteDialog extends SheimiDialogFragment {
        private Repo mRepo;
        private RepoDetailActivity mActivity;
        private ArrayAdapter<String> mAdapter;

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            Bundle args = getArguments();
            if (args != null && args.containsKey(Repo.TAG)) {
                mRepo = (Repo) args.getSerializable(Repo.TAG);
            }

            mActivity = (RepoDetailActivity) getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            LayoutInflater inflater = mActivity.getLayoutInflater();

            View layout = inflater.inflate(R.layout.dialog_remove_remote, null);
            ListView mRemoteList = layout.findViewById(R.id.remoteList);

            mAdapter = new ArrayAdapter<String>(mActivity,
                    android.R.layout.simple_list_item_1);
            Set<String> remotes = mRepo.getRemotes();
            mAdapter.addAll(remotes);
            mRemoteList.setAdapter(mAdapter);

            mRemoteList.setOnItemClickListener((parent, view, position, id) -> {
                String remote = mAdapter.getItem(position);
                try {
                    removeRemote(mRepo, mActivity, remote);
                } catch (IOException e) {
                    Timber.e(e);
                    mActivity.showMessageDialog(R.string.dialog_error_title, getString(R.string.error_something_wrong));
                }
                dismiss();
            });

            builder.setTitle(R.string.dialog_remove_remote_title)
                    .setView(layout)
                    .setNegativeButton(R.string.label_cancel, new DummyDialogListener());
            return builder.create();
        }
    }

}
