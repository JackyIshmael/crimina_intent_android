package com.bignerdranch.android.criminalintent

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.lang.reflect.Constructor
import java.util.*



@Entity
data class Crime constructor(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = ""
){
    @Ignore
    constructor() : this(UUID.randomUUID(),"默认值")
}