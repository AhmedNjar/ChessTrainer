package com.knight.chesstrainer.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual fun createDatabaseDriver(platformContext: Any?): SqlDriver =
    JdbcSqliteDriver("jdbc:sqlite:chess-trainer.db", schema = ChessDatabase.Schema)
