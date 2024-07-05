package com.example.rvs

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.BaseColumns
import android.util.Log
import com.bumptech.glide.Glide
import kotlin.math.min

private const val TAG = "VideoProvider"

class VideoProvider : ContentProvider() {

    private companion object {
        val POSTERS_URL = arrayOf(
            "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Balcony%20Toss/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Dance%20Search/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Epic%20Docs%20Animation/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Extra%20Spicy/card.jpg"
        )

        private val QUERY_PROJECTION = arrayOf(
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1, // name
            SearchManager.SUGGEST_COLUMN_TEXT_2, // description
            SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE,
            SearchManager.SUGGEST_COLUMN_IS_LIVE,
            SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR,
            SearchManager.SUGGEST_COLUMN_DURATION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
        )

        const val SEARCH_SUGGEST = 1
        const val GET_POSTER = 2

        val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            val auth = VideoContract.CONTENT_AUTHORITY
            addURI(auth, "search/${SearchManager.SUGGEST_URI_PATH_QUERY}", SEARCH_SUGGEST)
            addURI(auth, "search/${SearchManager.SUGGEST_URI_PATH_QUERY}/*", SEARCH_SUGGEST)
            addURI(auth, VideoContract.PATH_POSTER, GET_POSTER)
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.i(TAG, "uri=${uri}")
        when (sUriMatcher.match(uri)) {
            SEARCH_SUGGEST -> {
                if (selectionArgs?.isNotEmpty() == true)
                {
                    return onSearchSuggest(selectionArgs[0], uri.getQueryParameter("limit")?.toInt() ?: -1)
                }
            }
            else -> Log.w(TAG, "URI is not matched")
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("Not yet implemented")
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        Log.i(TAG, "Open file uri=${uri}")
        if (sUriMatcher.match(uri) == GET_POSTER) {
            val link = uri.getQueryParameter("link")
            Log.i(TAG, "URL for download=${link}")

            val file = Glide.with(getContext()!!)
                .downloadOnly()
                .load(link)
                .submit()
                .get()

            return AssetFileDescriptor(
                ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY),
                0, file.length())
        }
        return super.openAssetFile(uri, mode)
    }

    private fun onSearchSuggest(query: String, limit: Int) : Cursor? {
        Log.i(TAG, "Request for search query=${query}, limit=${limit}")

        val cursor = MatrixCursor(QUERY_PROJECTION)

        var _limit = limit
        if (_limit != -1)
            _limit = min(_limit, POSTERS_URL.size)

        for (i in 0..<_limit)
        {
            cursor.addRow(arrayOf(
                i,
                query,
                "$query test description",
                "${VideoContract.BASE_CONTENT_URI}/${VideoContract.PATH_POSTER}/?link=${POSTERS_URL[i]}",
                false, // is_live
                2000, // year
                20 * 60 * 1000, // duration in ms
                "${VideoContract.BASE_CONTENT_URI}/${VideoContract.PATH_VIDEO}/${i}"
            ))
        }


        return cursor
    }
}