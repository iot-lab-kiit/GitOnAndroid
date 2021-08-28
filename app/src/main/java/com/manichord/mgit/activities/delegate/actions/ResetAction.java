package com.manichord.mgit.activities.delegate.actions;

import me.sheimi.sgit.R;
import com.manichord.mgit.activities.RepoDetailActivity;
import com.manichord.mgit.database.models.Repo;

import com.manichord.mgit.tasks.repo.ResetCommitTask;

public class ResetAction extends RepoAction {

    public ResetAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showMessageDialog(R.string.dialog_reset_commit_title,
                R.string.dialog_reset_commit_msg, R.string.action_reset,
            (dialogInterface, i) -> reset());
        mActivity.closeOperationDrawer();
    }

    public void reset() {
        ResetCommitTask resetTask = new ResetCommitTask(mRepo,
            isSuccess -> mActivity.reset());
        resetTask.executeTask();
    }
}
