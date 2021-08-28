package com.manichord.mgit.ui.delegate.actions;

import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.tasks.AddToStageTask;

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
