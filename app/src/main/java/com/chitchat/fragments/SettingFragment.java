package com.chitchat.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.FireBaseUtil;
import com.chitchat.Utilities.MySharedPref;
import com.chitchat.activities.HomeActivity;
import com.chitchat.activities.SignUpActivity;

import java.io.File;

public class SettingFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        initViews(view);
        return view;
    }

    /**
     * Method to initialise the views
     */
    private void initViews(View view) {
        Toolbar tbToolbar = view.findViewById(R.id.tb_toolbar);
        view.findViewById(R.id.iv_toolbar_left).setVisibility(View.GONE);
        view.findViewById(R.id.tv_last_seen).setVisibility(View.GONE);
        ((HomeActivity) getActivity()).setSupportActionBar(tbToolbar);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.settings_title));
        view.findViewById(R.id.iv_toolbar_right).setVisibility(View.GONE);
        view.findViewById(R.id.iv_under_construction).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        MySharedPref.getPreference(getActivity()).clearPreferences();
        FireBaseUtil.getInstance().signOut();
        //clearApplicationData(getActivity());
        startActivity(new Intent(getActivity(), SignUpActivity.class));
    }

    public static void clearApplicationData(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                File f = new File(appDir, s);
                if(deleteDir(f))
                    Log.i("Kartik", String.format("**************** DELETED -> (%s) *******************", f.getAbsolutePath()));
            }
        }
    }
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
