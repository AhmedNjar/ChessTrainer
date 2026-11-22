package com.knight.chesstrainer.android

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.knight.chesstrainer.ChessTrainerPreviewContent

@Preview(name = "Home", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenAndroidPreview() = ChessTrainerPreviewContent("Home")

@Preview(name = "Opening Trainer", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OpeningTrainerAndroidPreview() = ChessTrainerPreviewContent("Opening Trainer")

@Preview(name = "Puzzle Arena", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PuzzleArenaAndroidPreview() = ChessTrainerPreviewContent("Puzzle Arena")

@Preview(name = "Settings", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsAndroidPreview() = ChessTrainerPreviewContent("Settings")