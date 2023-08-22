package com.example.codingtestpractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
                ShowDetail(it.arguments?.getString("title").toString(), navController)
            }
        }
    }
}

@Composable
fun SelectProblem(navController: NavHostController) {
    val context = LocalContext.current
    val problemTitleState = remember { mutableStateOf(emptyList<String>()) }
    val problemTitleRef = FirebaseDatabase.getInstance().getReference("problem")
    val day1Ref = problemTitleRef.child("day 1")

    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val day1List = mutableListOf<String>()
            for(childSnapShot in snapshot.children) {
                val day1Text = childSnapShot.key
                if(day1Text != null && !day1Text.contains(" " + context.getString(R.string.hint))) {
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
        .background(Color(0XFFD09AFF)),
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
fun ShowDetail(title: String, navController: NavHostController) {
    val context = LocalContext.current
    val problemDetailState = remember { mutableStateOf(emptyList<String>()) }
    val problemDetailRef = FirebaseDatabase.getInstance().getReference("problem")
    val detail = remember { mutableStateOf("") }
    val hintDetail = remember { mutableStateOf("") }
    val detailRef = problemDetailRef.child("day 1")
    var showDetail by remember { mutableStateOf(false) }

    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val problemDetail = mutableListOf<String>()
            val detailText = snapshot.child(title).value
            val detailHintText = snapshot.child(title + " " + context.getString(R.string.hint)).value
            detail.value = detailText.toString()
            hintDetail.value = detailHintText.toString()
            problemDetailState.value = problemDetail
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
    detailRef.addValueEventListener(valueEventListener)
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                contentDescription = stringResource(id = R.string.ic_back),
                modifier = Modifier
                    .clickable(interactionSource = MutableInteractionSource(), indication = null) { navController.popBackStack() }
                    .padding(horizontal = 10.dp)
            )
            Text(text = title)
        }
        Column(modifier = Modifier
            .padding(all = 12.dp)
            .fillMaxWidth()
            .background(Color(0XFFD09AFF))) {
            Text(
                text = stringResource(id = R.string.question_detail),
                style = MaterialTheme.typography.bodyMedium.copy(Color.Black),
                modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 10.dp)
            )
            Text(
                text = detail.value,
                style = MaterialTheme.typography.bodySmall.copy(Color.Black),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            )
        }
        Column(modifier = Modifier
            .padding(all = 12.dp)
            .height(100.dp)
            .fillMaxWidth()
            .clickable { showDetail = !showDetail }
            .background(if(showDetail) Color(0XFFD09AFF) else Color(0XFFD09AFF).copy(alpha = 0.5f))) {
            Text(
                text = stringResource(id = R.string.hint_detail),
                style = MaterialTheme.typography.bodyMedium.copy(Color.Black),
                modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 10.dp)
            )
            Text(
                text = if(showDetail) hintDetail.value else "",
                style = MaterialTheme.typography.bodySmall.copy(Color.Black),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            )
        }
    }
}