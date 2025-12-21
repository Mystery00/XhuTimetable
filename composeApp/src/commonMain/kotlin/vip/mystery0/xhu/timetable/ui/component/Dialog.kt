package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.utils.BaseValues
import com.maxkeppeler.sheets.state.StateDialog
import com.maxkeppeler.sheets.state.models.ProgressIndicator
import com.maxkeppeler.sheets.state.models.State
import com.maxkeppeler.sheets.state.models.StateConfig

fun xhuHeader(title: String): Header =
    Header.Custom {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BaseValues.CONTENT_DEFAULT_PADDING)
                .padding(top = 24.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            )
        }
    }

@Composable
fun ShowProgressDialog(
    useCaseState: UseCaseState,
    text: String,
    successText: String,
    errorText: String,
) {
    val state = when {
        successText.isNotBlank() -> State.Success(successText)
        errorText.isNotBlank() -> State.Failure(errorText)
        else -> State.Loading(text, ProgressIndicator.Circular())
    }
    StateDialog(
        state = useCaseState,
        config = StateConfig(state = state),
    )
}

@Composable
expect fun ShowUpdateDialog()