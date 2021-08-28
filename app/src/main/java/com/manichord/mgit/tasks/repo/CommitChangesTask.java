package com.manichord.mgit.tasks.repo;

import android.content.Context;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;

import com.manichord.mgit.android.utils.Profile;
import com.manichord.mgit.MGitApplication;
import me.sheimi.sgit.R;
import com.manichord.mgit.database.models.Repo;
import com.manichord.mgit.exception.StopTaskException;

public class CommitChangesTask extends RepoOpTask {

    private final AsyncTaskPostCallback mCallback;
    private final String mCommitMsg;
    private final String mAuthorName;
    private final String mAuthorEmail;
    private final boolean mIsAmend;
    private final boolean mStageAll;

    public CommitChangesTask(Repo repo, String commitMsg, boolean isAmend,
                             boolean stageAll, String authorName, String authorEmail,
                             AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mCommitMsg = commitMsg;
        mIsAmend = isAmend;
        mStageAll = stageAll;
        mAuthorName = authorName;
        mAuthorEmail = authorEmail;
        setSuccessMsg(R.string.success_commit);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return commit();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean commit() {
        try {
            commit(mRepo, mStageAll, mIsAmend, mCommitMsg, mAuthorName, mAuthorEmail);
        } catch (StopTaskException e) {
            return false;
        } catch (GitAPIException e) {
            setException(e);
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }

    public static void commit(Repo repo, boolean stageAll, boolean isAmend,
            String msg, String authorName, String authorEmail) throws Exception {
        Context context = MGitApplication.getContext();
        StoredConfig config = repo.getGit().getRepository().getConfig();
        String committerEmail = config.getString("user", null, "email");
        String committerName = config.getString("user", null, "name");

        if (committerName == null || committerName.equals("")) {
            committerName = Profile.getUsername(context);
        }
        if (committerEmail == null || committerEmail.equals("")) {
            committerEmail = Profile.getEmail(context);
        }
        if (committerName.isEmpty() || committerEmail.isEmpty()) {
            throw new Exception("Please set your name and email");
        }
        if (msg.isEmpty()) {
            throw new Exception("Please include a commit message");
        }
        CommitCommand cc = repo.getGit().commit()
                .setCommitter(committerName, committerEmail).setAll(stageAll)
                .setAmend(isAmend).setMessage(msg);
        if (authorName != null && authorEmail != null) {
            cc.setAuthor(authorName, authorEmail);
        }
        cc.call();
        repo.updateLatestCommitInfo();
    }
}
