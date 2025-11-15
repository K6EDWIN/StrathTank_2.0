// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/data/Comment.kt
package com.example.strathtankalumni.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val id: String = "",
    val text: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)