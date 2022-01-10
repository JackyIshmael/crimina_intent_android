package com.bignerdranch.android.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.criminalintent.Crime
import java.util.*

// 定义一个Database Access Object 数据库访问对象，等同于DBMS，调用其函数即访问数据库内容
@Dao
interface CrimeDao {
    // 直接返回全部Crime
    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    // 接收id参数，返回Crime单个实例
    @Query("SELECT * FROM crime WHERE id =(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)
}