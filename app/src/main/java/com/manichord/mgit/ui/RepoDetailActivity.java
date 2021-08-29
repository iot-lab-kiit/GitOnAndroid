package com.manichord.mgit.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import me.sheimi.sgit.R;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.manichord.mgit.tasks.RepoOpTask;
import com.manichord.mgit.ui.delegate.RepoOperationDelegate;
import com.manichord.mgit.adapters.RepoOperationsAdapter;
import com.manichord.mgit.models.Repo;
import com.manichord.mgit.ui.fragments.BaseFragment;
import com.manichord.mgit.ui.fragments.CommitsFragment;
import com.manichord.mgit.ui.fragments.FilesFragment;
import com.manichord.mgit.ui.fragments.StatusFragment;

public class RepoDetailActivity extends SheimiFragmentActivity {

    private FilesFragment mFilesFragment;
    private CommitsFragment mCommitsFragment;
    private StatusFragment mStatusFragment;

    private RelativeLayout mRightDrawer;
    private DrawerLayout mDrawerLayout;
    private TabItemPagerAdapter mTabItemPagerAdapter;
    private ViewPager2 mViewPager;
    private Button mCommitNameButton;
    private ImageView mCommitType;
    private MenuItem mSearchItem;

    private Repo mRepo;

    private View mPullProgressContainer;
    private ProgressBar mPullProgressBar;
    private TextView mPullMsg;
    private TextView mPullLeftHint;
    private TextView mPullRightHint;

    private RepoOperationDelegate mRepoDelegate;

    private static final int FILES_FRAGMENT_INDEX = 0;
    private static final int COMMITS_FRAGMENT_INDEX = 1;
    private static final int STATUS_FRAGMENT_INDEX = 2;
    private static final int BRANCH_CHOOSE_ACTIVITY = 0;
    private int mSelectedTab;

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BRANCH_CHOOSE_ACTIVITY) {
            String branchName = mRepo.getBranchName();
            if (branchName == null) {
                showToastMessage(R.string.error_something_wrong);
                return;
            }
            reset(branchName);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepo = (Repo) getIntent().getSerializableExtra(Repo.TAG);
        // aweful hack! workaround for null repo when returning from BranchChooser, but going to
        // shortly refactor passing in serialised repo, so not worth doing more to fix for now
        if (mRepo == null) {
            finish();
            return;
        }
        repoInit();
        setTitle(mRepo.getDiaplayName());
        setContentView(R.layout.activity_repo_detail);
        setupActionBar();
        createFragments();
        setupViewPager();
        setupPullProgressView();
        setupDrawer();
        mCommitNameButton = findViewById(R.id.commitName);
        mCommitType = findViewById(R.id.commitType);
        mCommitNameButton.setOnClickListener(view -> {
    Intent intent = new Intent(RepoDetailActivity.this, BranchChooserActivity.class);
    intent.putExtra(Repo.TAG, mRepo);
    startActivityForResult(intent, BRANCH_CHOOSE_ACTIVITY);
        });
        String branchName = mRepo.getBranchName();
        if (branchName == null) {
            showToastMessage(R.string.error_something_wrong);
            return;
        }
        resetCommitButtonName(branchName);
    }

    public RepoOperationDelegate getRepoDelegate() {
        if (mRepoDelegate == null) {
            mRepoDelegate = new RepoOperationDelegate(mRepo, this);
        }
        return mRepoDelegate;
    }

    private void setupViewPager() {
        mViewPager = findViewById(R.id.pager);
        mTabItemPagerAdapter = new TabItemPagerAdapter(this);
        mViewPager.setAdapter(mTabItemPagerAdapter);
        TabLayout tabLayout= findViewById(R.id.tabs_repo);
        TabLayoutMediator.TabConfigurationStrategy tabsConfig= new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(mTabItemPagerAdapter.getPageTitle(position));
            }
        };
        new TabLayoutMediator(tabLayout, mViewPager,tabsConfig).attach();
    }

    private void setupDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mRightDrawer = findViewById(R.id.right_drawer);
        ListView mRepoOperationList = findViewById(R.id.repoOperationList);
        RepoOperationsAdapter mDrawerAdapter = new RepoOperationsAdapter(this);
        mRepoOperationList.setAdapter(mDrawerAdapter);
        mRepoOperationList.setOnItemClickListener(mDrawerAdapter);
    }

    private void setupPullProgressView() {
        mPullProgressContainer = findViewById(R.id.pullProgressContainer);
        mPullProgressContainer.setVisibility(View.GONE);
        mPullProgressBar = mPullProgressContainer
                .findViewById(R.id.pullProgress);
        mPullMsg = mPullProgressContainer.findViewById(R.id.pullMsg);
        mPullLeftHint = mPullProgressContainer
                .findViewById(R.id.leftHint);
        mPullRightHint = mPullProgressContainer
                .findViewById(R.id.rightHint);
    }

    private void setupActionBar() {
        Toolbar toolbar=findViewById(R.id.repo_toolbar);
//        toolbar.setDisplayShowTitleEnabled(true);
//        toolbar.setDisplayHomeAsUpEnabled(true);
    }

    private void createFragments() {
        mFilesFragment = FilesFragment.newInstance(mRepo);
        mCommitsFragment = CommitsFragment.newInstance(mRepo, null);
        mStatusFragment = StatusFragment.newInstance(mRepo);
    }

    private void resetCommitButtonName(String commitName) {
        int commitType = Repo.getCommitType(commitName);
        switch (commitType) {
            case Repo.COMMIT_TYPE_REMOTE:
                // change the display name to local branch
                commitName = Repo.convertRemoteName(commitName);
            case Repo.COMMIT_TYPE_HEAD:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_branch_w);
                break;
            case Repo.COMMIT_TYPE_TAG:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_tag_w);
                break;
            case Repo.COMMIT_TYPE_TEMP:
                mCommitType.setVisibility(View.GONE);
                break;
        }
        String displayName = Repo.getCommitDisplayName(commitName);
        mCommitNameButton.setText(displayName);
    }

    public void reset(String commitName) {
        resetCommitButtonName(commitName);
        reset();
    }

    public void reset() {
        mFilesFragment.reset();
        mCommitsFragment.reset();
        mStatusFragment.reset();
    }

    public void setFilesFragment(FilesFragment filesFragment) {
        mFilesFragment = filesFragment;
    }

    public FilesFragment getFilesFragment() {
        return mFilesFragment;
    }

    public void setCommitsFragment(CommitsFragment commitsFragment) {
        mCommitsFragment = commitsFragment;
    }

    public void setStatusFragment(StatusFragment statusFragment) {
        mStatusFragment = statusFragment;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.repo_detail, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(mSearchItem, mTabItemPagerAdapter);
        mSearchItem.setVisible(mSelectedTab == COMMITS_FRAGMENT_INDEX);
        SearchView searchView = (SearchView) mSearchItem.getActionView();
        if (searchView != null) {
            searchView.setIconifiedByDefault(true);
            searchView.setOnQueryTextListener(mTabItemPagerAdapter);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_DEL:
                int position = mViewPager.getCurrentItem();
                OnBackClickListener onBackClickListener = mTabItemPagerAdapter
                        .createFragment(position).getOnBackClickListener();
                if (onBackClickListener != null) {
                    if (onBackClickListener.onClick())
                        return true;
                }
                finish();
                return true;
            case KeyEvent.KEYCODE_F:
                mViewPager.setCurrentItem(FILES_FRAGMENT_INDEX);
                return true;
            case KeyEvent.KEYCODE_C:
                mViewPager.setCurrentItem(COMMITS_FRAGMENT_INDEX);
                return true;
            case KeyEvent.KEYCODE_S:
                mViewPager.setCurrentItem(STATUS_FRAGMENT_INDEX);
                return true;
            case KeyEvent.KEYCODE_SLASH:
                if (event.isShiftPressed()) {
                    showKeyboardShortcutsHelpOverlay();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void showKeyboardShortcutsHelpOverlay() {
        showMessageDialog(R.string.dialog_keymap_title, getString(R.string.dialog_keymap_mesg));
    }

    public void error() {
        finish();
        showToastMessage(R.string.error_unknown);
    }

    public class ProgressCallback implements RepoOpTask.AsyncTaskCallback {

        private final int mInitMsg;

        public ProgressCallback(int initMsg) {
            mInitMsg = initMsg;
        }

        @Override
        public void onPreExecute() {
            mPullMsg.setText(mInitMsg);
            Animation anim = AnimationUtils.loadAnimation(
                    RepoDetailActivity.this, R.anim.fade_in);
            mPullProgressContainer.setAnimation(anim);
            mPullProgressContainer.setVisibility(View.VISIBLE);
            mPullLeftHint.setText(R.string.progress_left_init);
            mPullRightHint.setText(R.string.progress_right_init);
        }

        @Override
        public void onProgressUpdate(String... progress) {
            mPullMsg.setText(progress[0]);
            mPullLeftHint.setText(progress[1]);
            mPullRightHint.setText(progress[2]);
            mPullProgressBar.setProgress(Integer.parseInt(progress[3]));
        }

        @Override
        public void onPostExecute(Boolean isSuccess) {
            Animation anim = AnimationUtils.loadAnimation(
                    RepoDetailActivity.this, R.anim.fade_out);
            mPullProgressContainer.setAnimation(anim);
            mPullProgressContainer.setVisibility(View.GONE);
            reset();
        }

        @Override
        public boolean doInBackground(Void... params) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_toggle_drawer) {
            if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                mDrawerLayout.closeDrawer(mRightDrawer);
            } else {
                mDrawerLayout.openDrawer(mRightDrawer);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void closeOperationDrawer() {
        mDrawerLayout.closeDrawer(mRightDrawer);
    }

    public void enterDiffActionMode() {
        mViewPager.setCurrentItem(COMMITS_FRAGMENT_INDEX);
        mCommitsFragment.enterDiffActionMode();
    }

    private void repoInit() {
        mRepo.updateLatestCommitInfo();
        mRepo.getRemotes();
    }

    class TabItemPagerAdapter extends FragmentStateAdapter implements ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {

        private final int[] PAGE_TITLE = { R.string.tab_files_label,
                R.string.tab_commits_label, R.string.tab_status_label };

        public TabItemPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

//        public TabItemPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }

//        @NonNull
//        @Override
//        public BaseFragment getItem(int position) {
//
//        }


        public CharSequence getPageTitle(int position) {
            return getString(PAGE_TITLE[position]);
        }
//
//        @Override
//        public int getCount() {
//        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mSelectedTab = position;
            if (mSearchItem != null) {
                mSearchItem.setVisible(position == COMMITS_FRAGMENT_INDEX);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mViewPager.getCurrentItem() == COMMITS_FRAGMENT_INDEX) {
                mCommitsFragment.setFilter(query);
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            if (mViewPager.getCurrentItem() == COMMITS_FRAGMENT_INDEX) {
                mCommitsFragment.setFilter(query);
            }
            return true;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            if (mViewPager.getCurrentItem() == COMMITS_FRAGMENT_INDEX) {
                mCommitsFragment.setFilter(null);
            }
            return true;
        }

        @NonNull
        @Override
        public BaseFragment createFragment(int position) {
            switch (position) {
                case FILES_FRAGMENT_INDEX:
                    return mFilesFragment;
                case COMMITS_FRAGMENT_INDEX:
                    return mCommitsFragment;
                case STATUS_FRAGMENT_INDEX:
                    mStatusFragment.reset();
                    return mStatusFragment;
            }
            return mFilesFragment;
        }

        @Override
        public int getItemCount() {
            return PAGE_TITLE.length;
        }
    }

}
