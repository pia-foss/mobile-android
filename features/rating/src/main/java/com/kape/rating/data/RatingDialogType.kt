package com.kape.rating.data

sealed class RatingDialogType {

    data object General : RatingDialogType()
    data object Review : RatingDialogType()
    data object Feedback : RatingDialogType()
}