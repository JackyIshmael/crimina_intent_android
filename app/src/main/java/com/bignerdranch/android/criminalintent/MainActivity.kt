package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(),CrimeListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 如果FragmentManager队列中存在fragment，直接用资源id调用
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment === null) {
            val fragment = CrimeListFragment.newInstance()
            // 新建fragment事务、添加资源id绑定实例、commit
            supportFragmentManager.beginTransaction().add(R.id.fragment_container,fragment).commit()
        }

    }

    /**
     * 实现Crime item点击事件接口，用户点击时使用CrimeFragment实例替换CrimeListFragment
     * param: crimeId
     *
     */
    override fun onCrimeSelected(crimeId: UUID) {
        Log.d(TAG,"You clicked this, you fucked up $crimeId")

        // 进行fragment替换
        val fragment = CrimeFragment.newInstance(crimeId)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).addToBackStack(null).commit()


    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            Log.i("MainActivity", "popping backstack")
            fm.popBackStack()
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super")
            super.onBackPressed()
        }
    }
}