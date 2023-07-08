package com.example.util.simpletimetracker.domain.interactor

sealed interface AppLanguage {

    object System : AppLanguage
    object English : AppLanguage
    object Catalan : AppLanguage
    object German : AppLanguage
    object Spanish : AppLanguage
    object Farsi : AppLanguage
    object French : AppLanguage
    object Hindi : AppLanguage
    object Indonesian : AppLanguage
    object Italian : AppLanguage
    object Japanese : AppLanguage
    object Dutch : AppLanguage
    object Portuguese : AppLanguage
    object Russian : AppLanguage
    object Swedish : AppLanguage
    object Turkish : AppLanguage
    object Ukrainian : AppLanguage
    object ChineseSimplified : AppLanguage
    object ChineseTraditional : AppLanguage
}