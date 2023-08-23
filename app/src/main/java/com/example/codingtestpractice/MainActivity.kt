package com.example.codingtestpractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
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
    val detailQuestion = remember { mutableStateOf("") }
    val detailHint = remember { mutableStateOf("") }
    NavHost(navController, startDestination = "select_problem") {
        composable("select_problem") {
            Column(Modifier.fillMaxSize()) {
                SelectProblem(navController)
            }
        }
        composable("show_detail/{title}", arguments = listOf(navArgument("title") { type = NavType.StringType})) {
            Column(Modifier.fillMaxSize()) {
                ShowDetail(it.arguments?.getString("title").toString(), navController, detailQuestion, detailHint)
            }
        }
    }
}

@Composable
fun SelectProblem(navController: NavHostController) {
    val problemTitleRef = FirebaseDatabase.getInstance().getReference("problem")
    val day1TitleState = remember { mutableStateOf(emptyList<String>()) }
    val day2TitleState = remember { mutableStateOf(emptyList<String>()) }
    val day1Ref = problemTitleRef.child("day 1")
    val day2Ref = problemTitleRef.child("day 2")

    val day1VEL = valueEventListener(day1TitleState)
    val day2VEL = valueEventListener(day2TitleState)
    day1Ref.addValueEventListener(day1VEL)
    day2Ref.addValueEventListener(day2VEL)

    Column {
        PrintDay(day1Ref.key.toString())
        day1TitleState.value.forEach { title ->
            ItemLayout(title) {
                navController.navigate("show_detail/$title")
            }
        }
        Divider()
        PrintDay(day2Ref.key.toString())
        day2TitleState.value.forEach { title ->
            ItemLayout(title) {
                navController.navigate(("show_detail/$title"))
            }
        }
    }
}

@Composable
fun valueEventListener(titleList: MutableState<List<String>>) : ValueEventListener {
    val context = LocalContext.current
    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val problemList = mutableListOf<String>()
            for(childSnapShot in snapshot.children) {
                val text = childSnapShot.key ?: ""
                if(!text.contains(" " + context.getString(R.string.hint))) {
                    problemList.add(text)
                }
            }
            titleList.value = problemList
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
    return valueEventListener
}

@Composable
fun PrintDay(day: String) {
    Text(
        text = "[ $day ]",
        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp)
    )
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
fun ShowDetail(title: String, navController: NavHostController, detail: MutableState<String>, hint: MutableState<String>) {
    val problemDetailRef = FirebaseDatabase.getInstance().getReference("problem")
    val day1Detail = remember { mutableStateOf("") }
    val day2Detail = remember { mutableStateOf("") }
    val day1HintDetail = remember { mutableStateOf("") }
    val day2HintDetail = remember { mutableStateOf("") }
    val day1Ref = problemDetailRef.child("day 1")
    val day2Ref = problemDetailRef.child("day 2")
    val showDetail = remember { mutableStateOf(false) }
    val day1VEL = detailValueEventListener(title, day1Detail, day1HintDetail)
    val day2VEL = detailValueEventListener(title, day2Detail, day2HintDetail)
    day1Ref.addValueEventListener(day1VEL)
    day2Ref.addValueEventListener(day2VEL)
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                contentDescription = stringResource(id = R.string.ic_back),
                modifier = Modifier
                    .clickable {
                        detail.value = ""
                        hint.value = ""
                        navController.popBackStack() }
                    .padding(horizontal = 10.dp)
            )
            Text(text = title)
        }
        DetailQuestionHint(showDetail, detail, hint)
/*        Column(modifier = Modifier
            .padding(all = 12.dp)
            .fillMaxWidth()
            .background(Color(0XFFD09AFF))) {
            Text(
                text = stringResource(id = R.string.question_detail),
                style = MaterialTheme.typography.bodyMedium.copy(Color.Black),
                modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 10.dp)
            )
            Text(
                text = day1Detail.value,
                style = MaterialTheme.typography.bodySmall.copy(Color.Black),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            )
        }
        Column(modifier = Modifier
            .padding(all = 12.dp)
            .height(100.dp)
            .fillMaxWidth()
            .clickable { showDetail = !showDetail }
            .background(if (showDetail) Color(0XFFD09AFF) else Color(0XFFD09AFF).copy(alpha = 0.5f))) {
            Text(
                text = stringResource(id = R.string.hint_detail),
                style = MaterialTheme.typography.bodyMedium.copy(Color.Black),
                modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 10.dp)
            )
            Text(
                text = if(showDetail) day1HintDetail.value else "",
                style = MaterialTheme.typography.bodySmall.copy(Color.Black),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            )
        }*/
    }
}

@Composable
fun detailValueEventListener(title: String, detail: MutableState<String>, hintDetail: MutableState<String>): ValueEventListener {
    val context = LocalContext.current
    println("detail in VEL ${detail.value}")
    println("hint in VEL ${hintDetail.value}")
    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val detailText = snapshot.child(title).value
            val detailHintText = snapshot.child(title + " " + context.getString(R.string.hint)).value
            detail.value = detailText.toString()
            hintDetail.value = detailHintText.toString()
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
    return valueEventListener
}

@Composable
fun DetailQuestionHint(showDetail: MutableState<Boolean>, detailQuestion: MutableState<String>, detailHint: MutableState<String>) {
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
            text = detailQuestion.value,
            style = MaterialTheme.typography.bodySmall.copy(Color.Black),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        )
    }
    Column(modifier = Modifier
        .padding(all = 12.dp)
        .height(100.dp)
        .fillMaxWidth()
        .clickable { showDetail.value = !showDetail.value }
        .background(if (showDetail.value) Color(0XFFD09AFF) else Color(0XFFD09AFF).copy(alpha = 0.5f))) {
        Text(
            text = stringResource(id = R.string.hint_detail),
            style = MaterialTheme.typography.bodyMedium.copy(Color.Black),
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 10.dp)
        )
        Text(
            text = if(showDetail.value) detailHint.value else "",
            style = MaterialTheme.typography.bodySmall.copy(Color.Black),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        )
    }
}