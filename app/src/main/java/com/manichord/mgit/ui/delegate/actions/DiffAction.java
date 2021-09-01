package com.manichord.mgit.ui.delegate.actions;

import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;

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
