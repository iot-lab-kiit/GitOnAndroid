package com.manichord.mgit.activities.delegate.actions;

import me.sheimi.sgit.R;
import com.manichord.mgit.activities.RepoDetailActivity;
import com.manichord.mgit.database.models.Repo;

public class DeleteAction extends RepoAction {

    public DeleteAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showMessageDialog(R.string.dialog_delete_repo_title,
                R.string.dialog_delete_repo_msg, R.string.label_delete,
            (dialogInterface, i) -> {
                mRepo.deleteRepo();
                mActivity.finish();
            });
        mActivity.closeOperationDrawer();
    }
}
