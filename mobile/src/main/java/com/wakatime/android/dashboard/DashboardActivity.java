package com.wakatime.android.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.wakatime.android.AboutActivity;
import com.wakatime.android.R;
import com.wakatime.android.WakatimeApplication;
import com.wakatime.android.dashboard.stats.LastSevenDaysFragment;
import com.wakatime.android.dashboard.stats.TabbedEnvironmentFragment;
import com.wakatime.android.dashboard.leaderboard.Leader;
import com.wakatime.android.dashboard.leaderboard.LeaderProfileFragment;
import com.wakatime.android.dashboard.leaderboard.LeaderboardFragment;
import com.wakatime.android.dashboard.model.Project;
import com.wakatime.android.dashboard.project.ProjectFragment;
import com.wakatime.android.dashboard.project.SingleProjectFragment;
import com.wakatime.android.support.view.NavigationHeaderView;
import com.wakatime.android.user.UserStartActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DashboardActivity extends AppCompatActivity
    implements LogoutActionView, NavigationView.OnNavigationItemSelectedListener,
    LastSevenDaysFragment.OnProgrammingFragmentInteractionListener,
    ProjectFragment.OnProjectFragmentInteractionListener,
    LeaderboardFragment.OnLeaderListFragmentInteractionListener,
    SingleProjectFragment.OnSingleProjectInteractionListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.content_dashboard)
    FrameLayout contentDashboard;

    @BindView(R.id.nav_view)
    NavigationView navView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Inject
    NavigationHeaderView navigationHeaderView;

    @Inject
    LogoutHandler mLogoutHandler;

    private Tracker mTracker;

    private Fragment tabHostFragment;

    private Fragment projectFragment;

    private Fragment leaderboardFragment;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);

        ((WakatimeApplication) this.getApplication()).useNetworkComponent()
            .inject(this);

        toolbar.setTitle(R.string.title_activity_dashboard);
        setSupportActionBar(toolbar);

        restoreFragments(savedInstanceState);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);
        navigationHeaderView.on(navView.getHeaderView(0)).load();
        // Called new fragment when there is no other to saved
        if (savedInstanceState == null) {
            changeToDefaultFragment();
        }

        mTracker = ((WakatimeApplication) getApplication()).getTracker();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Dashboard");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tabHostFragment != null && tabHostFragment.isAdded()) {
            getSupportFragmentManager()
                .putFragment(outState, TabbedEnvironmentFragment.KEY, this.tabHostFragment);
        }
        if (projectFragment != null && projectFragment.isAdded()) {
            getSupportFragmentManager()
                .putFragment(outState, ProjectFragment.KEY, this.projectFragment);
        }

        if (leaderboardFragment != null && leaderboardFragment.isAdded()) {
            getSupportFragmentManager()
                .putFragment(outState, LeaderboardFragment.KEY, this.leaderboardFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.drawer_environment) {
            changeToDefaultFragment();
        } else if (id == R.id.drawer_projects) {
            this.projectFragment = ProjectFragment.newInstance();
            changeFragment(this.projectFragment);
        } else if (id == R.id.drawer_leaderboard) {
            this.leaderboardFragment = LeaderboardFragment.newInstance();
            changeFragment(this.leaderboardFragment);
        } else if (id == R.id.drawer_logout) {
            this.logout();
        } else if (id == R.id.drawer_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else {
            changeToDefaultFragment();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeToDefaultFragment() {
        this.tabHostFragment = TabbedEnvironmentFragment.newInstance();
        changeFragment(this.tabHostFragment);
    }

    private void restoreFragments(Bundle bundle) {
        if (bundle == null) return;

        this.tabHostFragment = getSupportFragmentManager()
            .getFragment(bundle, TabbedEnvironmentFragment.KEY);

        this.projectFragment = getSupportFragmentManager()
            .getFragment(bundle, ProjectFragment.KEY);

        this.leaderboardFragment = getSupportFragmentManager()
            .getFragment(bundle, LeaderboardFragment.KEY);
    }

    private void changeFragment(Fragment fragment) {
        this.getSupportFragmentManager().beginTransaction()
            .replace(R.id.content_dashboard, fragment)
            .commit();
    }

    @Override
    public void logout() {
        mLogoutHandler.clearData(() -> {
            startActivity(new Intent(this, UserStartActivity.class));
            finish();
        });

    }

    @Override
    public void onListFragmentInteraction(Leader leader) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content_dashboard, LeaderProfileFragment.newInstance(leader), leader.getUser().getName())
            .addToBackStack(leader.getUser().getName())
            .commit();
    }

    @Override
    public void showProjectPage(Project project) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content_dashboard, SingleProjectFragment.newInstance(project.getName()), project.getName())
            .addToBackStack(project.getName())
            .commit();
    }
}
