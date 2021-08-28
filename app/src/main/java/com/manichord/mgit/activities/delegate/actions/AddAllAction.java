package com.manichord.mgit.activities.delegate.actions;

import com.manichord.mgit.activities.RepoDetailActivity;
import com.manichord.mgit.database.models.Repo;
import com.manichord.mgit.tasks.repo.AddToStageTask;

public class AddAllAction extends RepoAction {

    public AddAllAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        AddToStageTask addTask = new AddToStageTask(mRepo, ".");
        addTask.executeTask();
        mActivity.closeOperationDrawer();
    }

}
