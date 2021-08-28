package com.manichord.mgit.ui.delegate.actions;

import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;

public abstract class RepoAction {

    protected final Repo mRepo;
    protected final RepoDetailActivity mActivity;

    public RepoAction(Repo repo, RepoDetailActivity activity) {
        mRepo = repo;
        mActivity = activity;
    }

    public abstract void execute();
}
