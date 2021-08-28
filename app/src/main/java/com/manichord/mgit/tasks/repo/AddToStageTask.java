package com.manichord.mgit.tasks.repo;

import me.sheimi.sgit.R;
import com.manichord.mgit.database.models.Repo;
import com.manichord.mgit.exception.StopTaskException;

public class AddToStageTask extends RepoOpTask {

    public final String mFilePattern;

    public AddToStageTask(Repo repo, String filepattern) {
        super(repo);
        mFilePattern = filepattern;
        setSuccessMsg(R.string.success_add_to_stage);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return addToStage();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
    }

    public boolean addToStage() {
        try {
            mRepo.getGit().add().addFilepattern(mFilePattern).call();
        } catch (StopTaskException e) {
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }
}
