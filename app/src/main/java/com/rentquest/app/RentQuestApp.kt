package com.rentquest.app

import android.app.Application
import com.rentquest.app.data.local.DataStoreManager

/**
 * RentQuest Application class
 * Initializes app-wide dependencies
 */
class RentQuestApp : Application() {
    
    lateinit var dataStoreManager: DataStoreManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(this)
    }
}
