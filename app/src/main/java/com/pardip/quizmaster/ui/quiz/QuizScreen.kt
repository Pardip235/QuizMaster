package com.pardip.quizmaster.ui.quiz

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pardip.quizmaster.R
import com.pardip.quizmaster.data.remote.dto.KahootChoice
import com.pardip.quizmaster.ui.quiz.component.QuizCardScaffold
import com.pardip.quizmaster.ui.quiz.model.QuizUiState
import com.pardip.quizmaster.ui.quiz.model.RevealReason
import com.pardip.quizmaster.ui.quiz.model.UiOpenEnded
import com.pardip.quizmaster.ui.quiz.model.UiQuiz
import com.pardip.quizmaster.ui.quiz.model.UiSlider
import com.pardip.quizmaster.ui.theme.CorrectColor
import com.pardip.quizmaster.ui.theme.KahootBlue
import com.pardip.quizmaster.ui.theme.KahootGreen
import com.pardip.quizmaster.ui.theme.KahootRed
import com.pardip.quizmaster.ui.theme.KahootYellow
import com.pardip.quizmaster.ui.theme.NeutralColor
import com.pardip.quizmaster.ui.theme.WrongColor

@Composable
fun QuizScreen(
    viewModel: QuizViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuizScreen(
        state = state,
        onAnswerSelected = viewModel::selectAnswer,
        onRetry = { viewModel.load() },
        onContinue = viewModel::continueNext,
        typeOpenEnded = viewModel::typeOpenEnded,
        submitOpenEnded = viewModel::submitOpenEnded,
        setSlider = viewModel::setSlider,
        submitSlider = viewModel::submitSlider
    )
}

@Composable
private fun QuizScreen(
    modifier: Modifier = Modifier,
    state: QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    typeOpenEnded: (String) -> Unit,
    submitOpenEnded: () -> Unit,
    setSlider: (Int) -> Unit,
    submitSlider: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.quiz_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent // Avoid hiding bg with scaffold background
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding)
            ) {
                when {
                    state.loading -> Loading()
                    state.error != null -> Error(state.error, onRetry)
                    state.finished -> Finished()
                    else -> {
                        when (val quizItem = state.items.getOrNull(state.currentIndex)) {
                            is UiQuiz -> QuizQuestionCard(
                                quizItem = quizItem,
                                state = state,
                                onSelect = onAnswerSelected,
                                onContinue = onContinue
                            )

                            is UiOpenEnded -> OpenEndedCard(
                                quizItem = quizItem,
                                onType = typeOpenEnded,
                                onSubmit = submitOpenEnded,
                                onContinue = onContinue,
                                state = state,
                            )

                            is UiSlider -> SliderCard(
                                quizItem = quizItem,
                                state = state,
                                onSet = setSlider,
                                onSubmit = submitSlider,
                                onContinue = onContinue,
                            )

                            null -> {}
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizQuestionCard(
    quizItem: UiQuiz,
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onContinue: () -> Unit,
) {
    QuizCardScaffold(
        reveal = state.reveal,
        index = state.currentIndex,
        total = state.items.size,
        imageUrl = quizItem.imageUrl,
        questionText = quizItem.text,
        answersCount = quizItem.answers.size.coerceAtMost(4),
        answersContent = { cardHeight: Dp ->
            FlowRow(
                modifier = Modifier.fillMaxSize(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quizItem.answers.take(4).forEachIndexed { i, answerText ->
                    val isCorrect = i in quizItem.correctIndices
                    AnswerButton(
                        index = i,
                        choice = KahootChoice(answerText, isCorrect),
                        selectedIndex = state.selectedIndex,
                        showSolution = state.showSolution,
                        onClick = { onSelect(i) },
                        modifier = Modifier
                            .weight(1f)
                            .height(cardHeight)
                    )
                }
            }
        },
        supportingText = state.inlineMessage,
        showProgress = !state.showSolution,
        progress = state.progress,
        remainingSeconds = state.remainingSeconds,
        primaryActionLabel = if (state.showSolution) {
            stringResource(R.string.continueButtonText)
        } else {
            null
        },
        onPrimaryAction = if (state.showSolution) onContinue else null
    )
}


@Composable
fun OpenEndedCard(
    quizItem: UiOpenEnded,
    state: QuizUiState,
    onType: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit
) {
    val isWrong = state.showSolution && state.reveal == RevealReason.WRONG
    val isCorrect = state.showSolution && state.reveal == RevealReason.CORRECT
    val supporting = when {
        isCorrect -> stringResource(R.string.correctWithCheck)
        isWrong -> stringResource(
            id = R.string.wrongWithCross,
            quizItem.acceptedAnswers.joinToString()
        )

        else -> null
    }
    val buttonTextId = if (state.showSolution) {
        R.string.continueButtonText
    } else {
        R.string.submitButtonText
    }
    QuizCardScaffold(
        reveal = state.reveal,
        index = state.currentIndex,
        total = state.items.size,
        imageUrl = quizItem.imageUrl,
        questionText = quizItem.text,
        answersContent = {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val fieldHeight = maxHeight * 0.35f
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(fieldHeight),
                    verticalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = state.typedAnswer,
                        onValueChange = onType,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.placeholderTextField),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        enabled = !state.showSolution,
                        modifier = Modifier.fillMaxWidth(),
                        isError = isWrong,
                        supportingText = {
                            if (!supporting.isNullOrBlank()) {
                                Text(
                                    supporting,
                                    color = if (isCorrect) {
                                        CorrectColor
                                    } else {
                                        WrongColor
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        colors = if (isCorrect) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CorrectColor,
                                unfocusedBorderColor = CorrectColor,
                                cursorColor = CorrectColor
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        }
                    )
                }
            }
        },
        supportingText = state.inlineMessage, // we’re showing feedback in the field
        showProgress = !state.showSolution,
        progress = state.progress,
        remainingSeconds = state.remainingSeconds,
        primaryActionLabel = stringResource(buttonTextId),
        onPrimaryAction = if (state.showSolution) onContinue else onSubmit,
        answersCount = 2,
        modifier = Modifier
    )
}

@Composable
fun SliderCard(
    quizItem: UiSlider,
    state: QuizUiState,
    onSet: (Int) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit,
) {
    QuizCardScaffold(
        reveal = state.reveal,
        index = state.currentIndex,
        total = state.items.size,
        imageUrl = quizItem.imageUrl,
        questionText = quizItem.text,
        answersCount = 1, // only one slider block
        answersContent = { _: Dp ->
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val v = state.sliderValue ?: quizItem.start
                Text(
                    text = stringResource(R.string.your_answer, v),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = v.toFloat(),
                    onValueChange = {
                        val snapped = (((it - quizItem.start) / quizItem.step) * quizItem.step + quizItem.start)
                            .coerceIn(quizItem.start, quizItem.end).toInt()
                        onSet(snapped)
                    },
                    valueRange = quizItem.start.toFloat()..quizItem.end.toFloat(),
                    steps = ((quizItem.end - quizItem.start).toInt() / quizItem.step.toInt()) - 1,
                    enabled = !state.showSolution,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        supportingText = state.inlineMessage,
        showProgress = !state.showSolution,
        progress = state.progress,
        remainingSeconds = state.remainingSeconds,
        primaryActionLabel = stringResource(
            when {
                state.showSolution -> R.string.continueButtonText
                else -> R.string.submitButtonText
            }
        ),
        onPrimaryAction = when {
            state.showSolution -> onContinue
            else -> onSubmit
        }
    )
}


@Composable
private fun Loading() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.errorMessage, message),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(text = stringResource(R.string.retry)) }
    }
}

@Composable
private fun Finished() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.done),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.thankYou),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun AnswerButton(
    index: Int,
    choice: KahootChoice,
    selectedIndex: Int?,
    showSolution: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardHeight: Dp? = null
) {
    // Use colors from theme.kt
    val baseColors = listOf(KahootRed, KahootBlue, KahootYellow, KahootGreen)
    val baseColor = baseColors[index % baseColors.size] // cycle safely

    val isSelected = selectedIndex == index
    val isCorrect = choice.isCorrect
    val backgroundColor = when {
        showSolution && isCorrect -> CorrectColor
        showSolution && isSelected && !isCorrect -> WrongColor
        showSolution -> NeutralColor
        else -> baseColor
    }

    val shapeIcons = listOf(
        R.drawable.ic_triangle,
        R.drawable.ic_diamond,
        R.drawable.ic_circle,
        R.drawable.ic_square
    )
    val shapeRes = shapeIcons[index.coerceIn(0, shapeIcons.lastIndex)]
    val indicatorAtStart = index % 2 == 0
    val indicatorAlignment = if (indicatorAtStart) Alignment.TopStart else Alignment.TopEnd

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            onClick = { if (!showSolution && selectedIndex == null) onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (cardHeight != null) Modifier.height(cardHeight) else Modifier.heightIn(min = 64.dp))
        ) {
            Box(Modifier.fillMaxSize()) {
                if (!showSolution) {
                    Image(
                        painter = painterResource(shapeRes),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }

                Text(
                    text = choice.text,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    maxLines = Int.MAX_VALUE
                )
            }
        }

        if (showSolution) {
            val indicatorRes = when {
                isCorrect -> R.drawable.ic_correct
                isSelected && !isCorrect -> R.drawable.ic_wrong
                else -> null
            }
            if (indicatorRes != null) {
                Icon(
                    painter = painterResource(indicatorRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(indicatorAlignment)
                        .offset(
                            x = if (indicatorAtStart) (-6).dp else 6.dp,
                            y = (-6).dp
                        )
                        .size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Quiz - Multiple Choice")
@Composable
fun QuizUiPreview() {
    QuizScreen(
        state = QuizUiState(
            loading = false,
            title = "Sample Quiz",
            items = listOf(
                UiQuiz(
                    text = "What is the capital of France?",
                    imageUrl = "android.resource://com.pardip.quizmaster/drawable/preview_placeholder_image",
                    timeMs = 15000,
                    altText = "Eiffel Tower at sunset",
                    answers = listOf("Berlin", "Madrid", "Paris", "Rome"),
                    correctIndices = setOf(2),
                )
            ),
            currentIndex = 0,
            selectedIndex = 2,
            showSolution = true,
            reveal = RevealReason.CORRECT,
            progress = 0.4f,
            inlineMessage = "Well done!"
        ),
        onAnswerSelected = {},
        onRetry = {},
        onContinue = {},
        typeOpenEnded = {},
        submitOpenEnded = {},
        setSlider = {},
        submitSlider = {}
    )
}

@Preview(showBackground = true, name = "Quiz - Open Ended")
@Composable
fun OpenEndedUiPreview() {
    QuizScreen(
        state = QuizUiState(
            loading = false,
            title = "Open Ended",
            items = listOf(
                UiOpenEnded(
                    text = "The Colossus of Rhodes was based on which god?",
                    imageUrl = "android.resource://com.pardip.quizmaster/drawable/preview_placeholder_image",
                    timeMs = 20000,
                    altText = "Colossus of Rhodes engraving",
                    acceptedAnswers = listOf("Helios", "helios")
                )
            ),
            currentIndex = 0,
            typedAnswer = "Helios",
            showSolution = true,
            reveal = RevealReason.CORRECT,
            inlineMessage = null,
            progress = 0.8f
        ),
        onAnswerSelected = {},
        onRetry = {},
        onContinue = {},
        typeOpenEnded = {},
        submitOpenEnded = {},
        setSlider = {},
        submitSlider = {}
    )
}

@Preview(showBackground = true, name = "Quiz - Slider")
@Composable
fun SliderUiPreview() {
    QuizScreen(
        state = QuizUiState(
            loading = false,
            title = "Slider",
            items = listOf(
                UiSlider(
                    text = "How many of the Seven Wonders still exist?",
                    imageUrl = "android.resource://com.pardip.quizmaster/drawable/preview_placeholder_image",
                    timeMs = 20000,
                    altText = "Seven Wonders illustration",
                    start = 0.0,
                    end = 7.0,
                    step = 1.0,
                    correct = 1.0,
                    tolerance = 0.0
                )
            ),
            currentIndex = 0,
            sliderValue = 3,
            showSolution = true,
            reveal = RevealReason.WRONG,
            progress = 0.5f
        ),
        onAnswerSelected = {},
        onRetry = {},
        onContinue = {},
        typeOpenEnded = {},
        submitOpenEnded = {},
        setSlider = {},
        submitSlider = {}
    )
}