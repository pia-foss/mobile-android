package com.kape.ui.mobile.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.router.AppFlow
import com.kape.router.EnterFlow
import com.kape.router.Router
import com.kape.ui.R
import com.kape.ui.utils.LocalColors
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Footer(modifier: Modifier = Modifier) {
    ProvideTextStyle(
        value = TextStyle(
            fontSize = 14.sp,
        ),
    ) {
        Row(modifier = modifier) {
            ClickableText(R.string.privacy_policy, EnterFlow.PrivacyPolicy)
            Text(
                text = " ${stringResource(R.string.and)} ",
                modifier = Modifier.semantics {
                    this.invisibleToUser()
                },
            )
            ClickableText(R.string.terms_of_service, EnterFlow.TermsOfService)
        }
    }
}

@Composable
fun ClickableText(@StringRes textId: Int, flow: AppFlow) {
    val router: Router = koinInject()
    val primaryColor = LocalColors.current.primary

    Text(
        text = stringResource(textId),
        color = primaryColor,
        overflow = TextOverflow.Clip,
        softWrap = true,
        modifier = Modifier
            .clickable { router.handleFlow(flow) }
            // This draws the underline.
            // We could add the underline by defining the style as TextDecoration.Underline
            // However, this causes the font size to change, which makes it quite hard to
            // align texts vertically
            .drawBehind {
                val verticalOffset = size.height - 4.sp.toPx()
                drawLine(
                    color = primaryColor,
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(0f, verticalOffset),
                    end = Offset(size.width, verticalOffset),
                )
            },
    )
}