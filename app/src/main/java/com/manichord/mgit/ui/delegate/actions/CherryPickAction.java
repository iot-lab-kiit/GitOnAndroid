package com.manichord.mgit.ui.delegate.actions;

import com.manichord.mgitt.R;
import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.tasks.CherryPickTask;

public class CherryPickAction extends RepoAction {

    public CherryPickAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showEditTextDialog(R.string.dialog_cherrypick_title,
                R.string.dialog_cherrypick_msg_hint,
                R.string.dialog_label_cherrypick,
            text -> cherrypick(text));
        mActivity.closeOperationDrawer();
    }

    public void cherrypick(String commit) {
        CherryPickTask task = new CherryPickTask(mRepo, commit, isSuccess -> mActivity.reset());
        task.executeTask();
    }

}
