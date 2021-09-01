package com.manichord.mgit.ui.delegate.actions;

import com.manichord.mgitt.R;
import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.Repo;

public class NewDirAction extends RepoAction {

    public NewDirAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showEditTextDialog(R.string.dialog_create_dir_title,
                R.string.dialog_create_dir_hint, R.string.label_create,
            text -> mActivity.getFilesFragment().newDir(text));
        mActivity.closeOperationDrawer();
    }
}
