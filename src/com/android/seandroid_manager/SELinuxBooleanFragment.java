
package com.android.seandroid_manager;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.SELinux;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class SELinuxBooleanFragment extends ListFragment {

    private myBooleanAdapter mAdapter;
    
    private class myBooleanAdapter extends ArrayAdapter<String> {
        private final LayoutInflater mInflater;
        private String[] mBooleans;
        
        public myBooleanAdapter(Context context, int textViewResourceId, String[] items) {
            super(context, textViewResourceId, items);
            mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mBooleans = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView  == null) {
                convertView = mInflater.inflate(R.layout.selinux_manage_booleans_item, parent, 
                                                false);
                holder = new ViewHolder();
                holder.tx = (TextView) convertView.findViewById(R.id.text);
                holder.cb = (CheckBox) convertView.findViewById(R.id.checkbox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            String name = mBooleans[position];
            holder.tx.setText(name);
            holder.cb.setChecked(SELinux.getBooleanValue(name));
            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        String name = (String)holder.tx.getText();
                        SELinux.setBooleanValue(name, isChecked);
                        holder.cb.setChecked(SELinux.getBooleanValue(name));
                        saveBooleans();
                    }
                });
            return convertView;
        }
        
        private class ViewHolder {
            TextView tx;
            CheckBox cb;
        }
        
        private void saveBooleans()
        {
            String[] mNames = SELinux.getBooleanNames();
            StringBuilder newBooleanList = new StringBuilder();
            boolean first = true;
            for (String n : mNames) {
                if (first) {
                    first = false;
                } else {
                    newBooleanList.append(",");
                }
                newBooleanList.append(n);
                newBooleanList.append(":");
                newBooleanList.append(SELinux.getBooleanValue(n) ? 1 : 0);
            }
            Settings.Secure.putString(getActivity().getContentResolver(),
                                      Settings.Secure.SELINUX_BOOLEANS,
                                      newBooleanList.toString());
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getActivity().getText(R.string.selinux_no_booleans));
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new myBooleanAdapter(getActivity(), R.layout.selinux_manage_booleans,
                                        SELinux.getBooleanNames());
        setListAdapter(mAdapter);
    }
}

