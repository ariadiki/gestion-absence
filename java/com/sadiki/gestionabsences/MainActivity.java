package com.sadiki.gestionabsences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.sadiki.gestionabsences.Adapter.ViewPagerAdapter;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;
import com.sadiki.gestionabsences.Model.Group;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    Toolbar toolbar;
    Group group;
    ViewPagerAdapter vpAdapter;
    FirebaseHelper firebaseHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseHelper= FirebaseHelper.getInstance();
        //toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //recevoir l'objet
        group = (Group) getIntent().getSerializableExtra("group");
        getSupportActionBar().setTitle(group.getNomGroup());
        getSupportActionBar().setSubtitle(group.getDescription());
        //ViewPager
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.pageview);

        tabLayout.setupWithViewPager(viewPager);
        vpAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new MembresFragment(group), getString(R.string.membres));
        vpAdapter.addFragment(new PresenceFragment(group), getString(R.string.presence));
        vpAdapter.addFragment(new StatistiqueFragment(group), getString(R.string.statistique));
        viewPager.setAdapter(vpAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            firebaseHelper.logout();
            // Get the fragment manager for this activity
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Get a list of all fragments currently attached to the activity
            List<Fragment> fragments = fragmentManager.getFragments();

            // Iterate over the list of fragments and call onDestroyView() on each  one
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onDestroyView();
                }
            }
            Intent intent = new Intent(this, Authentification.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}