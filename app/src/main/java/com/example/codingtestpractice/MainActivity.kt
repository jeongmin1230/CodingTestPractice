package com.example.codingtestpractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.codingtestpractice.ui.theme.CodingTestPracticeTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
    val navController = rememberNavController()
    NavHost(navController, startDestination = "select_problem") {
        composable("select_problem") {
            Column(Modifier.fillMaxSize()) {
                SelectProblem(navController)
            }
        }
        composable("show_detail/{title}", arguments = listOf(navArgument("title") { type = NavType.StringType})) {
            Column(Modifier.fillMaxSize()) {
                ShowDetail(it.arguments?.getString("title").toString())
            }
        }
    }
}

@Composable
fun SelectProblem(navController: NavHostController) {
    val problemTitleState = remember { mutableStateOf(emptyList<String>()) }
    val problemTitleRef = FirebaseDatabase.getInstance().getReference("problem")
    val day1Ref = problemTitleRef.child("day 1")

    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val day1List = mutableListOf<String>()
            for(childSnapShot in snapshot.children) {
                val day1Text = childSnapShot.key
                println(childSnapShot)
                if(day1Text != null) {
                    day1List.add(day1Text.toString())
                }
            }
            problemTitleState.value = day1List
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
    day1Ref.addValueEventListener(valueEventListener)

    Column {
        Text(
            text = "[ ${day1Ref.key} ]",
            style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp)
        )
        problemTitleState.value.forEach { title ->
            ItemLayout(title) {
                navController.navigate("show_detail/$title")
            }
        }
    }
}

@Composable
fun ItemLayout(title: String, clickAction: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { clickAction() }
        .padding(start = 12.dp, bottom = 10.dp, end = 12.dp)
        .background(Color(0XFFD09AFF), shape = CircleShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, top = 12.dp, bottom = 12.dp)
        )
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
            contentDescription = stringResource(id = R.string.ic_arrow_right)
        )
    }
}

@Composable
fun ShowDetail(title: String) {
    Text(text = title)
}