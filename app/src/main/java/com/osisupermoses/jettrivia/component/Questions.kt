package com.osisupermoses.jettrivia.component

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.osisupermoses.jettrivia.model.QuestionItem
import com.osisupermoses.jettrivia.screens.QuestionViewModel
import com.osisupermoses.jettrivia.util.AppColors
import kotlinx.coroutines.launch
import java.lang.Exception

@Composable
fun Questions(viewModel: QuestionViewModel) {
    /** Important note: since, Question class returns an ...*/
    val questions = viewModel.data.value.data?.toMutableList() //Important!

    val questionIndex = remember {
        mutableStateOf(0)
    }

    val correctAnswerList = remember {
        mutableListOf("")
    }

    if (viewModel.data.value.loading == true) {
        Surface(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }

//        Log.d("Loading", "Questions: ...Loading...")
    } else {
        val question = try {
            questions?.get(questionIndex.value)
        } catch (ex: Exception) {
            null
        }

        val correctAnswerState = remember(question) {
            mutableStateOf<Boolean?>(null)
        }

        val isSelected = remember {
            mutableStateOf(true)
        }

        if (questions != null ) {
            QuestionDisplay(
                question = question!!,
                questionIndex = questionIndex,
                viewModel = viewModel,
                correctAnswerState = correctAnswerState,
                correctAnswerList = correctAnswerList,
                isSelected = isSelected) {

                questionIndex.value = questionIndex.value + 1
                if (correctAnswerState.value == true ) {
                    correctAnswerList.add(question.answer)
                }
                isSelected.value = true
            }

//            Log.d("CORRECT ANSWER", "Correct Answer: ${correctAnswerList.size - 1}")
        
        
//        Log.d("Loading", "Questions: Loading STOPPED...")
//        questions?.forEach { questionItem ->
//            Log.d("Result", "Questions: ${questionItem.question}")
        }
    }
//    Log.d("SIZE", "Questions: ${questions?.size}")
}

//@Preview
@Composable
fun QuestionDisplay(
    question: QuestionItem,
    questionIndex: MutableState<Int>,
    correctAnswerState: MutableState<Boolean?>,
    correctAnswerList: MutableList<String>,
    viewModel: QuestionViewModel,
    isSelected: MutableState<Boolean>,
    onNextClicked: (Int) -> Unit
) {

    val choicesState = remember(question) {
        question.choices.toMutableList()

    }
    val answerState = remember(question) {
        mutableStateOf<Int?>(null)
    }

    val updateAnswer: (Int) -> Unit = remember(question) {
        {
            answerState.value = it
            correctAnswerState.value = choicesState[it] == question.answer
        }
    }

    val pathEffect = PathEffect.dashPathEffect((floatArrayOf(10f, 10f)),0f)

    Surface(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
        color = AppColors.mDarkPurple) {

        Column(modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start) {


            if (questionIndex.value >= 3)
                ShowProgress(score = correctAnswerList.size - 1, questionIndex, viewModel)

            QuestionTracker(counter = questionIndex.value + 1,
                outOf = viewModel.getTotalQuestionCount()
            )

            DrawDottedLineHorizontally(pathEffect)

            Column {
                Text(text = question.question,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(alignment = Alignment.Start)
                        .fillMaxHeight(0.3f),
                    fontSize = 17.sp,
                    color = AppColors.mOffWhite,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp)

                //choices
                choicesState.forEachIndexed { index, answerText ->
                    Row(modifier = Modifier
                        .padding(3.dp)
                        .fillMaxWidth()
                        .height(45.dp)
                        .border(
                            width = 4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AppColors.mOffDarkPurple,
                                    AppColors.mOffDarkPurple
                                )
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clip(
                            RoundedCornerShape(
                                topStartPercent = 50,
                                topEndPercent = 50,
                                bottomEndPercent = 50,
                                bottomStartPercent = 50
                            )
                        )
                        .background(Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically) {

                        if (isSelected.value) {
                            RadioButton(
                                enabled = true,
                                selected = (answerState.value == index),
                                onClick = { updateAnswer(index)
                                    isSelected.value = false
                                },
                                modifier = Modifier.padding(start = 16.dp),
                                colors =
                                RadioButtonDefaults.colors(
                                    selectedColor =
                                    if (correctAnswerState.value == true
                                        && answerState.value == index)
                                        Color.Green.copy(alpha = 0.2f)
                                    else Color.Red.copy(alpha = 0.2f)
                                )
                            )
                        } else {
                            RadioButton(
                                enabled = false,
                                selected = (answerState.value == index),
                                onClick = { updateAnswer(index) },
                                modifier = Modifier.padding(start = 16.dp),
                                colors =
                                RadioButtonDefaults.colors(
                                    selectedColor =
                                    if (correctAnswerState.value == true
                                        && answerState.value == index
                                    ) Color.Green.copy(alpha = 0.2f)
                                    else Color.Red.copy(alpha = 0.2f)
                                )
                            ) //rb ends
                        }

                        val annotatedString = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Light,
                                                        color = if (correctAnswerState.value == true
                                                            && answerState.value == index) {
                                                            Color.Green
                                                        } else if (correctAnswerState.value == false
                                                            && answerState.value == index) {
                                                            Color.Red
                                                        } else {AppColors.mOffWhite},
                                                        fontSize = 17.sp)) {
                                append(answerText)
                            }
                        }
                        Text(text = annotatedString, modifier = Modifier.padding(6.dp))
                    }
                }

                Button(onClick = { onNextClicked(questionIndex.value) },
                    modifier = Modifier
                        .padding(3.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(34.dp),
                    colors = buttonColors(
                            backgroundColor = AppColors.mLightBlue)) {

                    if (questionIndex.value == viewModel.getTotalQuestionCount() - 1 ) {
                        Text(text = "FINISHED",
                            modifier = Modifier.padding(4.dp),
                            color = AppColors.mOffWhite,
                            fontSize = 17.sp)

                        SnackbarScreen()

                    } else {
                        Text(text = "Next",
                            modifier = Modifier.padding(4.dp),
                            color = AppColors.mOffWhite,
                            fontSize = 17.sp)
                    }
                }
            }
        }

    }
}

@Composable
fun SnackbarScreen() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
        FloatingActionButton(
            onClick = {
                //Important part here
                scope.launch {
                    snackbarHostState.showSnackbar("You have completed the exercise!")
                }
                //
            },
            content = { Icon(imageVector = Icons.Default.Add,
                contentDescription = "Finished Snackbar") }
        )

    SnackbarHost(hostState = snackbarHostState)
}

//@Preview
@Composable
fun ShowProgress(score: Int = 12, questionIndex: MutableState<Int>, viewModel: QuestionViewModel) {

    val gradient = Brush.linearGradient(listOf(Color(0xfff95075), Color(0xffbe6be5)))

    val progressFactor = remember {
        mutableStateOf((questionIndex.value + 1).toFloat() /
                viewModel.getTotalQuestionCount().toFloat() )
    }

    Row(modifier = Modifier
        .padding(3.dp)
        .height(45.dp)
        .border(
            width = 4.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    AppColors.mLightPurple, AppColors.mLightPurple
                )
            ),
            shape = RoundedCornerShape(34.dp)
        )
        .clip(
            RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 50,
                bottomStartPercent = 50,
                bottomEndPercent = 50
            )
        )
        .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically) {

        Row(Modifier.fillMaxWidth(0.8f)) {

            Button(
                contentPadding = PaddingValues(1.dp),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth(progressFactor.value)
                    .background(brush = gradient),
                enabled = false,
                elevation = null,
                colors = buttonColors(
                    backgroundColor = Color.Transparent,
                    disabledBackgroundColor = Color.Transparent)) {
            }
        }

        Row {

            Button(
                contentPadding = PaddingValues(1.dp),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = gradient),
                enabled = false,
                elevation = null,
                colors = buttonColors(
                    backgroundColor = AppColors.mOffDarkPurple,
                    disabledBackgroundColor = AppColors.mOffDarkPurple)) {
                Text(
                    text = score.toString(),
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(23.dp))
                        .fillMaxWidth()
                        .fillMaxHeight(0.87f)
                        .padding(6.dp),
                    color = AppColors.mOffWhite,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun DrawDottedLineHorizontally(pathEffect: PathEffect) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {
        drawLine(color = Color.LightGray,
            start = Offset(0f, 0f),
            end = Offset(size.width,0f),
            pathEffect = pathEffect)
    }
}

@Preview
@Composable
fun QuestionTracker(counter: Int = 10,
                    outOf: Int = 100) {
    Text(text = buildAnnotatedString {
        withStyle(style = ParagraphStyle(textIndent = TextIndent.None)) {
            withStyle(style = SpanStyle(color = AppColors.mLightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 27.sp)) {
                append("Question $counter/")
                withStyle(style = SpanStyle(color = AppColors.mLightGray,
                                            fontWeight = FontWeight.Light,
                                            fontSize = 14.sp)){
                    append("$outOf")
                }

            }
        }
    },
        modifier = Modifier.padding(20.dp))
}