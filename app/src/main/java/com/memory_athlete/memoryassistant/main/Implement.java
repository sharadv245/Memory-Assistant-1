package com.memory_athlete.memoryassistant.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.memory_athlete.memoryassistant.Helper;
import com.memory_athlete.memoryassistant.R;
import com.memory_athlete.memoryassistant.lessons.ImplementLesson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import timber.log.Timber;

public class Implement extends AppCompatActivity {
    ArrayList<String> pathList = new ArrayList<>();
    int listViewId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Helper.theme(this, Implement.this);
        setContentView(R.layout.activity_implement_list);
        setTitle(getString(R.string.apply));
        Timber.v("Title Set");
        Timber.v(Helper.TYPE + " " + intent.getStringExtra(Helper.TYPE));
        int s = intent.getIntExtra(Helper.TYPE, 0);
        if (s == 0) throw new RuntimeException("Error in getting 'Implement' from the intent");
        pathList.add(getString(s));

        setAdapter();
    }

    @Override
    public void onBackPressed() {
        if (listViewId == 0) {
            super.onBackPressed();
            return;
        }
        LinearLayout linearLayout = findViewById(R.id.apply_layout);
        linearLayout.removeViewAt(listViewId--);
        linearLayout.findViewById(listViewId).setVisibility(View.VISIBLE);
        pathList.remove(pathList.size() - 1);
    }

    public void setAdapter() {
        try {
            StringBuilder path = new StringBuilder("");
            for (String i : pathList) path.append(i);
            Timber.v("path = " + path);
            String[] list = listAssetFiles(path.toString());
            Timber.v("list set");
            if (list == null) {
                Toast.makeText(this, "Nothing here", Toast.LENGTH_SHORT).show();
                return;
            }
            Timber.v("list.size() = " + list.length);
            final ArrayList<Item> arrayList = new ArrayList<>();
            for (String i : list) arrayList.add(new Item(i, true));
            Timber.v("arrayList set");
            ApplyAdapter adapter = new ApplyAdapter(this, arrayList);
            ListView listView = new ListView(this);
            listView.setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
            listView.setDividerHeight(0);

            listView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            listView.setId(listViewId);
            final LinearLayout linearLayout = findViewById(R.id.apply_layout);
            linearLayout.addView(listView);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    Item item = arrayList.get(position);
                    boolean webView, hasList;

                    if (checkPathContents(pathList, "Language") || item.mFileName.equals("Dates.txt")) {
                        webView = false;
                        hasList = true;
                    } else {
                        webView = true;
                        hasList = false;
                    }

                    if (item.mFileName.endsWith(".txt")) {
                        Intent intent = new Intent(getApplicationContext(), ImplementLesson.class);
                        intent.putExtra("headerString", item.mItem);
                        intent.putExtra("webView", webView);
                        intent.putExtra("list", hasList);
                        intent.putExtra("resource", true);
                        StringBuilder path = new StringBuilder("");
                        for (String i : pathList) path.append(i);
                        intent.putExtra("fileString", path + "/" + item.mFileName);
                        startActivity(intent);
                    } else {
                        pathList.add("/" + item.mFileName);
                        linearLayout.findViewById(listViewId).setVisibility(View.GONE);
                        listViewId++;
                        setAdapter();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show();
        }
    }

    // suppressed because there will be more parameters in the future
    boolean checkPathContents(ArrayList<String> strings, @SuppressWarnings("SameParameterValue") String pattern){
        for (String s : strings) if (s.contains(pattern)) return true;
        return false;
    }

    private String[] listAssetFiles(String path) {

        String[] list;
        try {
            list = getAssets().list(path);
            Timber.v("got assets");
        } catch (IOException e) {
            Toast.makeText(this, "Nothing here", Toast.LENGTH_SHORT).show();
            throw new RuntimeException("error recovering asset - " + path);
        }
        return list;
    }

    private class Item {
        String mItem, mFileName;
        boolean webView;

        Item(String item, boolean wV) {
            mFileName = item;
            mItem = item.endsWith(".txt") ? item.substring(0, item.length() - 4) : item;
            webView = wV;
        }
    }

    private class ApplyAdapter extends ArrayAdapter<Item> {

        ApplyAdapter(Activity context, ArrayList<Item> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, View listItemView, @NonNull ViewGroup parent) {
            if (listItemView == null) listItemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_file, null, true);

            TextView textView = listItemView.findViewById(R.id.main_textView);
            textView.setText(Objects.requireNonNull(getItem(position)).mItem);

            return listItemView;
        }
    }
}
