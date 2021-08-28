package com.manichord.mgit.ui.explorer;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

import com.manichord.mgit.ui.SheimiFragmentActivity;
import com.manichord.mgit.utils.Profile;
import me.sheimi.sgit.R;
import com.manichord.mgit.adapters.FilesListAdapter;

public abstract class FileExplorerActivity extends SheimiFragmentActivity {

    public static final String RESULT_PATH = "result_path";

    private File mRootFolder;
    private File mCurrentDir;
    protected FilesListAdapter mFilesListAdapter;
    private TextView mCurrentPathView;
    private TextView mUpDir;
    private ImageView mUpDirIcon;

    protected abstract File getRootFolder();

    protected abstract FileFilter getExplorerFileFilter();

    protected abstract AdapterView.OnItemClickListener getOnListItemClickListener();

    protected abstract AdapterView.OnItemLongClickListener getOnListItemLongClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mRootFolder = getRootFolder();
        mCurrentDir = mRootFolder;

        ListView mFileList = findViewById(R.id.fileList);
        mCurrentPathView = findViewById(R.id.currentPath);
        mCurrentPathView.setText(mCurrentDir.getPath());

        mUpDirIcon = findViewById(R.id.upDirIcon);
        mUpDirIcon.setImageResource(Profile.getStyledResource(this, R.attr.ic_folder_fl));

        mUpDir = findViewById(R.id.upDir);
        mUpDir.setOnClickListener(v -> {
            File parent = mCurrentDir.getParentFile();
            if (parent != null) {
                setCurrentDir(parent);
            }
        });

        mFilesListAdapter = new FilesListAdapter(this, getExplorerFileFilter());
        mFileList.setAdapter(mFilesListAdapter);
        mFilesListAdapter.setDir(mRootFolder);

        mFileList.setOnItemClickListener(getOnListItemClickListener());
        mFileList.setOnItemLongClickListener(getOnListItemLongClickListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final File parent = mCurrentDir.getParentFile();
            if (!mRootFolder.equals(mCurrentDir) && (parent != null)) {
                setCurrentDir(parent);
                return true;
            }
            finish();
            return true;
        }
        return false;
    }

    protected void setCurrentDir(File dir) {
        mCurrentDir = dir;
        mFilesListAdapter.setDir(mCurrentDir);
        mCurrentPathView.setText(mCurrentDir.getPath());

        if (dir.getParentFile() == null) {
            mUpDir.setVisibility(View.GONE);
            mUpDirIcon.setVisibility(View.GONE);
        } else {
            mUpDir.setVisibility(View.VISIBLE);
            mUpDirIcon.setVisibility(View.VISIBLE);
        }

    }
    
    protected File getCurrentDir() {
        return mCurrentDir;
    }

}
