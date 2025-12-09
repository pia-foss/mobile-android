package com.kape.rating.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kape.ui.R
import com.kape.ui.tiles.Dialog

@Composable
fun RatingFeedbackDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.rating_feedback_title),
        text = stringResource(id = R.string.rating_feedback_message),
        onConfirmButtonText = stringResource(id = R.string.yes),
        onConfirm = onConfirm,
        onDismissButtonText = stringResource(id = R.string.no),
        onDismiss = onDismiss,
    )
}

@Composable
fun RatingReviewDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.rating_review_title),
        text = stringResource(id = R.string.rating_review_message),
        onConfirmButtonText = stringResource(id = R.string.yes),
        onConfirm = onConfirm,
        onDismissButtonText = stringResource(id = R.string.no),
        onDismiss = onDismiss,
    )
}