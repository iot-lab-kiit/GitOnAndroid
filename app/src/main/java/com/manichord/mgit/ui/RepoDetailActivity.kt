package com.manichord.mgit.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.manichord.mgit.adapters.RepoOperationsAdapter
import com.manichord.mgit.models.Repo
import com.manichord.mgit.tasks.RepoOpTask.AsyncTaskCallback
import com.manichord.mgit.ui.delegate.RepoOperationDelegate
import com.manichord.mgit.ui.fragments.BaseFragment
import com.manichord.mgit.ui.fragments.CommitsFragment
import com.manichord.mgit.ui.fragments.FilesFragment
import com.manichord.mgit.ui.fragments.StatusFragment
import me.sheimi.sgit.R

class RepoDetailActivity : SheimiFragmentActivity() {
      lateinit var filesFragment: FilesFragment
    private  lateinit var mCommitsFragment: CommitsFragment
    private  lateinit var mStatusFragment: StatusFragment
    private  lateinit var mRightDrawer: RelativeLayout
    private  lateinit var mDrawerLayout: DrawerLayout
    private  lateinit var mTabItemPagerAdapter: TabItemPagerAdapter
    private  lateinit var mViewPager: ViewPager2
    private  lateinit var mCommitNameButton: Button
    private  lateinit var mCommitType: ImageView
    private  var mSearchItem: MenuItem? =null
    private  var mRepo: Repo?= null
    private  lateinit var mPullProgressContainer: View
    private  lateinit var mPullProgressBar: ProgressBar
    private  lateinit var mPullMsg: TextView
    private  lateinit var mPullLeftHint: TextView
    private  lateinit var mPullRightHint: TextView
    private  lateinit var mRepoDelegate: RepoOperationDelegate
    private  var mSelectedTab = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BRANCH_CHOOSE_ACTIVITY) {
            val branchName = mRepo!!.branchName
            if (branchName == null) {
                showToastMessage(R.string.error_something_wrong)
                return
            }
            reset(branchName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRepo = intent.getSerializableExtra(Repo.TAG) as Repo
        // aweful hack! workaround for null repo when returning from BranchChooser, but going to
        // shortly refactor passing in serialised repo, so not worth doing more to fix for now
        if (mRepo == null) {
            finish()
            return
        }
        repoInit()
        title = mRepo?.diaplayName
        setContentView(R.layout.activity_repo_detail)
        setupActionBar()
        createFragments()
        setupViewPager()
        setupPullProgressView()
        setupDrawer()
        mCommitNameButton = findViewById(R.id.commitName)
        mCommitType = findViewById(R.id.commitType)
        mCommitNameButton.setOnClickListener { view: View? ->
            val intent = Intent(this@RepoDetailActivity, BranchChooserActivity::class.java)
            intent.putExtra(Repo.TAG, mRepo)
            startActivityForResult(intent, BRANCH_CHOOSE_ACTIVITY)
        }
        val branchName = mRepo?.branchName
        if (branchName == null) {
            showToastMessage(R.string.error_something_wrong)
            return
        }
        resetCommitButtonName(branchName)
    }

    val repoDelegate: RepoOperationDelegate
        get() {
            return mRepoDelegate
        }

    private fun setupViewPager() {
        mViewPager = findViewById(R.id.pager)
        mTabItemPagerAdapter = TabItemPagerAdapter(this)
        mViewPager.setAdapter(mTabItemPagerAdapter)
        val tabLayout = findViewById<TabLayout>(R.id.tabs_repo)
        val tabsConfig = TabConfigurationStrategy { tab, position ->
            tab.text = mTabItemPagerAdapter.getPageTitle(position)
        }
        TabLayoutMediator(tabLayout, mViewPager, tabsConfig).attach()
    }

    private fun setupDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mRightDrawer = findViewById(R.id.right_drawer)
        val mRepoOperationList = findViewById<ListView>(R.id.repoOperationList)
        val mDrawerAdapter = RepoOperationsAdapter(this)
        mRepoOperationList.adapter = mDrawerAdapter
        mRepoOperationList.onItemClickListener = mDrawerAdapter
    }

    private fun setupPullProgressView() {
        mPullProgressContainer = findViewById(R.id.pullProgressContainer)
        mPullProgressContainer.setVisibility(View.GONE)
        mPullProgressBar = mPullProgressContainer
            .findViewById(R.id.pullProgress)
        mPullMsg = mPullProgressContainer.findViewById(R.id.pullMsg)
        mPullLeftHint = mPullProgressContainer
            .findViewById(R.id.leftHint)
        mPullRightHint = mPullProgressContainer
            .findViewById(R.id.rightHint)
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.repo_toolbar)
        //        toolbar.setDisplayShowTitleEnabled(true);
//        toolbar.setDisplayHomeAsUpEnabled(true);
    }

    private fun createFragments() {
        filesFragment = FilesFragment.newInstance(mRepo)
        mCommitsFragment = CommitsFragment.newInstance(mRepo, null)
        mStatusFragment = StatusFragment.newInstance(mRepo)
    }

    private fun resetCommitButtonName(name: String) {
        var commitName=name
        when (Repo.getCommitType(commitName)) {
            Repo.COMMIT_TYPE_REMOTE -> {
                // change the display name to local branch
                commitName = Repo.convertRemoteName(commitName)
                mCommitType.visibility = View.VISIBLE
                mCommitType.setImageResource(R.drawable.ic_branch_w)
            }
            Repo.COMMIT_TYPE_HEAD -> {
                mCommitType.visibility = View.VISIBLE
                mCommitType.setImageResource(R.drawable.ic_branch_w)
            }
            Repo.COMMIT_TYPE_TAG -> {
                mCommitType.visibility = View.VISIBLE
                mCommitType.setImageResource(R.drawable.ic_tag_w)
            }
            Repo.COMMIT_TYPE_TEMP -> mCommitType.visibility = View.GONE
        }
        val displayName = Repo.getCommitDisplayName(commitName)
        mCommitNameButton.text = displayName
    }

    fun reset(commitName: String) {
        resetCommitButtonName(commitName)
        reset()
    }

    fun reset() {
        filesFragment.reset()
        mCommitsFragment.reset()
        mStatusFragment.reset()
    }

    fun setCommitsFragment(commitsFragment: CommitsFragment) {
        mCommitsFragment = commitsFragment
    }

    fun setStatusFragment(statusFragment: StatusFragment) {
        mStatusFragment = statusFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.repo_detail, menu)
        mSearchItem = menu.findItem(R.id.action_search)
        mSearchItem?.setOnActionExpandListener(mTabItemPagerAdapter)

        mSearchItem?.isVisible = mSelectedTab == COMMITS_FRAGMENT_INDEX
        val searchView = mSearchItem?.actionView as SearchView?
        if (searchView != null) {
            searchView.isIconifiedByDefault = true
            searchView.setOnQueryTextListener(mTabItemPagerAdapter)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_DEL -> {
                val position = mViewPager.currentItem
                val onBackClickListener = mTabItemPagerAdapter
                    .createFragment(position).onBackClickListener
                if (onBackClickListener != null) {
                    if (onBackClickListener.onClick()) return true
                }
                finish()
                true
            }
            KeyEvent.KEYCODE_F -> {
                mViewPager.currentItem = FILES_FRAGMENT_INDEX
                true
            }
            KeyEvent.KEYCODE_C -> {
                mViewPager.currentItem = COMMITS_FRAGMENT_INDEX
                true
            }
            KeyEvent.KEYCODE_S -> {
                mViewPager.currentItem = STATUS_FRAGMENT_INDEX
                true
            }
            KeyEvent.KEYCODE_SLASH -> {
                if (event.isShiftPressed) {
                    showKeyboardShortcutsHelpOverlay()
                }
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }

    private fun showKeyboardShortcutsHelpOverlay() {
        showMessageDialog(R.string.dialog_keymap_title, getString(R.string.dialog_keymap_mesg))
    }

    fun error() {
        finish()
        showToastMessage(R.string.error_unknown)
    }

    inner class ProgressCallback(private val mInitMsg: Int) : AsyncTaskCallback {
        override fun onPreExecute() {
            mPullMsg.setText(mInitMsg)
            val anim = AnimationUtils.loadAnimation(
                this@RepoDetailActivity, R.anim.fade_in
            )
            mPullProgressContainer.animation = anim
            mPullProgressContainer.visibility = View.VISIBLE
            mPullLeftHint.setText(R.string.progress_left_init)
            mPullRightHint.setText(R.string.progress_right_init)
        }

        override fun onProgressUpdate(vararg progress: String) {
            mPullMsg.text = progress[0]
            mPullLeftHint.text = progress[1]
            mPullRightHint.text = progress[2]
            mPullProgressBar.progress = progress[3].toInt()
        }

        override fun onPostExecute(isSuccess: Boolean) {
            val anim = AnimationUtils.loadAnimation(
                this@RepoDetailActivity, R.anim.fade_out
            )
            mPullProgressContainer.animation = anim
            mPullProgressContainer.visibility = View.GONE
            reset()
        }

        override fun doInBackground(vararg params: Void): Boolean {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                return false
            }
            return true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            finish()
            return true
        } else if (itemId == R.id.action_toggle_drawer) {
            if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                mDrawerLayout.closeDrawer(mRightDrawer)
            } else {
                mDrawerLayout.openDrawer(mRightDrawer)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun closeOperationDrawer() {
        mDrawerLayout.closeDrawer(mRightDrawer)
    }

    fun enterDiffActionMode() {
        mViewPager.currentItem = COMMITS_FRAGMENT_INDEX
        mCommitsFragment.enterDiffActionMode()
    }

    private fun repoInit() {
        mRepo?.updateLatestCommitInfo()
        mRepo?.remotes
    }

    internal inner class TabItemPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity), OnPageChangeListener,
        SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
        private val PAGE_TITLE = intArrayOf(
            R.string.tab_files_label,
            R.string.tab_commits_label, R.string.tab_status_label
        )

        fun getPageTitle(position: Int): CharSequence {
            return getString(PAGE_TITLE[position])
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            mSelectedTab = position
            if (mSearchItem != null) {
                mSearchItem?.isVisible = position == COMMITS_FRAGMENT_INDEX
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
        override fun onQueryTextSubmit(query: String): Boolean {
            if (mViewPager.currentItem == COMMITS_FRAGMENT_INDEX) {
                mCommitsFragment.setFilter(query)
            }
            return true
        }

        override fun onQueryTextChange(query: String): Boolean {
            if (mViewPager.currentItem == COMMITS_FRAGMENT_INDEX) {
                mCommitsFragment.setFilter(query)
            }
            return true
        }

        override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
            return true
        }

        override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
            if (mViewPager.currentItem == COMMITS_FRAGMENT_INDEX) {
                mCommitsFragment.setFilter(null)
            }
            return true
        }

        override fun createFragment(position: Int): BaseFragment {
            when (position) {
                FILES_FRAGMENT_INDEX -> return filesFragment
                COMMITS_FRAGMENT_INDEX -> return mCommitsFragment
                STATUS_FRAGMENT_INDEX -> {
                    mStatusFragment.reset()
                    return mStatusFragment
                }
            }
            return filesFragment
        }

        override fun getItemCount(): Int {
            return PAGE_TITLE.size
        }
    }

    companion object {
        private const val FILES_FRAGMENT_INDEX = 0
        private const val COMMITS_FRAGMENT_INDEX = 1
        private const val STATUS_FRAGMENT_INDEX = 2
        private const val BRANCH_CHOOSE_ACTIVITY = 0
    }
}
