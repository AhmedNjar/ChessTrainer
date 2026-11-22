package com.knight.chesstrainer

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember
import com.knight.chesstrainer.db.ChessDatabase
import com.knight.chesstrainer.db.createDatabaseDriver

fun MainViewController() = ComposeUIViewController {
    val database = remember { ChessDatabase(createDatabaseDriver()) }
    App(database)
}
