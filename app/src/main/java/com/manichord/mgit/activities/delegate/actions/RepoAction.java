package com.manichord.mgit.activities.delegate.actions;

import com.manichord.mgit.activities.RepoDetailActivity;
import com.manichord.mgit.database.models.Repo;

public abstract class RepoAction {

    protected final Repo mRepo;
    protected final RepoDetailActivity mActivity;

    public RepoAction(Repo repo, RepoDetailActivity activity) {
        mRepo = repo;
        mActivity = activity;
    }

    public abstract void execute();
}
