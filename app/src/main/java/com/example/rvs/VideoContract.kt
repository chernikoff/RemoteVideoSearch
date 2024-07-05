package com.example.rvs

import android.net.Uri

class VideoContract {
    companion object {
        const val CONTENT_AUTHORITY = "com.example.rvs"
        val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")
        const val PATH_VIDEO = "video"
        const val PATH_POSTER = "poster"
    }
}