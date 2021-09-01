package com.manichord.mgit.ui.delegate.actions;

import com.manichord.mgitt.R;

import com.manichord.mgit.tasks.RepoOpTask;
import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.tasks.CheckoutTask;

/**
 * Created by liscju - piotr.listkiewicz@gmail.com on 2015-03-15.
 */
public class NewBranchAction extends RepoAction {
    public NewBranchAction(Repo mRepo, RepoDetailActivity mActivity) {
        super(mRepo,mActivity);
    }

    @Override
    public void execute() {
        mActivity.showEditTextDialog(R.string.dialog_create_branch_title,
                R.string.dialog_create_branch_hint,R.string.label_create,
            branchName -> {
                CheckoutTask checkoutTask = new CheckoutTask(mRepo, null, branchName,
                        new ActivityResetPostCallback(branchName));
                checkoutTask.executeTask();
            });
        mActivity.closeOperationDrawer();
    }

    private class ActivityResetPostCallback implements RepoOpTask.AsyncTaskPostCallback {
        private final String mBranchName;
        public ActivityResetPostCallback(String branchName) {
            mBranchName = branchName;
        }
        @Override
        public void onPostExecute(Boolean isSuccess) {
            mActivity.reset(mBranchName);
        }
    }
}
