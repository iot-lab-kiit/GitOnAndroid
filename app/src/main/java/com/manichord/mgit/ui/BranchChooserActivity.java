package com.manichord.mgit.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.api.errors.CannotDeleteCurrentBranchException;
import org.eclipse.jgit.api.errors.GitAPIException;

import me.sheimi.sgit.R;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.ui.dialogs.RenameBranchDialog;
import com.manichord.mgit.utils.exception.StopTaskException;
import com.manichord.mgit.tasks.CheckoutTask;

public class BranchChooserActivity extends SheimiFragmentActivity implements ActionMode.Callback {
    private static final String LOGTAG = BranchChooserActivity.class.getSimpleName();

    private Repo mRepo;
    private ListView mBranchTagList;
    private ProgressBar mLoadding;
    private BranchTagListAdapter mAdapter;
    private boolean mInActionMode;
    String mChosenCommit;

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mInActionMode = false;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_mode_rename_branch) {
            Bundle pathArg = new Bundle();
            pathArg.putString(RenameBranchDialog.FROM_COMMIT,
                mChosenCommit);
            pathArg.putSerializable(Repo.TAG,
                mRepo);
            mode.finish();
            RenameBranchDialog rbd = new RenameBranchDialog();
            rbd.setArguments(pathArg);
            rbd.show(getFragmentManager(), "rename-dialog");

            return true;
        } else if (itemId == R.id.action_mode_delete) {
            tryDelete();
            mode.finish();
            return true;
        }
        return false;
    }


    private void tryDelete() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getString(R.string.dialog_branch_delete) + " " + mChosenCommit)
            .setMessage(R.string.dialog_branch_delete_msg)
            .setPositiveButton(R.string.label_delete, (dialog, which) -> {
                int commitType = Repo.getCommitType(mChosenCommit);
                try {
                    switch (commitType) {
                        case Repo.COMMIT_TYPE_HEAD:
                            mRepo.getGit().branchDelete()
                                .setBranchNames(mChosenCommit)
                                .setForce(true)
                                .call();
                            break;
                        case Repo.COMMIT_TYPE_TAG:
                            mRepo.getGit().tagDelete()
                                .setTags(mChosenCommit)
                                .call();
                            break;
                    }
                } catch (StopTaskException e) {
                    Log.e(LOGTAG, "can't delete " + mChosenCommit, e);
                    runOnUiThread(() -> Toast.makeText(BranchChooserActivity.this, getString(R.string.cannot_delete_branch, mChosenCommit),
                        Toast.LENGTH_LONG).show());
                } catch (CannotDeleteCurrentBranchException e) {
                    Log.e(LOGTAG, "can't delete " + mChosenCommit, e);
                    runOnUiThread(() -> Toast.makeText(BranchChooserActivity.this, getString(R.string.cannot_delete_current_branch, mChosenCommit),
                        Toast.LENGTH_LONG).show());
                } catch (GitAPIException e) {
                    Log.e(LOGTAG, "can't delete " + mChosenCommit, e);
                    runOnUiThread(() -> Toast.makeText(BranchChooserActivity.this, getString(R.string.cannot_delete_branch, mChosenCommit),
                        Toast.LENGTH_LONG).show());
                }
                refreshList();
            })
            .setNegativeButton(R.string.label_cancel, null);
        alert.show();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.action_mode_branch, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public void refreshList() {
        mAdapter = new BranchTagListAdapter(this);
        mBranchTagList.setAdapter(mAdapter);
        String[] branches = mRepo.getBranches();
        String[] tags = mRepo.getTags();
        mAdapter.addAll(branches);
        mAdapter.addAll(tags);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = getLayoutInflater().inflate(R.layout.fragment_branches, null);
        mRepo = (Repo) getIntent().getSerializableExtra(Repo.TAG);
        mBranchTagList = v.findViewById(R.id.branches);
        mLoadding = v.findViewById(R.id.loading);
        mAdapter = new BranchTagListAdapter(this);
        mBranchTagList.setAdapter(mAdapter);
        mBranchTagList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setTitle(R.string.dialog_choose_branch_title);

        refreshList();

        mBranchTagList
                .setOnItemClickListener((adapterView, view, position, id) -> {
                    String commitName = mAdapter.getItem(position);
                    CheckoutTask checkoutTask = new CheckoutTask(mRepo, commitName, null,
                        isSuccess -> finish());
                    mLoadding.setVisibility(View.VISIBLE);
                    mBranchTagList.setVisibility(View.GONE);
                    checkoutTask.executeTask();
                });

        mBranchTagList
                .setOnItemLongClickListener((adapterView, view, position, id) -> {

                    if (mInActionMode) {
                        return true;
                    }

                    mInActionMode = true;
                    mChosenCommit = mAdapter.getItem(position);
                    BranchChooserActivity.this.startActionMode(BranchChooserActivity.this);
                    view.setSelected(true);
                    mAdapter.notifyDataSetChanged();
                    return true;
                });

        setContentView(v);
    }

    private static class BranchTagListAdapter extends ArrayAdapter<String> {

        public BranchTagListAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            ListItemHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(
                        R.layout.listitem_dialog_choose_commit, parent, false);
                holder = new ListItemHolder();
                holder.commitTitle = convertView
                        .findViewById(R.id.commitTitle);
                holder.commitIcon = convertView
                        .findViewById(R.id.commitIcon);
                convertView.setTag(holder);
            } else {
                holder = (ListItemHolder) convertView.getTag();
            }
            String commitName = getItem(position);
            String displayName = Repo.getCommitDisplayName(commitName);
            int commitType = Repo.getCommitType(commitName);
            switch (commitType) {
                case Repo.COMMIT_TYPE_HEAD:
                    holder.commitIcon.setImageResource(R.drawable.ic_branch_d);
                    break;
                case Repo.COMMIT_TYPE_TAG:
                    holder.commitIcon.setImageResource(R.drawable.ic_tag_d);
                    break;
            }
            holder.commitTitle.setText(displayName);

            // set if selected
            if (convertView.isSelected()) {
                convertView.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.pressed_sgit));
            } else {
                convertView.setBackgroundColor(convertView.getContext().getResources().getColor(android.R.color.transparent));
            }
            return convertView;
        }

    }

    private static class ListItemHolder {
        public TextView commitTitle;
        public ImageView commitIcon;
    }
}
