/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.moduth.blockcanary.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.github.moduth.blockcanary.LogWriter;
import com.github.moduth.blockcanary.R;
import com.github.moduth.blockcanary.log.Block;
import com.github.moduth.blockcanary.log.BlockCanaryInternals;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Page show blocks
 *
 * @author markzhai on 15/9/26.
 */
public class DisplayBlockActivity extends Activity {

    private static final String TAG = "DisplayBlockActivity";
    private static final String SHOW_BLOCK_EXTRA = "show_latest";
    public static final String SHOW_BLOCK_EXTRA_KEY = "BlockStartTime";
    // null until it's been first loaded.
    private List<Block> mBlockEntries = new ArrayList<>();
    private String mBlockStartTime;

    private ListView mListView;
    private TextView mFailureView;
    private Button mActionButton;
    private int mMaxStoredBlockCount;

    public static PendingIntent createPendingIntent(Context context) {
        return createPendingIntent(context, null);
    }

    public static PendingIntent createPendingIntent(Context context, String blockStartTime) {
        Intent intent = new Intent(context, DisplayBlockActivity.class);
        intent.putExtra(SHOW_BLOCK_EXTRA, blockStartTime);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 1, intent, FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mBlockStartTime = savedInstanceState.getString(SHOW_BLOCK_EXTRA_KEY);
        } else {
            Intent intent = getIntent();
            if (intent.hasExtra(SHOW_BLOCK_EXTRA)) {
                mBlockStartTime = intent.getStringExtra(SHOW_BLOCK_EXTRA);
            }
        }

        //noinspection unchecked
//        mBlockEntries = (List<Block>) getLastNonConfigurationInstance();

        setContentView(R.layout.block_canary_display_leak);

        mListView = (ListView) findViewById(R.id.__leak_canary_display_leak_list);
        mFailureView = (TextView) findViewById(R.id.__leak_canary_display_leak_failure);
        mActionButton = (Button) findViewById(R.id.__leak_canary_action);

        mMaxStoredBlockCount = getResources().getInteger(R.integer.block_canary_max_stored_count);

        updateUi();
    }

    // No, it's not deprecated. Android lies.
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mBlockEntries;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SHOW_BLOCK_EXTRA_KEY, mBlockStartTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadBlocks.load(this);
    }

    @Override
    public void setTheme(int resid) {
        // We don't want this to be called with an incompatible theme.
        // This could happen if you implement runtime switching of themes
        // using ActivityLifecycleCallbacks.
        if (resid != R.style.block_canary_BlockCanary_Base) {
            return;
        }
        super.setTheme(resid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadBlocks.forgetActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final Block block = getBlock(mBlockStartTime);
        if (block != null) {
            menu.add(R.string.block_canary_share_leak)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            shareBlock(block);
                            return true;
                        }
                    });
            menu.add(R.string.block_canary_share_stack_dump)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            shareHeapDump(block);
                            return true;
                        }
                    });
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mBlockStartTime = null;
            updateUi();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mBlockStartTime != null) {
            mBlockStartTime = null;
            updateUi();
        } else {
            super.onBackPressed();
        }
    }

    private void shareBlock(Block block) {
        String leakInfo = block.toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, leakInfo);
        startActivity(Intent.createChooser(intent, getString(R.string.block_canary_share_with)));
    }

    private void shareHeapDump(Block block) {
        File heapDumpFile = block.logFile;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            heapDumpFile.setReadable(true, false);
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(heapDumpFile));
        startActivity(Intent.createChooser(intent, getString(R.string.block_canary_share_with)));
    }

    private void updateUi() {
        final Block block = getBlock(mBlockStartTime);
        if (block == null) {
            mBlockStartTime = null;
        }

        // Reset to defaults
        mListView.setVisibility(VISIBLE);
        mFailureView.setVisibility(GONE);

        if (block != null) {
            renderBlockDetail(block);
        } else {
            renderBlockList();
        }
    }

    private void renderBlockList() {
        ListAdapter listAdapter = mListView.getAdapter();
        if (listAdapter instanceof BlockListAdapter) {
            ((BlockListAdapter) listAdapter).notifyDataSetChanged();
        } else {
            BlockListAdapter adapter = new BlockListAdapter();
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mBlockStartTime = mBlockEntries.get(position).timeStart;
                    updateUi();
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                invalidateOptionsMenu();
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            }
            setTitle(getString(R.string.block_canary_block_list_title, getPackageName()));
            mActionButton.setText(R.string.block_canary_delete_all);
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogWriter.deleteLogFiles();
                    mBlockEntries = Collections.emptyList();
                    updateUi();
                }
            });
        }
        mActionButton.setVisibility(mBlockEntries.isEmpty() ? GONE : VISIBLE);
    }

    private void renderBlockDetail(final Block block) {
        ListAdapter listAdapter = mListView.getAdapter();
        final BlockDetailAdapter adapter;
        if (listAdapter instanceof BlockDetailAdapter) {
            adapter = (BlockDetailAdapter) listAdapter;
        } else {
            adapter = new BlockDetailAdapter();
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.toggleRow(position);
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                invalidateOptionsMenu();
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
            mActionButton.setVisibility(VISIBLE);
            mActionButton.setText(R.string.block_canary_delete);
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (block != null) {
                        block.logFile.delete();
                        mBlockStartTime = null;
                        mBlockEntries.remove(block);
                        updateUi();
                    }
                }
            });
        }
        adapter.update(block);
        setTitle(getString(R.string.block_canary_class_has_blocked, block.timeCost));
    }

    private Block getBlock(String startTime) {
        if (mBlockEntries == null || TextUtils.isEmpty(startTime)) {
            return null;
        }
        for (Block block : mBlockEntries) {
            if (block.timeStart.equals(startTime)) {
                return block;
            }
        }
        return null;
    }

    class BlockListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mBlockEntries.size();
        }

        @Override
        public Block getItem(int position) {
            return mBlockEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(DisplayBlockActivity.this)
                        .inflate(R.layout.block_canary_block_row, parent, false);
            }
            TextView titleView = (TextView) convertView.findViewById(R.id.__leak_canary_row_text);
            TextView timeView = (TextView) convertView.findViewById(R.id.__leak_canary_row_time);
            Block block = getItem(position);

            String index;
            if (position == 0 && mBlockEntries.size() == mMaxStoredBlockCount) {
                index = "MAX. ";
            } else {
                index = (mBlockEntries.size() - position) + ". ";
            }

            String title = index + block.getKeyStackString() + " " +
                    getString(R.string.block_canary_class_has_blocked, block.timeCost);
            titleView.setText(title);
            String time = DateUtils.formatDateTime(DisplayBlockActivity.this,
                    block.logFile.lastModified(), FORMAT_SHOW_TIME | FORMAT_SHOW_DATE);
            timeView.setText(time);
            return convertView;
        }
    }

    static class LoadBlocks implements Runnable {

        static final List<LoadBlocks> inFlight = new ArrayList<>();
        static final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
        private DisplayBlockActivity activityOrNull;
        private final Handler mainHandler;

        LoadBlocks(DisplayBlockActivity activity) {
            this.activityOrNull = activity;
            mainHandler = new Handler(Looper.getMainLooper());
        }

        static void load(DisplayBlockActivity activity) {
            LoadBlocks loadBlocks = new LoadBlocks(activity);
            inFlight.add(loadBlocks);
            backgroundExecutor.execute(loadBlocks);
        }

        static void forgetActivity() {
            for (LoadBlocks loadBlocks : inFlight) {
                loadBlocks.activityOrNull = null;
            }
            inFlight.clear();
        }

        @Override
        public void run() {
            final List<Block> blocks = new ArrayList<Block>();
            File[] files = BlockCanaryInternals.getLogFiles();
            if (files != null) {
                for (File blockFile : files) {
                    try {
                        blocks.add(Block.newInstance(blockFile));
                    } catch (Exception e) {
                        // Likely a format change in the blockFile
                        blockFile.delete();
                        Log.e(TAG, "Could not read block log file, deleted :" + blockFile, e);
                    }
                }
                Collections.sort(blocks, new Comparator<Block>() {
                    @Override
                    public int compare(Block lhs, Block rhs) {
                        return Long.valueOf(rhs.logFile.lastModified())
                                .compareTo(lhs.logFile.lastModified());
                    }
                });
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    inFlight.remove(LoadBlocks.this);
                    if (activityOrNull != null) {
                        activityOrNull.mBlockEntries = blocks;
                        //Log.d("BlockCanary", "load block entries: " + blocks.size());
                        activityOrNull.updateUi();
                    }
                }
            });
        }
    }

    static String classSimpleName(String className) {
        int separator = className.lastIndexOf('.');
        if (separator == -1) {
            return className;
        } else {
            return className.substring(separator + 1);
        }
    }
}