package com.example.util.simpletimetracker.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val color: AppColor,
    val note: String,
)