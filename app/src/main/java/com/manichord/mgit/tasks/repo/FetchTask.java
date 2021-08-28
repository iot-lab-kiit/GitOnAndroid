package com.manichord.mgit.tasks.repo;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;

import me.sheimi.sgit.R;
import com.manichord.mgit.database.models.Repo;
import com.manichord.mgit.exception.StopTaskException;
import com.manichord.mgit.ssh.SgitTransportCallback;

public class FetchTask extends RepoRemoteOpTask {

    private final AsyncTaskCallback mCallback;
    private final String[] mRemotes;

    public FetchTask(String[] remotes, Repo repo, AsyncTaskCallback callback) {
        super(repo);
        mCallback = callback;
        mRemotes = remotes;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = true;
        for (final String remote : mRemotes) {
            result = fetchRepo(remote) & result;
            if (mCallback != null) {
                result = mCallback.doInBackground(params) & result;
            }
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        if (mCallback != null) {
            mCallback.onProgressUpdate(progress);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onPreExecute();
        }
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    private boolean fetchRepo(String remote) {
        Git git;
        try {
            git = mRepo.getGit();
        } catch (StopTaskException e) {
            return false;
        }

        final FetchCommand fetchCommand = git.fetch()
                .setProgressMonitor(new BasicProgressMonitor())
                .setTransportConfigCallback(new SgitTransportCallback())
                .setRemote(remote);

        setCredentials(fetchCommand);

        try {
            fetchCommand.call();
        } catch (TransportException e) {
            setException(e);
            handleAuthError(this);
            return false;
        } catch (Exception e) {
            setException(e, R.string.error_pull_failed);
            return false;
        } catch (OutOfMemoryError e) {
            setException(e, R.string.error_out_of_memory);
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }

    @Override
    public RepoRemoteOpTask getNewTask() {
        return new FetchTask(mRemotes, mRepo, mCallback);
    }
}
