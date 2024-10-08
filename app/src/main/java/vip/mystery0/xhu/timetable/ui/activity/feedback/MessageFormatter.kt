package vip.mystery0.xhu.timetable.ui.activity.feedback

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

// Regex containing the syntax tokens
val symbolPattern by lazy {
    Regex("""(https?://[^\s\t\n]+)|(`[^`]+`)|(@\w+)|(\*[\w]+\*)|(_[\w]+_)|(~[\w]+~)""")
}

typealias StringAnnotation = AnnotatedString.Range<String>
// Pair returning styled content and annotation for ClickableText when matching syntax token
typealias SymbolAnnotation = Pair<AnnotatedString, StringAnnotation?>

/**
 * Format a message following Markdown-lite syntax
 * | @username -> bold, primary color and clickable element
 * | http(s)://... -> clickable link, opening it into the browser
 * | *bold* -> bold
 * | _italic_ -> italic
 * | ~strikethrough~ -> strikethrough
 * | `MyClass.myMethod` -> inline code styling
 *
 * @param text contains message to be parsed
 * @return AnnotatedString with annotations used inside the ClickableText wrapper
 */
@Composable
fun messageFormatter(
    text: String,
    primary: Boolean
): AnnotatedString {
    val tokens = symbolPattern.findAll(text)

    return buildAnnotatedString {
        var cursorPosition = 0

        val codeSnippetBackground =
            if (primary) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }

        for (token in tokens) {
            //普通文本
            append(text.slice(cursorPosition until token.range.first))

            getSymbolAnnotation(
                matchResult = token,
                colors = MaterialTheme.colorScheme,
                primary = primary,
                codeSnippetBackground = codeSnippetBackground
            )

            cursorPosition = token.range.last + 1
        }

        if (!tokens.none()) {
            append(text.slice(cursorPosition..text.lastIndex))
        } else {
            append(text)
        }
    }
}

/**
 * Map regex matches found in a message with supported syntax symbols
 *
 * @param matchResult is a regex result matching our syntax symbols
 * @return pair of AnnotatedString with annotation (optional) used inside the ClickableText wrapper
 */
private fun AnnotatedString.Builder.getSymbolAnnotation(
    matchResult: MatchResult,
    colors: ColorScheme,
    primary: Boolean,
    codeSnippetBackground: Color
) {
    when (matchResult.value.first()) {
        '@' -> {
            withStyle(
                style = SpanStyle(
                    color = if (primary) colors.secondary else colors.primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(matchResult.value)
            }
        }

        '*' -> {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(matchResult.value.trim('*'))
            }
        }

        '_' -> {
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                append(matchResult.value.trim('_'))
            }
        }

        '~' -> {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                append(matchResult.value.trim('~'))
            }
        }

        '`' -> {
            withStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    background = codeSnippetBackground,
                    baselineShift = BaselineShift(0.2f)
                )
            ) {
                append(matchResult.value.trim('`'))
            }
        }

        'h' -> {
            withLink(
                link = LinkAnnotation.Url(matchResult.value),
            ) {
                withStyle(
                    style = SpanStyle(
                        color = if (primary) colors.secondary else colors.primary
                    )
                ) {
                    append(matchResult.value)
                }
            }
        }

        else -> append(matchResult.value)
    }
}