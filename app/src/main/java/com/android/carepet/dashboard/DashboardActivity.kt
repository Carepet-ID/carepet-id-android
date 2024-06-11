package com.android.carepet.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.carepet.R
import com.android.carepet.dashboard.fragment.AccountFragment
import com.android.carepet.dashboard.fragment.ArticlesFragment
import com.android.carepet.dashboard.fragment.DogsFragment
import com.android.carepet.dashboard.fragment.HomeFragment
import com.android.carepet.view.settings.SettingsActivity
import com.android.carepet.dashboard.about.AboutActivity
import com.android.carepet.dashboard.bookmark.BookmarkActivity
import com.android.carepet.data.di.Injection
import com.android.carepet.data.pref.UserRepository
import com.android.carepet.view.detail.DetailDiseaseActivity
import com.android.carepet.view.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            showBottomSheetDialog()
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleBottomNavigationItemSelected(item)
        }

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            navigationView.setCheckedItem(R.id.nav_home)
            bottomNavigationView.selectedItemId = R.id.home
        }

        if (intent.getBooleanExtra("openHomeFragment", false)) {
            replaceFragment(HomeFragment())
        }

        userRepository = Injection.provideRepository(this)
    }

    private fun handleBottomNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                replaceFragment(HomeFragment())
                true
            }
            R.id.articles -> {
                replaceFragment(ArticlesFragment())
                true
            }
            R.id.dogs -> {
                replaceFragment(DogsFragment())
                true
            }
            R.id.account -> {
                replaceFragment(AccountFragment())
                true
            }
            else -> false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val menu = navigationView.menu
        val group = menu.findItem(R.id.nav_home).groupId

        menu.setGroupCheckable(group, true, true)

        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(HomeFragment())
                findViewById<BottomNavigationView>(R.id.bottomNavigationView).selectedItemId = R.id.home
            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_bookmark -> {
                val intent = Intent(this, BookmarkActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                logoutUser()
            }
        }
        menu.setGroupCheckable(group, false, true)

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            userRepository.logout()
            Toast.makeText(this@DashboardActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheetlayout, null)
        bottomSheetDialog.setContentView(view)

        val takePhotoLayout: LinearLayout = view.findViewById(R.id.layoutVideo)
        val uploadImageLayout: LinearLayout = view.findViewById(R.id.layoutImage)

        takePhotoLayout.setOnClickListener {
            Toast.makeText(this, "Take a Photo clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        uploadImageLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
            bottomSheetDialog.dismiss()
        }

        val cancelButton: ImageView = view.findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val intent = Intent(this, DetailDiseaseActivity::class.java).apply {
                    putExtra("IMAGE_URI", uri.toString())
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
