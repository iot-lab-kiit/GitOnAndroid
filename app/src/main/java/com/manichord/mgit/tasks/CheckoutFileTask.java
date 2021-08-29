package com.manichord.mgit.tasks;

import com.manichord.mgitt.R;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.utils.exception.StopTaskException;

public class CheckoutFileTask extends RepoOpTask {

    private final AsyncTaskPostCallback mCallback;
    private final String mPath;

    public CheckoutFileTask(Repo repo, String path,
            AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mPath = path;
        setSuccessMsg(R.string.success_checkout_file);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return checkout();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    private boolean checkout() {
        try {
            mRepo.getGit().checkout().addPath(mPath).call();
        } catch (StopTaskException e) {
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }

}
