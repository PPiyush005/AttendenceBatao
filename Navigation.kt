package com.Piyush.attendencebatao

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.Piyush.attendencebatao.screens.AboutMeScreen
import com.Piyush.attendencebatao.screens.SubjectScreen


@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "subjects") {
        composable("subjects") { SubjectScreen(navController) }
        composable("about") { AboutMeScreen(navController) }
    }
}