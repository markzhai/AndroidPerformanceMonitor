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

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.moduth.blockcanary.R;
import com.github.moduth.blockcanary.internal.BlockInfo;

import java.util.Arrays;

final class DetailAdapter extends BaseAdapter {

    private static final int TOP_ROW = 0;
    private static final int NORMAL_ROW = 1;

    private boolean[] mFoldings = new boolean[0];

    private BlockInfo mBlockInfo;

    private static final int POSITION_BASIC = 1;
    private static final int POSITION_TIME = 2;
    private static final int POSITION_CPU = 3;
    private static final int POSITION_THREAD_STACK = 4;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        if (getItemViewType(position) == TOP_ROW) {
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(context).inflate(R.layout.block_canary_ref_top_row, parent, false);
            }
            TextView textView = findById(convertView, R.id.__leak_canary_row_text);
            textView.setText(context.getPackageName());
        } else {
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(context).inflate(R.layout.block_canary_ref_row, parent, false);
            }
            TextView textView = findById(convertView, R.id.__leak_canary_row_text);

            boolean isThreadStackEntry = position == POSITION_THREAD_STACK + 1;
            String element = getItem(position);
            String htmlString = elementToHtmlString(element, position, mFoldings[position]);
            if (isThreadStackEntry && !mFoldings[position]) {
                htmlString += " <font color='#919191'>" + "blocked" + "</font>";
            }
            textView.setText(Html.fromHtml(htmlString));

            DisplayConnectorView connectorView = findById(convertView, R.id.__leak_canary_row_connector);
            connectorView.setType(connectorViewType(position));

            MoreDetailsView moreDetailsView = findById(convertView, R.id.__leak_canary_row_more);
            moreDetailsView.setFolding(mFoldings[position]);
        }

        return convertView;
    }

    private DisplayConnectorView.Type connectorViewType(int position) {
        return (position == 1) ? DisplayConnectorView.Type.START : (
                (position == getCount() - 1) ? DisplayConnectorView.Type.END :
                        DisplayConnectorView.Type.NODE);
    }

    private String elementToHtmlString(String element, int position, boolean folding) {
        String htmlString = element.replaceAll(BlockInfo.SEPARATOR, "<br>");

        switch (position) {
            case POSITION_BASIC:
                if (folding) {
                    htmlString = htmlString.substring(htmlString.indexOf(BlockInfo.KEY_CPU_CORE));
                }
                htmlString = String.format("<font color='#c48a47'>%s</font> ", htmlString);
                break;
            case POSITION_TIME:
                if (folding) {
                    htmlString = htmlString.substring(0, htmlString.indexOf(BlockInfo.KEY_TIME_COST_START));
                }
                htmlString = String.format("<font color='#f3cf83'>%s</font> ", htmlString);
                break;
            case POSITION_CPU:
                // FIXME Figure out why sometimes \r\n cannot replace completely
                htmlString = element;
                if (folding) {
                    htmlString = htmlString.substring(0, htmlString.indexOf(BlockInfo.KEY_CPU_RATE));
                }
                htmlString = htmlString.replace("cpurate = ", "<br>cpurate<br/>");
                htmlString = String.format("<font color='#998bb5'>%s</font> ", htmlString);
                htmlString = htmlString.replaceAll("]", "]<br>");
                break;
            case POSITION_THREAD_STACK:
            default:
                if (folding) {
                    for (String concernPackage : BlockCanaryUtils.getConcernPackages()) {
                        int index = htmlString.indexOf(concernPackage);
                        if (index > 0) {
                            htmlString = htmlString.substring(index);
                            break;
                        }
                    }
                }
                htmlString = String.format("<font color='#ffffff'>%s</font> ", htmlString);
                break;
        }
        return htmlString;
    }

    public void update(BlockInfo blockInfo) {
        if (mBlockInfo != null && blockInfo.timeStart.equals(mBlockInfo.timeStart)) {
            // Same data, nothing to change.
            return;
        }
        mBlockInfo = blockInfo;
        mFoldings = new boolean[POSITION_THREAD_STACK + mBlockInfo.threadStackEntries.size()];
        Arrays.fill(mFoldings, true);
        notifyDataSetChanged();
    }

    public void toggleRow(int position) {
        mFoldings[position] = !mFoldings[position];
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mBlockInfo == null) {
            return 0;
        }
        return POSITION_THREAD_STACK + mBlockInfo.threadStackEntries.size();
    }

    @Override
    public String getItem(int position) {
        if (getItemViewType(position) == TOP_ROW) {
            return null;
        }
        switch (position) {
            case POSITION_BASIC:
                return mBlockInfo.getBasicString();
            case POSITION_TIME:
                return mBlockInfo.getTimeString();
            case POSITION_CPU:
                return mBlockInfo.getCpuString();
            case POSITION_THREAD_STACK:
            default:
                return mBlockInfo.threadStackEntries.get(position - POSITION_THREAD_STACK);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TOP_ROW;
        }
        return NORMAL_ROW;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    private static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }
}
