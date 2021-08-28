package com.manichord.mgit.activities.delegate.actions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

import com.manichord.mgit.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import com.manichord.mgit.activities.RepoDetailActivity;
import com.manichord.mgit.database.models.Repo;
import com.manichord.mgit.dialogs.DummyDialogListener;
import com.manichord.mgit.tasks.repo.PullTask;

public class PullAction extends RepoAction {

    public PullAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
	Set<String> remotes = mRepo.getRemotes();
        if (remotes == null || remotes.isEmpty()) {
            mActivity.showToastMessage(R.string.alert_please_add_a_remote);
            return;
        }
        PullDialog pd = new PullDialog();
        pd.setArguments(mRepo.getBundle());
        pd.show(mActivity.getSupportFragmentManager(), "pull-repo-dialog");
        mActivity.closeOperationDrawer();
    }

    private static void pull(Repo repo, RepoDetailActivity activity,
			     String remote, boolean forcePull) {
        PullTask pullTask = new PullTask(repo, remote, forcePull, activity.new ProgressCallback(
                R.string.pull_msg_init));
        pullTask.executeTask();
        activity.closeOperationDrawer();
    }

    public static class PullDialog extends SheimiDialogFragment {

        private Repo mRepo;
        private RepoDetailActivity mActivity;
        private CheckBox mForcePull;
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

            View layout = inflater.inflate(R.layout.dialog_pull, null);
            mForcePull = layout.findViewById(R.id.forcePull);
            ListView mRemoteList = layout.findViewById(R.id.remoteList);

            mAdapter = new ArrayAdapter<String>(mActivity,
                    android.R.layout.simple_list_item_1);
            Set<String> remotes = mRepo.getRemotes();
            mAdapter.addAll(remotes);
            mRemoteList.setAdapter(mAdapter);

            mRemoteList.setOnItemClickListener((parent, view, position, id) -> {
                String remote = mAdapter.getItem(position);
                boolean isForcePull = mForcePull.isChecked();
                pull(mRepo, mActivity, remote, isForcePull);
                dismiss();
            });

            builder.setTitle(R.string.dialog_pull_repo_title)
                    .setView(layout)
                    .setNegativeButton(R.string.label_cancel,
                            new DummyDialogListener());
            return builder.create();
        }
    }

}
