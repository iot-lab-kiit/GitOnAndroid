package com.manichord.mgit.ui.explorer;

import android.content.Intent;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;

import java.io.File;
import java.io.FileFilter;

import com.manichord.mgitt.R;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.tasks.InitLocalTask;

public class ImportRepositoryActivity extends FileExplorerActivity {

    @Override
    protected File getRootFolder() {
        return Environment.getExternalStorageDirectory();
    }

    @Override
    protected FileFilter getExplorerFileFilter() {
        return file -> file.isDirectory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.import_repo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_create_external) {
            File dotGit = new File(getCurrentDir(), Repo.DOT_GIT_DIR);
            if (dotGit.exists()) {
                showToastMessage(R.string.alert_is_already_a_git_repo);
                return true;
            }
            showMessageDialog(R.string.dialog_create_external_title,
                R.string.dialog_create_external_msg,
                R.string.dialog_create_external_positive_label,
                (dialog, which) -> createExternalGitRepo());
            return true;
        } else if (itemId == R.id.action_import_external) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_PATH, getCurrentDir().getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected AdapterView.OnItemClickListener getOnListItemClickListener() {
        return (adapterView, view, position, id) -> {
            File file = mFilesListAdapter.getItem(position);
            if (file.isDirectory()) {
                setCurrentDir(file);
            }
        };
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnListItemLongClickListener() {
        return null;
    }

    void createExternalGitRepo() {
        File current = getCurrentDir();
        String localPath = Repo.EXTERNAL_PREFIX + current;

        Repo repo = Repo.createRepo(localPath, "local repository", getString(R.string.importing));

        InitLocalTask task = new InitLocalTask(repo);
        task.executeTask();
        finish();
    }
}
