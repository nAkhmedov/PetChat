package uz.petchat.feature.conversation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uz.petchat.core.ui.theme.PetChatTheme
import uz.petchat.feature.conversation.R
import uz.petchat.core.ui.R as CoreUiR

private val InputShape = RoundedCornerShape(22.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MessageInput(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textState = rememberTextFieldState()
    val canSend by remember { derivedStateOf { textState.text.isNotBlank() } }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            BasicTextField(
                state = textState,
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
                decorator = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (textState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.conversation_input_placeholder),
                                style = textStyle,
                                color = placeholderColor,
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .background(PetChatTheme.colors.inputBackground, InputShape)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                enabled = canSend,
                onClick = {
                    val text = textState.text.toString()
                    textState.clearText()
                    onSend(text)
                },
            ) {
                Icon(
                    painter = painterResource(CoreUiR.drawable.ic_send),
                    contentDescription = stringResource(R.string.conversation_send),
                    tint = if (canSend) MaterialTheme.colorScheme.primary else placeholderColor,
                )
            }
        }
    }
}
