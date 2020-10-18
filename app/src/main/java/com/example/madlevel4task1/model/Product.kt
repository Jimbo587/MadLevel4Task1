package com.example.madlevel4task1.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Creating the database structure of each column and table
@Entity(tableName = "productTable")
data class Product (

    @ColumnInfo(name = "amount")
    var amount: Short,

    @ColumnInfo(name = "product")
    var product: String,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null

)