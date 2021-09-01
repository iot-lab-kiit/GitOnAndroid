package com.manichord.mgit.tasks;

import java.io.File;

import com.manichord.mgit.utils.FsUtils;
import com.manichord.mgitt.R;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.utils.exception.StopTaskException;

public class DeleteFileFromRepoTask extends RepoOpTask {

    public final String mFilePattern;
    public final AsyncTaskPostCallback mCallback;
    private final DeleteOperationType mOperationType;

    public DeleteFileFromRepoTask(Repo repo, String filepattern,
            DeleteOperationType deleteOperationType,AsyncTaskPostCallback callback) {
        super(repo);
        mFilePattern = filepattern;
        mCallback = callback;
        mOperationType = deleteOperationType;
        setSuccessMsg(R.string.success_remove_file);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return removeFile();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean removeFile() {
        try {
            switch (mOperationType) {
                case DELETE:
                    File fileToDelete = FsUtils.joinPath(mRepo.getDir(), mFilePattern);
                    FsUtils.deleteFile(fileToDelete);
                    break;
                case REMOVE_CACHED:
                    mRepo.getGit().rm().setCached(true).addFilepattern(mFilePattern).call();
                    break;
                case REMOVE_FORCE:
                    mRepo.getGit().rm().addFilepattern(mFilePattern).call();
                    break;
            }
        } catch (StopTaskException e) {
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }

    /**
     * Created by lee on 2015-01-30.
     */
    public enum DeleteOperationType {
        DELETE,REMOVE_CACHED,REMOVE_FORCE
    }
}
