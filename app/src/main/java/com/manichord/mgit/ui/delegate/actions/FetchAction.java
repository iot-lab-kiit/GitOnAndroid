package com.manichord.mgit.ui.delegate.actions;

import android.app.AlertDialog;
import android.app.Dialog;

import java.util.ArrayList;

import me.sheimi.sgit.R;
import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.ui.dialogs.DummyDialogListener;
import com.manichord.mgit.tasks.FetchTask;

public class FetchAction extends RepoAction {
    public FetchAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        fetchDialog().show();
        mActivity.closeOperationDrawer();
    }

    private void fetch(String[] remotes) {
        final FetchTask fetchTask = new FetchTask(remotes, mRepo, mActivity.new ProgressCallback(R.string.fetch_msg_init));
        fetchTask.executeTask();
    }

    private Dialog fetchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final String[] originRemotes = mRepo.getRemotes().toArray(new String[0]);
        final ArrayList<String> remotes = new ArrayList<>();
        return builder.setTitle(R.string.dialog_fetch_title)
                .setMultiChoiceItems(originRemotes, null, (dialogInterface, index, isChecked) -> {
                    if (isChecked) {
                        remotes.add(originRemotes[index]);
                    } else {
                        for (int i = 0; i < remotes.size(); ++i) {
                            if (remotes.get(i).equals(originRemotes[index])) {
                                remotes.remove(i);
                            }
                        }
                    }
                })
                .setPositiveButton(R.string.dialog_fetch_positive_button, (dialogInterface, i) -> fetch(remotes.toArray(new String[0])))
                .setNeutralButton(R.string.dialog_fetch_all_button, (dialogInterface, i) -> fetch(originRemotes))
                .setNegativeButton(android.R.string.cancel, new DummyDialogListener())
                .create();
    }
}
