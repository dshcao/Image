package com.osshare.image.annotation

import androidx.annotation.IntDef
import com.osshare.image.annotation.ViewShape.Companion.CIRCLE
import com.osshare.image.annotation.ViewShape.Companion.SQUARE


@Retention(AnnotationRetention.SOURCE)
@IntDef(SQUARE, CIRCLE)
annotation class ViewShape{
    companion object {
        const val SQUARE = 1
        const val CIRCLE = 1 shl 1
    }
}