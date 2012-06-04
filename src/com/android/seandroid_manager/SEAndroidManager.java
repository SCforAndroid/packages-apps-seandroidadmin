
package com.android.seandroid_manager;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.os.SELinux;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SEAndroidManager extends PreferenceActivity {

    private List<Header> mHeaders;
    private Header mFirstHeader;

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (SELinux.isSELinuxEnabled()) {
            loadHeadersFromResource(R.xml.enabled_headers, target);

        } else {
            loadHeadersFromResource(R.xml.disabled_headers, target);
        }

        mHeaders = target;

        // find the first header (non CATEGORY), Android expects the first to have a fragment
        int i = 0;
        while (i < target.size()) {
            Header header = target.get(i);
            if (HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                mFirstHeader = header;
                break;
            }
            i++;
        }
    }

    @Override
    public Header onGetInitialHeader() {
        return mFirstHeader;
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_NORMAL + 1;

        private static class HeaderViewHolder {
            TextView title;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                com.android.internal.R.layout.simple_list_item_1, parent,
                                false);

                        holder.title = (TextView)
                                view.findViewById(com.android.internal.R.id.text1);

                        holder.title.setPadding(25,0,0,0);
                        break;
                }

                holder.title.setText(header.getTitle(getContext().getResources()));
                view.setTag(holder);

            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            return view;
        }
    }

   @Override
   public void setListAdapter(ListAdapter adapter) {
       if (mHeaders == null) {
           mHeaders = new ArrayList<Header>();
           for (int i = 0; i < adapter.getCount(); i++) {
               mHeaders.add((Header) adapter.getItem(i));
           }
       }
       super.setListAdapter(new HeaderAdapter(this, mHeaders));
   }
}
