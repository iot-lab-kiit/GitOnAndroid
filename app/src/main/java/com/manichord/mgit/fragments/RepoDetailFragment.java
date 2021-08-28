package com.manichord.mgit.fragments;

import com.manichord.mgit.activities.RepoDetailActivity;

public abstract class RepoDetailFragment extends BaseFragment {

    public RepoDetailActivity getRawActivity() {
        return (RepoDetailActivity) super.getRawActivity();
    }
    
}
