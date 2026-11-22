package com.knight.chesstrainer.db

import app.cash.sqldelight.db.SqlDriver

/** Platform implementations select the right SQLite driver for Android, desktop, or iOS. */
expect fun createDatabaseDriver(platformContext: Any? = null): SqlDriver
