package com.memory_athlete.memoryassistant.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.memory_athlete.memoryassistant.BuildConfig;
import com.memory_athlete.memoryassistant.Helper;
import com.memory_athlete.memoryassistant.R;
import com.memory_athlete.memoryassistant.mySpace.MySpace;
import com.memory_athlete.memoryassistant.reminders.ReminderUtils;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.widget.Toast.makeText;


public class MainActivity extends AppCompatActivity {
    boolean backPressed = false;
    private final int REQUEST_STORAGE_ACCESS = 444;
    private SharedPreferences sharedPreferences;

    @Override
    public void onBackPressed() {
        if (sharedPreferences.getBoolean(getString(R.string.double_back_to_exit), false)
                && !backPressed) {
            backPressed = true;
            makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        } else super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
        Helper.theme(this, MainActivity.this);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Runnable() {
            @Override
            public void run() {
                firstStart();
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.putLong("last_opened", System.currentTimeMillis());
                Timber.v("Last opened on" + System.currentTimeMillis());
                e.apply();
                ReminderUtils.scheduleReminder(getApplicationContext());
            }
        }.run();
    }

    void firstStart() {
        if (sharedPreferences.getLong("last_opened", 0) == 0) {
            AdView adView = findViewById(R.id.adView);
            adView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

            makeText(getApplicationContext(), R.string.confused, Toast.LENGTH_LONG).show();
            Timber.d("firstStart");
        }
    }

    private boolean mayAccessStorage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) requestPermissions(new
                String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_ACCESS);
        else requestPermissions(new
                String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_ACCESS);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_ACCESS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Helper.makeDirectory(Helper.APP_FOLDER);
            } else {
                Snackbar.make(findViewById(R.id.main_list), "The app might crash without these permissions",
                        Snackbar.LENGTH_SHORT).setAction("Grant", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        firstStart();
                    }
                });
            }
        }
    }

    public void setAdapter() {
        final List<Item> list = setList();

        MainAdapter adapter = new MainAdapter(this, list);
        ListView listView = findViewById(R.id.main_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Item item = list.get(position);
                Intent intent = new Intent(MainActivity.this, item.mClass);
                if (item.mItem == R.string.apply)
                    intent.putExtra(getString(R.string.apply), getString(R.string.apply));
                intent.putExtra(Helper.TYPE, item.mItem);
                startActivity(intent);
            }
        });
        Timber.v("Adapter set!");
    }

    private List<Item> setList() {
        return Arrays.asList(
                new Item(R.string.learn, R.drawable.learn, Learn.class),
                new Item(R.string.practice, R.drawable.practice, Practice.class),
                new Item(R.string.recall, R.drawable.recall, RecallSelector.class),
                new Item(R.string.apply, R.drawable.implement, Implement.class),
                new Item(R.string.my_space, R.drawable.my_space, MySpace.class),
                new Item(R.string.preferences, R.drawable.preferences, Preferences.class),
                new Item(R.string.get_pro, R.drawable.get_pro, GetPro.class));
        //list.add(new Item(R.string.login, Login.class));
        //list.add(new Item(R.string.reminders, ))
        //Timber.v("List set!");
    }

    private class Item {
        int mItem, mImageId;
        Class mClass;

        Item(int itemName, int im, Class class1) {
            mItem = itemName;
            mClass = class1;
            mImageId = im;
        }
    }

    private class MainAdapter extends ArrayAdapter<Item> {

        MainAdapter(Activity context, List<Item> words) {
            super(context, 0, words);
        }

        @SuppressLint("InflateParams")  // passing null as rootView
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.category, null, true);

            TextView textView = convertView.findViewById(R.id.text);
            textView.setText(getString(Objects.requireNonNull(getItem(position)).mItem));
            ImageView img = convertView.findViewById(R.id.image);
            Picasso
                    .with(getApplicationContext())
                    .load(Objects.requireNonNull(getItem(position)).mImageId)
                    .placeholder(R.mipmap.ic_launcher)
                    .fit()
                    .centerCrop()
                    //.centerInside()                 // or .centerCrop() to avoid a stretched imageÒ
                    .into(img);

            return convertView;
        }
    }
}

