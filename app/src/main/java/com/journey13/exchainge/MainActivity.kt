
package com.journey13.exchainge

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.os.Bundle
import com.journey13.exchainge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseError
import com.journey13.exchainge.Fragments.ChatsFragment
import com.journey13.exchainge.Fragments.UsersFragment
import com.journey13.exchainge.Fragments.ProfileFragment
import com.journey13.exchainge.Fragments.WalletFragment
import androidx.core.view.GravityCompat
import android.content.Intent
import android.content.res.Configuration
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.journey13.exchainge.Model.User
import com.journey13.exchainge.init_landing
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.HashMap

class MainActivity : AppCompatActivity() {
    var profilePic: CircleImageView? = null
    var username: TextView? = null
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    private var mDrawer: DrawerLayout? = null
    private val toolbar2: Toolbar? = null
    private var nvDrawer: NavigationView? = null
    private var drawerToggle: ActionBarDrawerToggle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //DRAWERS
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mDrawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        nvDrawer = findViewById<View>(R.id.nvView) as NavigationView
        // Setup drawer view
        setupDrawerContent(nvDrawer)
        setInitialFragment()

        //NAVIGATION VIEW MENU
        val navigationView = findViewById<View>(R.id.nvView) as NavigationView
        val headerLayout = navigationView.inflateHeaderView(R.layout.nav_header)
        val usernameText = headerLayout.findViewById<TextView>(R.id.usernameTextView)
        val profilePic = headerLayout.findViewById<ImageView>(R.id.navProfileImage)
        val taglineText = headerLayout.findViewById<TextView>(R.id.taglineTextView)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference =
            FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users").child(
                firebaseUser!!.uid
            )
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(
                    User::class.java
                )
                usernameText.text = user!!.username
                taglineText.text = user.tagline
                val imgUrl = user.imageURL

                //Load profile image into navigation drawer
                if (user.imageURL == "default") {
                    Glide.with(applicationContext).load(R.mipmap.ic_launcher).into(profilePic)
                } else {
                    Glide.with(applicationContext)
                        .load(user.imageURL)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.andromeda_galaxy)
                                .fitCenter()
                        )
                        .into(profilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        if (navigationView.headerCount > 0) {
            //Checking if there is at least one Header View available
            //View headerLayout = navigationView.getHeaderView(0);
        }

        //Set up hamburger icon in action bar for our drawer toggle
        drawerToggle = object : ActionBarDrawerToggle(
            this,
            mDrawer,
            toolbar,
            R.string.open_drawer_res,
            R.string.close_drawer_res
        ) {
            override fun onDrawerClosed(view: View) {
                supportInvalidateOptionsMenu()
                //drawerOpened = false;
            }

            override fun onDrawerOpened(drawerView: View) {
                supportInvalidateOptionsMenu()
                //drawerOpened = true;
            }
        }
        drawerToggle?.setDrawerIndicatorEnabled(true)
        mDrawer!!.setDrawerListener(drawerToggle)
        drawerToggle?.syncState()
    }

    //Get and sync state for hamburger icon in action bar
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    private fun setupDrawerContent(navigationView: NavigationView?) {
        navigationView!!.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    fun selectDrawerItem(menuItem: MenuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        var fragment: Fragment? = null
        val fragmentClass: Class<*>
        fragmentClass = when (menuItem.itemId) {
            R.id.nav_first_fragment -> ChatsFragment::class.java
            R.id.nav_second_fragment -> UsersFragment::class.java
            R.id.nav_third_fragment -> ProfileFragment::class.java
            R.id.nav_wallet -> WalletFragment::class.java
            else -> ChatsFragment::class.java
        }
        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Insert the fragment by replacing any existing fragment
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commit()

        // Highlight the selected item has been done by NavigationView
        menuItem.isChecked = true
        // Set action bar title
        title = menuItem.title
        // Close the navigation drawer
        mDrawer!!.closeDrawers()
    }

    // SETS UP INITIAL FRAGMENT TO CONVERSATIONS FRAGMENT TODO:: CHECK IF THIS IS ACTUALLY A GOOD WAY OF MANAGING INITIAL FRAGMENT LAUNCHING (IT PROBABLY ISNT)
    fun setInitialFragment() {
        var fragment: Fragment? = null
        val fragmentClass: Class<*>
        fragmentClass = ChatsFragment::class.java
        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Insert the fragment by replacing any existing fragment
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        when (item.itemId) {
            android.R.id.home -> {
                mDrawer!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(
                    Intent(
                        this@MainActivity,
                        init_landing::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun status(status: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reference!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        status("Online")
    }

    override fun onPause() {
        super.onPause()
        status("Offline")
    }
}
