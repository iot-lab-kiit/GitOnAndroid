package me.sheimi.sgit.repo.tasks;

import android.os.AsyncTask;
import androidx.annotation.StringRes;

import me.sheimi.sgit.R;
import timber.log.Timber;

public abstract class SheimiAsyncTask<A, B, C> extends AsyncTask<A, B, C> {

    protected Throwable mException;
    protected int mErrorRes = 0;

    protected void setException(Throwable e) {
        Timber.e(e, "set exception");
        mException = e;
    }

    protected void setException(Throwable e, int errorRes) {
        Timber.e(e, "set error [%d] exception", errorRes);
        mException = e;
        mErrorRes = errorRes;
    }

    protected void setError(int errorRes) {
        Timber.e("set error res id: %d", errorRes);
        mErrorRes = errorRes;
    }

    private boolean mIsCanceled = false;

    public void cancelTask() {
        mIsCanceled = true;
    }

    /**
     * This method is to be overridden and should return the resource that
     * is used as the title as the
     * {@link com.manichord.mgit.dialogs.ErrorDialog} title when the
     * task fails with an exception.
     */
    @StringRes
    public int getErrorTitleRes() {
        return R.string.dialog_error_title;
    }

    public boolean isTaskCanceled() {
        return mIsCanceled;
    }

    public interface AsyncTaskPostCallback {
        void onPostExecute(Boolean isSuccess);
    }

    public interface AsyncTaskCallback {
        boolean doInBackground(Void... params);

        void onPreExecute();

        void onProgressUpdate(String... progress);

        void onPostExecute(Boolean isSuccess);
    }
}
