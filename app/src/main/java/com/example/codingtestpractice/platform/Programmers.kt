package com.example.codingtestpractice.platform

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.codingtestpractice.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ProgrammersScreen(homeNavController: NavHostController) {
    val programmersNavController = rememberNavController()
    val detailQuestion = remember { mutableStateOf("") }
    val detailHint = remember { mutableStateOf("") }
    NavHost(programmersNavController, startDestination = "problemList") {
        composable("problemList") {
            Column(Modifier.padding(all = 10.dp)) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(id = R.string.ic_back),
                    modifier = Modifier.clickable { homeNavController.popBackStack() }
                )
                ProblemList(programmersNavController)
            }
        }
        composable("showDetail/{title}/{day}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType},
                navArgument("day") { type = NavType.StringType}
            )) {
            val title = it.arguments?.getString("title") ?: ""
            val day = it.arguments?.getString("day") ?: ""
            ShowDetail(day, title, programmersNavController, detailQuestion, detailHint)
        }
    }
}

@Composable
fun ProblemList(programmersNavController: NavHostController) {
    val programmersRef = FirebaseDatabase.getInstance().getReference("programmers")
    val dayTitleStates = List(18) { remember { mutableStateOf(emptyList<String>()) }}
    val dayRefs = List(18) { programmersRef.child("day ${it+1}")}
    val dayVELs = List(18) { valueEventListener(dayTitleStates[it])}
    dayRefs.forEachIndexed{ index, dayRef ->
        dayRef.addValueEventListener(dayVELs[index])
    }
    Column(Modifier.verticalScroll(rememberScrollState())) {
        repeat(18) { dayIndex ->
            val dayRef = dayRefs[dayIndex]
            val dayTitleState = dayTitleStates[dayIndex]
            PrintDay(dayRef.key.toString())
            dayTitleState.value.forEach { title ->
                EachProblem(title) {
                    programmersNavController.navigate("showDetail/$title/${dayRef.key}")
                }
            }
        }
    }
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
fun EachProblem(title: String, select: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .border(1.dp, Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(all = 10.dp)
                .weight(1f)
        )
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
            contentDescription = stringResource(id = R.string.ic_arrow_right),
            modifier = Modifier.clickable { select() }
        )
    }
}

@Composable
fun ShowDetail(day: String, title: String, navController: NavHostController, detail: MutableState<String>, hint: MutableState<String>) {
    val problemDetailRef = FirebaseDatabase.getInstance().getReference("problem")
    val dayRef = problemDetailRef.child(day)
    val dayVEL = detailValueEventListener(title, detail, hint)
    val showDetail = remember { mutableStateOf(false) }
    dayRef.addValueEventListener(dayVEL)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                contentDescription = stringResource(id = R.string.ic_back),
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .padding(horizontal = 10.dp)
            )
            Text(text = title)
        }
        DetailQuestionHint(showDetail, detail, hint)
    }
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

@Composable
fun valueEventListener(titleList: MutableState<List<String>>) : ValueEventListener {
    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val problemList = mutableListOf<String>()
            for(childSnapShot in snapshot.children) {
                val text = childSnapShot.key ?: ""
                problemList.add(text)
            }
            titleList.value = problemList
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
    return valueEventListener
}

@Composable
fun detailValueEventListener(title: String, detail: MutableState<String>, hintDetail: MutableState<String>): ValueEventListener {
    val context = LocalContext.current
    val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val detailText = snapshot.child(title).child(context.getString(R.string.question_detail)).value
            println("detail $detailText")
            val detailHintText = snapshot.child(title).child(context.getString(R.string.hint_detail)).value
            detail.value = detailText.toString()
            hintDetail.value = detailHintText.toString()
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
    return valueEventListener
}