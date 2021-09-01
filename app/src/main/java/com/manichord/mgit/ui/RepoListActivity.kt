package com.manichord.mgit.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.manichord.mgit.common.CloneViewModel
import com.manichord.mgit.hideKeyboard
import com.manichord.mgit.utils.transport.MGitHttpConnectionFactory
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.manichord.mgit.MGitApplication
import com.manichord.mgitt.R
import com.manichord.mgit.ui.explorer.ExploreFileActivity
import com.manichord.mgit.ui.explorer.ImportRepositoryActivity
import com.manichord.mgit.adapters.RepoListAdapter
import com.manichord.mgit.database.RepoDbManager
import com.manichord.mgit.models.Repo
import com.manichord.mgitt.databinding.ActivityMainBinding
import com.manichord.mgitt.databinding.CloneViewBinding
import com.manichord.mgit.ui.dialogs.DummyDialogListener
import com.manichord.mgit.ui.dialogs.ImportLocalRepoDialog
import com.manichord.mgit.tasks.CloneTask
import com.manichord.mgit.utils.ssh.PrivateKeyUtils
import timber.log.Timber
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class RepoListActivity : SheimiFragmentActivity() {
    private lateinit var mRepoListAdapter: RepoListAdapter
    private lateinit var cloneViewBinding: CloneViewBinding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestRequiredPermissions()
        enforcePrivacy(this)
        val viewModelProvider = ViewModelProvider(this)

        val cloneViewModel = viewModelProvider.get(CloneViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        PrivateKeyUtils.migratePrivateKeys()
        initUpdatedSSL()
        setSupportActionBar(binding.toolbar)
        mRepoListAdapter = RepoListAdapter(this)
        binding.repoList.adapter = mRepoListAdapter
        mRepoListAdapter.queryAllRepo()
        binding.repoList.onItemClickListener = mRepoListAdapter
        binding.repoList.onItemLongClickListener = mRepoListAdapter
        handleIntent(cloneViewModel)
        binding.addRepoButton.setOnClickListener {
            showCloneView()
        }

        setupCloneDialog(cloneViewModel)
        binding.searchView.setOnQueryTextListener(sd)
    }

    private fun setupCloneDialog(cloneViewModel: CloneViewModel) {
        val cloneDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT))
            .customView(R.layout.clone_view)
            .title(R.string.title_clone_repo)
            .noAutoDismiss()
            .positiveButton(R.string.label_clone) {
                cloneRepo()
            }.negativeButton(R.string.label_cancel) {
                hideCloneView()
            }
        val customView = cloneDialog.getCustomView()
        cloneViewBinding = CloneViewBinding.bind(customView)
        cloneViewBinding.viewModel = cloneViewModel

        cloneViewBinding.viewModel?.visible?.observe(this) { visible ->
            if (visible) cloneDialog.show() else cloneDialog.dismiss()
        }
    }

    private fun handleIntent(
        cloneViewModel: CloneViewModel
    ) {
        val mContext = applicationContext
        val uri = this.intent.data
        if (uri != null) {
            var mRemoteRepoUrl: URL? = null
            try {
                mRemoteRepoUrl = URL(uri.scheme, uri.host, uri.port, uri.path)
            } catch (e: MalformedURLException) {
                Toast.makeText(mContext, R.string.invalid_url, Toast.LENGTH_LONG).show()
                Timber.e(e)
            }
            if (mRemoteRepoUrl != null) {
                val remoteUrl = mRemoteRepoUrl.toString()
                var repoName = remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1)
                val repoUrlBuilder = StringBuilder(remoteUrl)

                //need git extension to clone some repos
                if (!remoteUrl.lowercase(Locale.getDefault())
                        .endsWith(getString(R.string.git_extension))
                ) {
                    repoUrlBuilder.append(getString(R.string.git_extension))
                } else { //if has git extension remove it from repository name
                    repoName = repoName.substring(0, repoName.lastIndexOf('.'))
                }
                //Check if there are others repositories with same remote
                val repositoriesWithSameRemote =
                    Repo.getRepoList(mContext, RepoDbManager.searchRepo(remoteUrl))

                //if so, just open it
                if (repositoriesWithSameRemote.size > 0) {
                    Toast.makeText(
                        mContext,
                        R.string.repository_already_present,
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(mContext, RepoDetailActivity::class.java)
                    intent.putExtra(Repo.TAG, repositoriesWithSameRemote[0])
                    startActivity(intent)
                } else if (Repo.getDir(
                        (applicationContext as MGitApplication).prefenceHelper,
                        repoName
                    ).exists()
                ) {
                    // Repository with name end already exists, see https://github.com/maks/MGit/issues/289
                    cloneViewModel.remoteUrl = repoUrlBuilder.toString()
                    showCloneView()
                } else {
                    val cloningStatus = getString(R.string.cloning)
                    val mRepo = Repo.createRepo(repoName, repoUrlBuilder.toString(), cloningStatus)
                    val task = CloneTask(
                        mRepo,
                        true,
                        cloningStatus,
                        null
                    )
                    task.executeTask()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val searchItem = menu.findItem(R.id.action_search)
        binding.searchView.setMenuItem(searchItem)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        val itemId = item.itemId
        if (itemId == R.id.action_import_repo) {
            intent = Intent(this, ImportRepositoryActivity::class.java)
            startActivityForResult(intent, REQUEST_IMPORT_REPO)
            forwardTransition()
            return true
        } else if (itemId == R.id.action_settings) {
            intent = Intent(this, UserSettingsActivity2::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (requestCode == REQUEST_IMPORT_REPO) {
            val path = data!!.extras!!.getString(
                ExploreFileActivity.RESULT_PATH
            )
            val file = File(path)
            val dotGit = File(file, Repo.DOT_GIT_DIR)
            if (!dotGit.exists()) {
                showToastMessage(getString(R.string.error_no_repository))
                return
            }
            val builder = AlertDialog.Builder(
                this
            )
            builder.setTitle(R.string.dialog_comfirm_import_repo_title)
            builder.setMessage(R.string.dialog_comfirm_import_repo_msg)
            builder.setNegativeButton(
                R.string.label_cancel,
                DummyDialogListener()
            )
            builder.setPositiveButton(
                R.string.label_import
            ) { dialogInterface: DialogInterface?, i: Int ->
                val args = Bundle()
                args.putString(ImportLocalRepoDialog.FROM_PATH, path)
                val rld = ImportLocalRepoDialog()
                rld.arguments = args
                rld.show(supportFragmentManager, "import-local-dialog")
            }
            builder.show()
        }
    }

    val sd: MaterialSearchView.OnQueryTextListener =
        object : MaterialSearchView.OnQueryTextListener,
            MenuItem.OnActionExpandListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                mRepoListAdapter.searchRepo(s)
                return false
            }

            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                mRepoListAdapter.queryAllRepo()
                return true
            }
        }

    override fun finish() {
        rawfinish()
    }

    private fun initUpdatedSSL() {
        MGitHttpConnectionFactory.install()
        Timber.i("Installed custom HTTPS factory")
    }

    private fun cloneRepo() {
        if (cloneViewBinding.viewModel?.validate() == true) {
            hideCloneView()
            cloneViewBinding.viewModel?.cloneRepo()
        }
    }

    private fun showCloneView() {
        cloneViewBinding.viewModel?.show(true)
    }

    private fun hideCloneView() {
        cloneViewBinding.viewModel?.show(false)
        hideKeyboard(this)
    }

    companion object {
        private const val REQUEST_IMPORT_REPO = 0
    }
}
