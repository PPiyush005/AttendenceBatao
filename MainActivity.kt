package com.Piyush.attendencebatao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.Piyush.attendencebatao.ui.theme.AttendencebataoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AttendencebataoTheme  {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}