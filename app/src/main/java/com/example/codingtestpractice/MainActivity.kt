package com.example.codingtestpractice

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.codingtestpractice.platform.LeetCodeScreen
import com.example.codingtestpractice.platform.ProgrammersScreen
import com.example.codingtestpractice.ui.theme.CodingTestPracticeTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/** 구상 : 처음에 무슨 앱인지 정보, 플랫폼 고르는 화면
 * 고르면 그 플랫폼 에 맞는 문제들 출력, 일단 프로그래머스 만 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodingTestPracticeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val homeNavController = rememberNavController()
    val platform = stringArrayResource(R.array.platform)

    NavHost(homeNavController, startDestination = "choose") {
        composable("choose") {
            anonymousLogin()
            Platform(platform, homeNavController)
        }
        composable("programmers") {
            ProgrammersScreen(homeNavController)
        }
        composable("leetcode") {
            LeetCodeScreen(homeNavController)
        }
    }
}

@Composable
fun Platform(platform: Array<String>, navController: NavHostController) {
    Column(modifier = Modifier
        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
        .fillMaxSize()) {
        platform.forEach { name ->
            EachPlatform(name, navController)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun EachPlatform(name: String, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(name) }
            .border(1.dp, Color.Black),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(Color.Black),
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

fun anonymousLogin() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    auth.signInAnonymously()
        .addOnCompleteListener { task ->
            if(task.isSuccessful) {
                User.uid = user!!.uid
            }
        }
}