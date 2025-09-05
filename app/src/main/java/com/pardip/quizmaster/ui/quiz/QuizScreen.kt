package com.pardip.quizmaster.ui.quiz

import android.os.SystemClock
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pardip.quizmaster.R
import com.pardip.quizmaster.core.net.ErrorType
import com.pardip.quizmaster.domain.model.Choice
import com.pardip.quizmaster.domain.model.MultipleChoice
import com.pardip.quizmaster.domain.model.OpenEnded
import com.pardip.quizmaster.domain.model.Question
import com.pardip.quizmaster.domain.model.Slider
import com.pardip.quizmaster.ui.quiz.component.ProgressStripe
import com.pardip.quizmaster.ui.quiz.component.QuestionContent
import com.pardip.quizmaster.ui.quiz.component.QuizTopBar
import com.pardip.quizmaster.ui.quiz.component.SimpleRoundedButton
import com.pardip.quizmaster.ui.quiz.component.SupportingText
import com.pardip.quizmaster.ui.quiz.util.clamp
import com.pardip.quizmaster.ui.quiz.util.sliderSteps
import com.pardip.quizmaster.ui.quiz.util.snapToStep
import com.pardip.quizmaster.ui.theme.CorrectColor
import com.pardip.quizmaster.ui.theme.KahootBlue
import com.pardip.quizmaster.ui.theme.KahootGreen
import com.pardip.quizmaster.ui.theme.KahootRed
import com.pardip.quizmaster.ui.theme.KahootYellow
import com.pardip.quizmaster.ui.theme.NeutralColor
import com.pardip.quizmaster.ui.theme.WrongColor
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun QuizScreen() {
    val viewModel: QuizViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuizScreen(
        state = state,
        onAnswerSelected = viewModel::selectAnswer,
        onRetry = { viewModel.load() },
        onContinue = viewModel::continueNext,
        onSubmitOpenEnded = viewModel::submitOpenEnded,
        onSubmitSlider = viewModel::submitSlider,
        onTimeUp = viewModel::onTimeUp
    )
}

@Composable
private fun QuizScreen(
    state: QuizScreenState,
    onAnswerSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    onSubmitOpenEnded: (String) -> Unit,
    onSubmitSlider: (Double) -> Unit,
    onTimeUp: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.quiz_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Scaffold(containerColor = Color.Transparent, modifier = Modifier.fillMaxSize()) { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                when (state) {
                    is QuizScreenState.Loading -> Loading()
                    is QuizScreenState.Error -> Error(state.error, onRetry)
                    is QuizScreenState.Content -> {
                        Content(
                            contentState = state,
                            onSelect = onAnswerSelected,
                            onContinue = onContinue,
                            onSubmitOpenEnded = onSubmitOpenEnded,
                            onSubmitSlider = onSubmitSlider,
                            onTimeUp = onTimeUp
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun Content(
    contentState: QuizScreenState.Content,
    onSelect: (Int) -> Unit,
    onContinue: () -> Unit,
    onSubmitOpenEnded: (String) -> Unit,
    onSubmitSlider: (Double) -> Unit,
    onTimeUp: () -> Unit
) {
    // End screen
    if (contentState.currentQuestionState is QuizScreenState.Content.QuestionState.End) {
        Finished()
        return
    }

    val q = contentState.question ?: return
    val qs = contentState.currentQuestionState

    var typed by rememberSaveable(contentState.currentIndex) { mutableStateOf("") }
    var slider by rememberSaveable(contentState.currentIndex) {
        val s = (q as? Slider)
        mutableDoubleStateOf(
            s?.let {
                val minV = minOf(it.start, it.end)
                val maxV = maxOf(it.start, it.end)
                val seed = it.start.coerceIn(minV, maxV)
                snapToStep(seed, it.start, it.step)
            } ?: 0.0
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top
        QuizTopBar(contentState)

        // Middle (question + answers)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (q) {
                is MultipleChoice -> MultipleQuestionCard(
                    quizItem = q,
                    questionState = qs,
                    onSelect = onSelect,
                    modifier = Modifier.fillMaxSize()
                )

                is OpenEnded -> OpenEndedQuestionCard(
                    quizItem = q,
                    shouldShowSolution = qs.showSolution(),
                    text = typed,
                    onType = { typed = it },
                    modifier = Modifier.fillMaxSize()
                )

                is Slider -> SliderQuestionCard(
                    quizItem = q,
                    shouldShowSolution = qs.showSolution(),
                    value = slider,
                    onSet = { slider = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Progress bar + banner text + button text
        val (progress, remainingSecs) = rememberQuestionTimer(
            questionKey = contentState.currentIndex,
            duration = q.duration,
            running = !qs.showSolution(),
            onFinished = onTimeUp
        )
        BottomContents(
            q = q,
            qs = qs,
            isRunning = !qs.showSolution(),
            progress = progress,
            remainingSecs = remainingSecs,
            showSolution = qs.showSolution(),
            onContinue = onContinue,
            onSubmitOpenEnded = onSubmitOpenEnded,
            typed = typed,
            onSubmitSlider = onSubmitSlider,
            slider = slider
        )
    }
}

@Composable
private fun BottomContents(
    q: Question,
    qs: QuizScreenState.Content.QuestionState,
    isRunning: Boolean,
    progress: Float,
    remainingSecs: Int?,
    showSolution: Boolean,
    onContinue: () -> Unit,
    onSubmitOpenEnded: (String) -> Unit,
    typed: String,
    onSubmitSlider: (Double) -> Unit,
    slider: Double
) {
    Spacer(Modifier.height(8.dp))

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val banner = bottomBannerFor(q, qs)

        if (isRunning) {
            ProgressStripe(
                progress = progress, remainingSeconds = remainingSecs, height = 24.dp
            )
            Spacer(Modifier.height(8.dp))
        }

        if (!banner.isNullOrBlank()) {
            SupportingText(banner)
            Spacer(Modifier.height(8.dp))
        }

        val (label, action) = when (q) {
            is MultipleChoice -> if (showSolution) R.string.continueButtonText to onContinue
            else null to null

            is OpenEnded -> if (showSolution) R.string.continueButtonText to onContinue
            else R.string.submitButtonText to { onSubmitOpenEnded(typed) }

            is Slider -> if (showSolution) R.string.continueButtonText to onContinue
            else R.string.submitButtonText to { onSubmitSlider(slider) }
        }

        if (label != null && action != null) {
            SimpleRoundedButton(
                label = stringResource(label),
                onClick = action,
                modifier = Modifier
                    .height(48.dp)
                    .width(200.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultipleQuestionCard(
    quizItem: MultipleChoice,
    questionState: QuizScreenState.Content.QuestionState,
    onSelect: (Int) -> Unit,
    modifier: Modifier
) {
    val selected =
        (questionState as? QuizScreenState.Content.QuestionState.Answered)?.selectedIndex()

    QuestionContent(
        modifier = modifier,
        question = quizItem,
        answersCount = quizItem.choices.size.coerceAtMost(4),
        answersContent = { cardHeight ->
            FlowRow(
                modifier = Modifier.fillMaxSize(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quizItem.choices.take(4).forEachIndexed { i, choice ->
                    AnswerButton(
                        index = i,
                        choice = choice,
                        selectedIndex = selected,
                        showSolution = questionState.showSolution(),
                        onClick = { onSelect(i) },
                        modifier = Modifier
                            .weight(1f)
                            .height(cardHeight)
                    )
                }
            }
        }
    )
}


@Composable
fun OpenEndedQuestionCard(
    quizItem: OpenEnded,
    shouldShowSolution: Boolean,
    text: String,
    onType: (String) -> Unit,
    modifier: Modifier
) {
    QuestionContent(
        modifier = modifier,
        question = quizItem,
        answersCount = 2,
        answersContent = {
            Column(Modifier.fillMaxSize()) {
                // input field
                BoxWithConstraints(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                ) {
                    val minFieldHeight = max(56.dp, maxHeight * 0.35f)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .heightIn(min = minFieldHeight),
                        verticalArrangement = Arrangement.Top
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = onType,
                            textStyle = MaterialTheme.typography.bodySmall,
                            placeholder = {
                                Text(stringResource(R.string.placeholderTextField), maxLines = 1)
                            },
                            singleLine = true,
                            enabled = !shouldShowSolution,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            isError = false
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun SliderQuestionCard(
    quizItem: Slider,
    shouldShowSolution: Boolean,
    value: Double,
    onSet: (Double) -> Unit,
    modifier: Modifier
) {

    val (minV, maxV) = remember(quizItem) {
        if (quizItem.start <= quizItem.end) {
            quizItem.start to quizItem.end
        } else {
            quizItem.end to quizItem.start
        }
    }
    val steps = remember(quizItem) { sliderSteps(quizItem.start, quizItem.end, quizItem.step) }

    QuestionContent(
        modifier = modifier,
        question = quizItem,
        answersCount = 1,
        answersContent = {
            Column(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.your_answer, value.toInt()),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = value.toFloat(),
                        onValueChange = {
                            val raw = it.toDouble()
                            val clamped = clamp(raw, quizItem.start, quizItem.end)
                            val snapped = snapToStep(clamped, quizItem.start, quizItem.step)
                            onSet(snapped)
                        },
                        valueRange = minV.toFloat()..maxV.toFloat(),
                        steps = steps,
                        enabled = !shouldShowSolution,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Composable
private fun Loading() {
    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(type: ErrorType, onRetry: () -> Unit) {
    val message = when (type) {
        is ErrorType.Http -> stringResource(R.string.error_http, type.code)
        is ErrorType.Offline -> stringResource(R.string.error_offline)
        is ErrorType.Timeout -> stringResource(R.string.error_timeout)
        is ErrorType.Network -> stringResource(R.string.error_network)
        is ErrorType.Parse -> stringResource(R.string.error_parse)
        is ErrorType.Unexpected -> stringResource(R.string.error_unknown)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, textAlign = TextAlign.Center)
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
            text = stringResource(R.string.thankYou), fontSize = 16.sp, textAlign = TextAlign.Center
        )
    }
}


@Composable
fun AnswerButton(
    index: Int,
    choice: Choice,
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
        showSolution && isSelected && !isCorrect -> WrongColor
        showSolution && isCorrect -> CorrectColor
        showSolution -> NeutralColor
        else -> baseColor
    }

    val shapeIcons = listOf(
        R.drawable.ic_triangle, R.drawable.ic_diamond, R.drawable.ic_circle, R.drawable.ic_square
    )
    val shapeRes = shapeIcons[index.coerceIn(0, shapeIcons.lastIndex)]
    val indicatorAtStart = index % 2 == 0
    val indicatorAlignment = if (indicatorAtStart) Alignment.TopStart else Alignment.TopEnd

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            onClick = { if (!showSolution && selectedIndex == null) onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor, contentColor = Color.White
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
                            x = if (indicatorAtStart) (-6).dp else 6.dp, y = (-6).dp
                        )
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun bottomBannerFor(
    question: Question, state: QuizScreenState.Content.QuestionState
): String? = when (question) {
    is OpenEnded -> when (state) {
        is QuizScreenState.Content.QuestionState.Answered.Correct -> stringResource(R.string.correctWithCheck)

        is QuizScreenState.Content.QuestionState.Answered.Wrong ->
            stringResource(R.string.wrongWithCross, question.acceptedAnswers.joinToString())

        is QuizScreenState.Content.QuestionState.TimesUp -> stringResource(R.string.time_up)

        else -> null
    }

    is Slider -> when (state) {
        is QuizScreenState.Content.QuestionState.Answered.Correct -> stringResource(R.string.correctWithCheck)

        is QuizScreenState.Content.QuestionState.Answered.Wrong -> {
            val correct = question.correct.toInt()
            stringResource(R.string.wrongWithCross, correct)
        }

        is QuizScreenState.Content.QuestionState.TimesUp -> stringResource(R.string.time_up)

        else -> null
    }

    is MultipleChoice -> when (state) {
        is QuizScreenState.Content.QuestionState.TimesUp -> stringResource(R.string.time_up)

        else -> null
    }
}

/**
 * UI-owned countdown tied to a question key (e.g., index).
 * Survives recompositions and handles background/resume correctly.
 *
 * @return Pair(progress[0f..1f], remainingSeconds or null when not running)
 */
@Composable
private fun rememberQuestionTimer(
    questionKey: Any,
    duration: Duration,
    running: Boolean,
    onFinished: () -> Unit,
    nowMs: () -> Long = { SystemClock.elapsedRealtime() },
    tickMs: Long = 100L
): Pair<Float, Int?> {
    // Anchor start time per question
    val startAt = rememberSaveable(questionKey) { nowMs() }
    val deadline = remember(questionKey, duration) { startAt + duration.inWholeMilliseconds }

    var remainingMs by remember(questionKey) { mutableLongStateOf(duration.inWholeMilliseconds) }

    LaunchedEffect(questionKey, running, deadline) {
        if (!running) return@LaunchedEffect
        while (true) {
            val rem = (deadline - nowMs()).coerceAtLeast(minimumValue = 0)
            if (rem != remainingMs) remainingMs = rem
            if (rem == 0L) {
                onFinished()
                break
            }
            delay(tickMs)
        }
    }

    val denom = duration.inWholeMilliseconds.coerceAtLeast(1)
    val progress = (remainingMs.toFloat() / denom).coerceIn(0f, 1f)
    val secs = ceil(remainingMs / 1000.0).toInt().takeIf { running }
    return progress to secs
}

@Preview(showBackground = true, name = "MultipleChoice – Correct (Continue shown)")
@Composable
fun PreviewQuizCorrect() {
    QuizScreen(
        state = QuizScreenState.Content(
            currentIndex = 0,
            totalQuestions = 1,
            question = previewMultipleChoice,
            currentQuestionState = QuizScreenState.Content.QuestionState.Answered.Correct(
                selectedIndex = 2
            )
        ),
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {})
}

@Preview(showBackground = true, name = "Open Ended – Wrong (shows correct)")
@Composable
fun PreviewOpenEndedWrong() {
    QuizScreen(
        state = QuizScreenState.Content(
            currentIndex = 0, totalQuestions = 1, question = previewOpen,
            // We’ve already judged it wrong; bottom banner will show accepted answers
            currentQuestionState = QuizScreenState.Content.QuestionState.Answered.Wrong(
                selectedIndex = -1, correctIndex = -1
            )
        ),
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {})
}

@Preview(showBackground = true, name = "Open Ended – Correct")
@Composable
fun PreviewOpenEndedCorrect() {
    QuizScreen(
        state = QuizScreenState.Content(
            currentIndex = 0,
            totalQuestions = 1,
            question = previewOpen,
            currentQuestionState = QuizScreenState.Content.QuestionState.Answered.Correct(
                selectedIndex = -1
            )
        ),
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {}

    )
}

@Preview(showBackground = true, name = "Slider – Wrong (shows correct value)")
@Composable
fun PreviewSliderWrong() {
    QuizScreen(
        state = QuizScreenState.Content(
            currentIndex = 0,
            totalQuestions = 1,
            question = previewSlider,
            currentQuestionState = QuizScreenState.Content.QuestionState.Answered.Wrong(
                selectedIndex = -1, correctIndex = -1
            )
        ),
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {})
}

@Preview(showBackground = true, name = "Slider – Correct")
@Composable
fun PreviewSliderCorrect() {
    QuizScreen(
        state = QuizScreenState.Content(
            currentIndex = 0,
            totalQuestions = 1,
            question = previewSlider,
            currentQuestionState = QuizScreenState.Content.QuestionState.Answered.Correct(
                selectedIndex = -1
            )
        ),
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {})
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun PreviewLoading() {
    QuizScreen(
        state = QuizScreenState.Loading,
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {})
}

@Preview(showBackground = true, name = "Error – Network")
@Composable
fun PreviewError() {
    QuizScreen(
        state = QuizScreenState.Error(error = ErrorType.Network),
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {})
}

@Preview(showBackground = true, name = "Finished")
@Composable
fun PreviewFinished() {
    QuizScreen(
        state = QuizScreenState.Content(
            currentIndex = 0,
            totalQuestions = 1,
            question = null,
            currentQuestionState = QuizScreenState.Content.QuestionState.End
        ),
        onContinue = {},
        onAnswerSelected = {},
        onRetry = {},
        onSubmitOpenEnded = {},
        onSubmitSlider = {},
        onTimeUp = {})
}

private val previewMultipleChoice = MultipleChoice(
    text = "What is the capital of France?",
    imageUrl = "android.resource://com.pardip.quizmaster/drawable/preview_placeholder_image",
    duration = 15.seconds,
    altText = "Eiffel Tower at sunset",
    choices = listOf(
        Choice(text = "Berlin", isCorrect = false),
        Choice(text = "Madrid", isCorrect = false),
        Choice(text = "Paris", isCorrect = true),
        Choice(text = "Rome", isCorrect = false),
    )
)

private val previewOpen = OpenEnded(
    text = "The Colossus of Rhodes was based on which god?",
    imageUrl = "android.resource://com.pardip.quizmaster/drawable/preview_placeholder_image",
    duration = 20.seconds,
    altText = "Colossus of Rhodes engraving",
    acceptedAnswers = listOf("Helios", "helios")
)

private val previewSlider = Slider(
    text = "How many of the Seven Wonders still exist?",
    imageUrl = "android.resource://com.pardip.quizmaster/drawable/preview_placeholder_image",
    duration = 20.seconds,
    altText = "Seven Wonders illustration",
    start = 0.0,
    end = 7.0,
    step = 1.0,
    correct = 1.0,
    tolerance = 0.0
)