package com.stylingandroid.muselee.providers

interface DataProvider<T> {

    fun requestData(callback: (item: T) -> Unit)
}
