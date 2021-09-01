package com.manichord.mgit.ui.delegate.actions;

import android.content.Intent;

import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.ui.ViewFileActivity;
import com.manichord.mgit.models.Repo;

/**
 * Created by phcoder on 05.12.15.
 */
public class RawConfigAction extends RepoAction {

    public RawConfigAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        Intent intent = new Intent(mActivity, ViewFileActivity.class);
        intent.putExtra(ViewFileActivity.TAG_FILE_NAME,
                mRepo.getDir().getAbsoluteFile() + "/.git/config");
        mActivity.startActivity(intent);
    }
}
