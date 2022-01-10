package com.bignerdranch.android.criminalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel : ViewModel() {

    // 需要将crimes目前的生成方式给注释掉

//    val crimes = mutableListOf<Crime>()
//
//    init {
//        for (i in 0 until 100) {
//            val crime = Crime()
//            crime.isSolved = i % 2 == 0
//            crime.title = "Crime #$i"
//            crimes += crime
//        }
//    }

    private val  crimeRepository = CrimeRepository.get()
    val crimesListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime:Crime){
        crimeRepository.addCrime(crime)
    }
}