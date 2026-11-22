package com.knight.chesstrainer.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun createDatabaseDriver(platformContext: Any?): SqlDriver {
    val context = requireNotNull(platformContext as? Context) { "Android Context is required to open the local database" }
    return AndroidSqliteDriver(ChessDatabase.Schema, context.applicationContext, "chess-trainer.db")
}
