package com.manichord.mgit.ui.fragments;

import com.manichord.mgit.ui.RepoDetailActivity;

public abstract class RepoDetailFragment extends BaseFragment {

    public RepoDetailActivity getRawActivity() {
        return (RepoDetailActivity) super.getRawActivity();
    }
    
}
