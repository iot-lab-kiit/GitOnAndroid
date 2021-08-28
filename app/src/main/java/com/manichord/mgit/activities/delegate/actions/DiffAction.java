package com.manichord.mgit.activities.delegate.actions;

import com.manichord.mgit.activities.RepoDetailActivity;
import com.manichord.mgit.database.models.Repo;

public class DiffAction extends RepoAction {

    public DiffAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.enterDiffActionMode();
        mActivity.closeOperationDrawer();
    }
}
