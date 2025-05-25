/**
 * COMPUTER STRATEGY DOCUMENTATION
 *
 * The computer strategy implemented here is a "Value-Based Strategy" that makes decisions
 * based on the current game state and dice values. It's designed to be efficient but easy to understand.
 *
 * STRATEGY OVERVIEW:
 * 1. The computer evaluates its current position in the game (ahead, behind, or close to winning)
 * 2. Based on this position, it adopts different risk levels for rerolling
 * 3. It keeps high-value dice (5s and 6s) and rerolls low-value dice (1s and 2s)
 * 4. It makes smarter decisions in the second reroll based on what happened in the first reroll
 *
 * DECISION MAKING PROCESS:
 * - First, decide WHETHER to reroll based on:
 *   a) Current dice values (if they're already good, don't reroll)
 *   b) Game situation (take more risks when behind, be conservative when ahead)
 *   c) How close to the target score (be more aggressive when close to winning)
 *
 * - Then, decide WHICH dice to keep based on:
 *   a) Keep high value dice (5s and 6s)
 *   b) Keep medium value dice (3s and 4s) depending on the situation
 *   c) Almost always reroll low value dice (1s and 2s)
 *
 * ADVANTAGES:
 * - Simple to understand and implement
 * - Adapts to the game situation (plays differently when ahead vs. behind)
 * - Makes reasonable decisions about which dice to keep
 * - Balances risk and reward appropriately
 *
 * DISADVANTAGES:
 * - Not as sophisticated as more complex strategies that might consider probabilities
 * - Doesn't account for specific combinations of dice
 * - Doesn't learn from previous games or adapt to the human player's strategy
 *
 */

package com.example.dicegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Main Activity class that serves as the entry point for the application.
 * Sets up the Compose UI by calling the DiceGameApp composable.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Set the main composable for the app
            DiceGameApp()
        }
    }
}

/**
 * Main composable function that manages the app's navigation between screens.
 * Handles the state for which screen is currently displayed and the target score.
 */
@Composable
fun DiceGameApp() {
    // Track which screen is currently displayed (menu or game)
    var screenState by rememberSaveable { mutableStateOf("menu") }
    // Store the target score that players need to reach to win
    var targetScore by rememberSaveable { mutableStateOf(101) }

    // Display the appropriate screen based on current state
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image that covers the entire screen
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay to ensure text remains readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.85f)
                .background(Color.White)
        )

        // Display the appropriate screen based on current state
        when (screenState) {
            "menu" -> MenuScreen(
                onNewGame = { screenState = "game" },
                targetScore = targetScore,
                onTargetScoreChange = { targetScore = it }
            )
            "game" -> GameScreen(
                onGameEnd = { screenState = "menu" },
                targetScore = targetScore
            )
        }
    }
}

/**
 * Menu screen composable that displays the game title and buttons for
 * starting a new game, setting the target score, and viewing about information.
 */
@Composable
fun MenuScreen(
    onNewGame: () -> Unit,
    targetScore: Int,
    onTargetScoreChange: (Int) -> Unit
) {
    // State variables for dialog visibility
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    // Temporary variable to hold target score during editing
    var tempTargetScore by rememberSaveable { mutableStateOf(targetScore.toString()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game title
        Text(
            "Dice Game",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // New Game button
        Button(onClick = onNewGame, modifier = Modifier.padding(16.dp)) {
            Text("New Game")
        }

        // Set Score button
        Button(onClick = { showSettingsDialog = true }, modifier = Modifier.padding(16.dp)) {
            Text("Set Score")
        }

        // About button
        Button(onClick = { showAboutDialog = true }, modifier = Modifier.padding(16.dp)) {
            Text("About")
        }

        // About dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About") },
                text = {
                    Text(
                        "I confirm that I understand what plagiarism is and have read and " +
                                "understood the section on Assessment Offences in the Essential " +
                                "Information for Students. The work that I have submitted is " +
                                "entirely my own. Any work from other authors is duly referenced " +
                                "and acknowledged.\n\n" +
                                "Student ID : 20230921\n" +
                                "Student Name : K.A.T.V.Perera"
                    )
                },
                confirmButton = {}
            )
        }

        // Settings dialog for changing target score
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Game Settings") },
                text = {
                    Column {
                        Text(
                            "Target Score:",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = tempTargetScore,
                            onValueChange = {
                                // Only allow numeric input
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    tempTargetScore = it
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Convert input to integer with fallback to 101
                            val newScore = tempTargetScore.toIntOrNull() ?: 101
                            // Ensure minimum score of 10
                            onTargetScoreChange(maxOf(10, newScore))
                            showSettingsDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Reset to original value on cancel
                            tempTargetScore = targetScore.toString()
                            showSettingsDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Game screen composable that handles the main gameplay.
 * Manages dice rolling, scoring, computer turns, and game state.
 */
@Composable
fun GameScreen(onGameEnd: () -> Unit, targetScore: Int) {
    // Player scores
    var humanScore by rememberSaveable { mutableStateOf(0) }
    var computerScore by rememberSaveable { mutableStateOf(0) }

    // Dice values for both players
    var humanDiceValues by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var computerDiceValues by rememberSaveable { mutableStateOf(List(5) { 1 }) }

    // Track which dice the player has selected to keep
    var selectedDice by rememberSaveable { mutableStateOf(List(5) { false }) }

    // Current game state (throw, reroll, score, computerTurn, gameOver)
    var gameState by rememberSaveable { mutableStateOf("throw") }

    // Track how many rerolls the player has used in current turn
    var rerollsUsed by rememberSaveable { mutableStateOf(0) }

    // Score tracking for the current turn
    var throwTotal by rememberSaveable { mutableStateOf(0) }
    var firstRerollTotal by rememberSaveable { mutableStateOf(0) }
    var secondRerollTotal by rememberSaveable { mutableStateOf(0) }
    var currentTotal by rememberSaveable { mutableStateOf(0) }

    // Computer turn state
    var computerTurnInProgress by rememberSaveable { mutableStateOf(false) }

    // Game over dialog state
    var showGameOverDialog by rememberSaveable { mutableStateOf(false) }

    // Tie-breaking state
    var isTieBreaking by rememberSaveable { mutableStateOf(false) }
    var humanTieRoll by rememberSaveable { mutableStateOf(0) }
    var computerTieRoll by rememberSaveable { mutableStateOf(0) }

    // Computer strategy logging
    var computerStrategyLog by rememberSaveable { mutableStateOf("") }

    // Error message for dice selection
    var showKeepDiceMessage by rememberSaveable { mutableStateOf(false) }

    // Handle back button when game is over
    if (gameState == "gameOver") {
        BackHandler {
            onGameEnd()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display target score
        Text(
            "Target: $targetScore",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display current scores
        Text(
            "H: $humanScore / C: $computerScore",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        // Display computer's dice
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            computerDiceValues.forEach { value ->
                Image(
                    painter = painterResource(id = getDiceImage(value)),
                    contentDescription = "Computer Dice $value",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display current total score for this turn
        if (gameState != "throw" && gameState != "gameOver" && !isTieBreaking) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    "Current Total: $currentTotal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Display tie breaker information
        if (isTieBreaking) {
            Text(
                "TIE BREAKER",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (humanTieRoll > 0 || computerTieRoll > 0) {
                Text(
                    "Tie Roll - H: $humanTieRoll / C: $computerTieRoll",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Display player's dice with selection highlighting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            humanDiceValues.forEachIndexed { index, value ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    // Highlight selected dice with a background
                    if (selectedDice[index] && gameState == "reroll" && rerollsUsed == 0 && !isTieBreaking) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = Color(0xFFE3F2FD),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    // Dice image with click handler for selection
                    Image(
                        painter = painterResource(id = getDiceImage(value)),
                        contentDescription = "Your Dice $value",
                        modifier = Modifier
                            .size(60.dp)
                            .clickable(enabled = gameState == "reroll" && rerollsUsed == 0 && !isTieBreaking) {
                                if (gameState == "reroll" && rerollsUsed == 0 && !isTieBreaking) {
                                    val selectedCount = selectedDice.count { it }

                                    if (selectedDice[index]) {
                                        // Deselect a die
                                        selectedDice = selectedDice.toMutableList().also { it[index] = false }
                                    }
                                    else if (selectedCount < 4) {
                                        // Select a die (max 4)
                                        selectedDice = selectedDice.toMutableList().also { it[index] = true }
                                    }
                                }
                            }
                    )
                }
            }
        }

        // Instructions for dice selection
        if (gameState == "reroll" && rerollsUsed == 0 && !isTieBreaking) {
            Text(
                "Tap dice you want to KEEP (max 4)",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Error message for invalid dice selection
        if (showKeepDiceMessage) {
            Text(
                "Please select 1-4 dice to keep",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game controls based on current state
        when {
            // Tie-breaking controls
            isTieBreaking -> {
                Button(onClick = {
                    // Roll dice for both players to break the tie
                    humanDiceValues = List(5) { Random.nextInt(1, 7) }
                    humanTieRoll = humanDiceValues.sum()

                    computerDiceValues = List(5) { Random.nextInt(1, 7) }
                    computerTieRoll = computerDiceValues.sum()

                    // End tie-breaking if scores are different
                    if (humanTieRoll != computerTieRoll) {
                        isTieBreaking = false
                        gameState = "gameOver"
                        showGameOverDialog = true
                    }

                }, modifier = Modifier.padding(8.dp)) {
                    Text("Roll for Tie-Break")
                }
            }

            // Initial throw controls
            gameState == "throw" -> {
                Button(onClick = {
                    // Reset turn state
                    rerollsUsed = 0
                    selectedDice = List(5) { false }
                    showKeepDiceMessage = false

                    // Roll player's dice
                    humanDiceValues = List(5) { Random.nextInt(1, 7) }

                    // Calculate initial score
                    throwTotal = humanDiceValues.sum()
                    currentTotal = throwTotal

                    firstRerollTotal = 0
                    secondRerollTotal = 0

                    // Roll computer's dice
                    computerDiceValues = List(5) { Random.nextInt(1, 7) }

                    // Move to reroll state
                    gameState = "reroll"
                }, modifier = Modifier.padding(8.dp)) {
                    Text("Throw")
                }
            }

            // Reroll controls
            gameState == "reroll" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (rerollsUsed < 2) {
                        Button(onClick = {
                            if (rerollsUsed == 0) {
                                // First reroll - validate selection
                                val selectedCount = selectedDice.count { it }
                                if (selectedCount < 1) {
                                    showKeepDiceMessage = true
                                    return@Button
                                }
                                if (selectedCount > 4) {
                                    showKeepDiceMessage = true
                                    return@Button
                                }

                                showKeepDiceMessage = false

                                // Reroll non-kept dice
                                humanDiceValues = humanDiceValues.mapIndexed { index, value ->
                                    if (!selectedDice[index]) Random.nextInt(1, 7) else value
                                }

                                // Update score
                                firstRerollTotal = humanDiceValues.sum()
                                currentTotal = throwTotal + firstRerollTotal
                            } else {
                                // Second reroll - reroll kept dice
                                humanDiceValues = humanDiceValues.mapIndexed { index, value ->
                                    if (selectedDice[index]) Random.nextInt(1, 7) else value
                                }

                                // Update score
                                secondRerollTotal = humanDiceValues.sum()
                                currentTotal = throwTotal + firstRerollTotal + secondRerollTotal
                            }

                            rerollsUsed++

                            // End player's turn after second reroll
                            if (rerollsUsed == 2) {
                                humanScore += currentTotal
                                gameState = "computerTurn"
                                computerTurnInProgress = true
                            }
                        }, modifier = Modifier.padding(8.dp)) {
                            Text(if (rerollsUsed == 0) "Reroll Non-Kept Dice" else "Reroll Kept Dice")
                        }
                    }

                    // Score button to end turn without using all rerolls
                    Button(onClick = {
                        gameState = "score"
                    }, modifier = Modifier.padding(8.dp)) {
                        Text("Score")
                    }
                }
            }

            // Score state - add current total to player's score
            gameState == "score" -> {
                LaunchedEffect(Unit) {
                    humanScore += currentTotal
                    gameState = "computerTurn"
                    computerTurnInProgress = true
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Computer's turn
        if (gameState == "computerTurn" && !isTieBreaking) {
            Text("Computer is playing...", fontSize = 18.sp, modifier = Modifier.padding(8.dp))

            if (computerTurnInProgress) {
                LaunchedEffect(Unit) {
                    // Calculate initial score
                    var computerTotal = computerDiceValues.sum()
                    computerStrategyLog = "First roll: ${computerDiceValues.joinToString()} = $computerTotal"

                    var computerRerollsUsed = 0
                    var firstRollValues = computerDiceValues.toList()

                    // Computer's reroll logic
                    while (computerRerollsUsed < 2) {
                        // Add delay to make computer's turn visible
                        delay(1000)

                        // Calculate game state variables for strategy
                        val scoreDifference = computerScore - humanScore
                        val pointsToTarget = targetScore - computerScore
                        val currentRollTotal = computerDiceValues.sum()

                        // Decide whether to reroll
                        val shouldReroll = if (computerRerollsUsed == 0) {
                            // First reroll decision
                            decideFirstReroll(computerDiceValues, scoreDifference, pointsToTarget)
                        } else {
                            // Second reroll decision
                            decideSecondReroll(computerDiceValues, firstRollValues, scoreDifference, pointsToTarget)
                        }

                        if (shouldReroll) {
                            // Decide which dice to keep
                            val keepDice = if (computerRerollsUsed == 0) {
                                decideWhichDiceToKeep(computerDiceValues, scoreDifference, pointsToTarget)
                            } else {
                                List(5) { index -> firstRollValues[index] == computerDiceValues[index] }
                            }

                            // Log strategy decisions
                            computerStrategyLog += "\nReroll #${computerRerollsUsed + 1}: " +
                                    "Keeping ${computerDiceValues.filterIndexed { index, _ -> keepDice[index] }}"

                            // Perform reroll based on which reroll it is
                            if (computerRerollsUsed == 0) {
                                // First reroll - keep selected dice
                                computerDiceValues = computerDiceValues.mapIndexed { index, value ->
                                    if (!keepDice[index]) Random.nextInt(1, 7) else value
                                }
                                firstRollValues = computerDiceValues.toList()
                            } else {
                                // Second reroll - reroll kept dice
                                computerDiceValues = computerDiceValues.mapIndexed { index, value ->
                                    if (keepDice[index]) Random.nextInt(1, 7) else value
                                }
                            }

                            // Update score
                            val newRollTotal = computerDiceValues.sum()
                            computerTotal += newRollTotal
                            computerStrategyLog += "\nNew roll: ${computerDiceValues.joinToString()} = $newRollTotal"
                        } else {
                            // Log decision not to reroll
                            computerStrategyLog += "\nDecided not to reroll #${computerRerollsUsed + 1}"
                            break
                        }

                        computerRerollsUsed++
                    }

                    // Add final score to computer's total
                    computerScore += computerTotal
                    computerStrategyLog += "\nFinal total: $computerTotal"
                    computerTurnInProgress = false

                    // Check for game end conditions
                    if (humanScore >= targetScore && computerScore >= targetScore) {
                        // Both players reached target - go to tie-breaker
                        isTieBreaking = true
                        humanTieRoll = 0
                        computerTieRoll = 0
                        gameState = "tieBreak"
                    }
                    else if (humanScore >= targetScore || computerScore >= targetScore) {
                        // One player reached target - game over
                        gameState = "gameOver"
                        showGameOverDialog = true
                    } else {
                        // Continue game
                        gameState = "throw"
                    }
                }
            }
        }

        // Game over dialog
        if (showGameOverDialog) {
            // Determine winner
            val playerWon = if (isTieBreaking) {
                humanTieRoll > computerTieRoll
            } else {
                humanScore >= targetScore && humanScore > computerScore
            }

            AlertDialog(
                onDismissRequest = {
                    showGameOverDialog = false
                },
                title = {
                    Text(
                        text = if (playerWon) "You Win!" else "You Lose!",
                        color = if (playerWon) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Display final scores
                        Text(
                            text = "Final Score - H: $humanScore / C: $computerScore",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        // Display tie-breaker results if applicable
                        if (isTieBreaking) {
                            Text(
                                text = "Tie-Breaking Roll - H: $humanTieRoll / C: $computerTieRoll",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Instructions to return to menu
                        Text(
                            text = "Press back button to return to menu",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                },
                confirmButton = {}
            )
        }
    }
}

/**
 * Decides whether the computer should perform the first reroll.
 */
fun decideFirstReroll(
    diceValues: List<Int>,
    scoreDifference: Int,
    pointsToTarget: Int
): Boolean {
    val rollTotal = diceValues.sum()
    val highValueDiceCount = diceValues.count { it >= 5 }
    val lowValueDiceCount = diceValues.count { it <= 2 }

    // Don't reroll if already have a good roll
    if (highValueDiceCount >= 3 && rollTotal >= 22) {
        return false
    }

    // Always reroll if many low-value dice
    if (lowValueDiceCount >= 3) {
        return true
    }

    // Different thresholds based on game situation
    if (pointsToTarget < 30) {
        return rollTotal < 20  // More aggressive near end
    }

    if (scoreDifference < -10) {
        return rollTotal < 18  // More aggressive when behind
    }

    if (scoreDifference > 10) {
        return rollTotal < 15  // More conservative when ahead
    }

    // Default threshold
    return rollTotal < 17
}

/**
 * Decides whether the computer should perform the second reroll.
 */
fun decideSecondReroll(
    diceValues: List<Int>,
    firstRollValues: List<Int>,
    scoreDifference: Int,
    pointsToTarget: Int
): Boolean {
    val rollTotal = diceValues.sum()

    // Don't reroll if already have a very good roll
    if (rollTotal >= 25) {
        return false
    }

    // Count high-value dice that were kept from first roll
    val highValueKeptDice = diceValues.filterIndexed { index, value ->
        value >= 5 && value == firstRollValues[index]
    }.count()

    // Don't reroll if kept many high-value dice
    if (highValueKeptDice >= 3) {
        return false
    }

    // Different strategies based on game situation
    if (pointsToTarget < 20) {
        return true  // Aggressive when close to winning
    }

    if (scoreDifference < -20) {
        return true  // Aggressive when far behind
    }

    if (scoreDifference > 20) {
        return false  // Conservative when far ahead
    }

    // Default threshold
    return rollTotal < 20
}

/**
 * Decides which dice the computer should keep during rerolls.
 *
 * @param diceValues Current dice values
 * @param scoreDifference Difference between computer and human scores
 * @param pointsToTarget Points needed to reach target score
 * @return List of booleans indicating which dice to keep
 */
fun decideWhichDiceToKeep(
    diceValues: List<Int>,
    scoreDifference: Int,
    pointsToTarget: Int
): List<Boolean> {
    // Initial decision based on dice values
    val keepDice = diceValues.map { value ->
        when {
            value >= 5 -> true   // Always keep high values
            value <= 2 -> false  // Always reroll low values
            else -> false        // Default for medium values
        }
    }.toMutableList()

    // Refine decisions for medium-value dice based on game situation
    diceValues.forEachIndexed { index, value ->
        if (value in 3..4) {
            if (scoreDifference > 15) {
                // When ahead, be more conservative
                keepDice[index] = true
            }
            else if (pointsToTarget < 30) {
                // When close to winning, keep 4s but reroll 3s
                keepDice[index] = (value == 4)
            }
            else if (scoreDifference < -15) {
                // When behind, be more aggressive
                keepDice[index] = false
            }
            else {
                // Default strategy - keep 4s, reroll 3s
                keepDice[index] = (value == 4)
            }
        }
    }

    // Ensure at least one die is kept
    if (!keepDice.contains(true)) {
        val maxValue = diceValues.maxOrNull() ?: 1
        val maxIndex = diceValues.indexOf(maxValue)
        keepDice[maxIndex] = true
    }

    return keepDice
}

/**
 * Returns the appropriate drawable resource ID for a given dice value.
 * @return Resource ID for the corresponding dice image
 */
fun getDiceImage(value: Int): Int {
    return when (value) {
        1 -> R.drawable.dice1
        2 -> R.drawable.dice2
        3 -> R.drawable.dice3
        4 -> R.drawable.dice4
        5 -> R.drawable.dice5
        6 -> R.drawable.dice6
        else -> R.drawable.dice1
    }
}