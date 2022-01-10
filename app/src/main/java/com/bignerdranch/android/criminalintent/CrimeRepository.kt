package com.bignerdranch.android.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.CrimeDatabase
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    // 添加属性，保存数据库和访问接口DAO对象
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext, // 此处传入应用程序上下文，生命周期最长。
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).build()

    // 保存DAO
    private val crimeDao = database.crimeDao()
    // 初始化后台线程实例，用于执行数据库操作
    private val executor = Executors.newSingleThreadExecutor()
    /**
     * 添加仓库函数用于外部取用
     */
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    /**
     * 添加更新与插入数据库函数，用于外部取用。由于数据库操作必须在后台执行，使用线程封装
     * executor被设定为只会使用后台线程（为什么）
     */
    fun updateCrime(crime:Crime){
        executor.execute{
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime:Crime){
        executor.execute{
            crimeDao.addCrime(crime)
        }
    }


    // 添加构造函数。根据需要，提供NewInstance或者get...
    // 此处实现了一个单例模式
    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}