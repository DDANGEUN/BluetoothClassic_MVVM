package com.lilly.bluetoothclassic.di

import com.lilly.bluetoothclassic.Repository
import com.lilly.bluetoothclassic.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
}

val repositoryModule = module{
    single{
        Repository()
    }
}
