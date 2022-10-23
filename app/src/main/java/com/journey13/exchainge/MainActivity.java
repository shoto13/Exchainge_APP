//TASK LIST FOR APP

//TODO: CREATE SINGNUP AND SIGNIN WIREFRAME  -- COMPLETE
//TODO: CREATE MESSAGING INTERFACE WIREFRAME
//TODO: CREATE SETTINS WIREFRAME
//TODO: CREATE USER SEARCH WIREFRAME

//TODO: CREATE LOGIN/SIGNUP FUNCTIONALITY (FIREBASE? - MAKE IT REUSABLE SO IT CAN BE USED WITH IOS)
//TODO: CREATE USER LIST AND ADD AND SEARCH FUNCTIONALITY
//TODO: CREATE MESSAGING FUNCTIONALITY
//TODO: CREATE SETTINGS FUNCTIONALITY

package com.journey13.exchainge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.Value;
import com.journey13.exchainge.Fragments.ChatsFragment;
import com.journey13.exchainge.Fragments.ProfileFragment;
import com.journey13.exchainge.Fragments.UsersFragment;
import com.journey13.exchainge.Fragments.WalletFragment;
import com.journey13.exchainge.Model.Chat;
import com.journey13.exchainge.Model.User;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profilePic;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private DrawerLayout mDrawer;
    private Toolbar toolbar2;
    private NavigationView nvDrawer;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DRAWERS
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        setInitialFragment();

        //NAVIGATION VIEW MENU
        NavigationView navigationView = (NavigationView) findViewById(R.id.nvView);
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView usernameText = headerLayout.findViewById(R.id.usernameTextView);
        ImageView profilePic = headerLayout.findViewById(R.id.navProfileImage);
        TextView taglineText = headerLayout.findViewById(R.id.taglineTextView);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                usernameText.setText(user.getUsername());
                taglineText.setText(user.getTagline());

                String imgUrl = user.getImageURL();

                //Load profile image into navigation drawer
                if (user.getImageURL().equals("default")) {
                    Glide.with(getApplicationContext()).load(R.mipmap.ic_launcher).into(profilePic);

                } else {

                    Glide.with(getApplicationContext())
                            .load(user.getImageURL())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.andromeda_galaxy)
                                    .fitCenter())
                            .into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (navigationView.getHeaderCount() > 0) {
            // avoid NPE by first checking if there is at least one Header View available
            //View headerLayout = navigationView.getHeaderView(0);
        }

        //Set up hamburger icon in action bar for our drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open_drawer_res, R.string.close_drawer_res)
        {
            public void onDrawerClosed(View view)
            {
                supportInvalidateOptionsMenu();
                //drawerOpened = false;
            }

            public void onDrawerOpened(View drawerView)
            {
                supportInvalidateOptionsMenu();
                //drawerOpened = true;
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        mDrawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

            }


    //Get and sync state for hamburger icon in action bar
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = ChatsFragment.class;
                break;
            case R.id.nav_second_fragment:
                fragmentClass = UsersFragment.class;
                break;
            case R.id.nav_third_fragment:
                fragmentClass = ProfileFragment.class;
                break;
            case R.id.nav_wallet:
                fragmentClass = WalletFragment.class;
                break;
            default:
                fragmentClass = ChatsFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    // SETS UP INITIAL FRAGMENT TO CONVERSATIONS FRAGMENT TODO:: CHECK IF THIS IS ACTUALLY A GOOD WAY OF MANAGING INITIAL FRAGMENT LAUNCHING (IT PROBABLY ISNT)
    public void setInitialFragment () {

        Fragment fragment = null;
        Class fragmentClass;

        fragmentClass = ChatsFragment.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(MainActivity.this, init_landing.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        System.out.println("WE HAVE SUCCESSFUL INFLATION!!!!");
        return true;
    }


    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("Offline");
    }
}

//TODO:: CREATE A LIST VIEW IN SETTINGS IN ORDER TO LIST OUT OPTIONS (SEE TELEGRAM)
//TODO:: SEE IF i CAN FIX THE GLIDE IMAGEVIEW
//TODO:: UPDATE STYLE TO DARK STYLE
//TODO:: UPDATE LOGIN AND SIGNUP PAGE TO BE THE SAME

//TODO:: CREATE FUNCTIONING SEARCH SYSTEM TO LOOK UP AND CONVERSE WITH USERS (SO ONLY THE RIGHT PEOPLE SHOW UP IN USERS)