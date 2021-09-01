package com.manichord.mgit.ui.delegate.actions;

import android.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;

import com.manichord.mgitt.R;
import com.manichord.mgit.ui.RepoDetailActivity;
import com.manichord.mgit.models.GitConfig;
import com.manichord.mgit.models.Repo;
import com.manichord.mgitt.databinding.DialogRepoConfigBinding;
import com.manichord.mgit.utils.exception.StopTaskException;
import timber.log.Timber;

/**
 * Action to display configuration for a Repo
 */
public class ConfigAction extends RepoAction {


    public ConfigAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {

        try {
            DialogRepoConfigBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_repo_config, null, false);
            GitConfig gitConfig = new GitConfig(mRepo);
            binding.setViewModel(gitConfig);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setView(binding.getRoot())
                .setNeutralButton(R.string.label_done, null)
                .create().show();

        } catch (StopTaskException e) {
            //FIXME: show error to user
            Timber.e(e);
        }
    }

}
