package com.manichord.mgit.ui.fragments;

import com.manichord.mgit.ui.SheimiFragmentActivity.OnBackClickListener;
import me.sheimi.sgit.R;
import com.manichord.mgit.ui.CommitDiffActivity;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.tasks.StatusTask;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

/**
 * Created by sheimi on 8/5/13.
 */
public class StatusFragment extends RepoDetailFragment {

    private Repo mRepo;
    private ProgressBar mLoadding;
    private TextView mStatus;

    public static StatusFragment newInstance(Repo mRepo) {
        StatusFragment fragment = new StatusFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Repo.TAG, mRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_status, container, false);
        getRawActivity().setStatusFragment(this);

        Bundle bundle = getArguments();
        mRepo = (Repo) Objects.requireNonNull(bundle).getSerializable(Repo.TAG);
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
        }
        if (mRepo == null) {
            return v;
        }
        mLoadding = v.findViewById(R.id.loading);
        mStatus = v.findViewById(R.id.status);
        Button mStagedDiff = v.findViewById(R.id.button_staged_diff);
        Button mUnstagedDiff = v.findViewById(R.id.button_unstaged_diff);
        mStagedDiff.setOnClickListener(v12 -> showDiff("HEAD", "dircache"));
        mUnstagedDiff.setOnClickListener(v1 -> showDiff("dircache", "filetree"));
        reset();
        return v;
    }

    private void showDiff(String oldCommit, String newCommit) {
        Intent intent = new Intent(getRawActivity(),
                CommitDiffActivity.class);
        intent.putExtra(CommitDiffActivity.OLD_COMMIT, oldCommit);
        intent.putExtra(CommitDiffActivity.NEW_COMMIT, newCommit);
        intent.putExtra(CommitDiffActivity.SHOW_DESCRIPTION, false);
        intent.putExtra(Repo.TAG, mRepo);
        getRawActivity().startActivity(intent);
    }

    @Override
    public void reset() {
        if (mLoadding == null || mStatus == null)
            return;
        mLoadding.setVisibility(View.VISIBLE);
        mStatus.setVisibility(View.GONE);
        StatusTask task = new StatusTask(mRepo, result -> {
            mStatus.setText(result);
            mLoadding.setVisibility(View.GONE);
            mStatus.setVisibility(View.VISIBLE);
        });
        task.executeTask();
    }

    @Override
    public OnBackClickListener getOnBackClickListener() {
        return null;
    }
}
