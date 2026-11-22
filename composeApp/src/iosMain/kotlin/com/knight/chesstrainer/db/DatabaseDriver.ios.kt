package com.knight.chesstrainer.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createDatabaseDriver(platformContext: Any?): SqlDriver =
    NativeSqliteDriver(ChessDatabase.Schema, "chess-trainer.db")
