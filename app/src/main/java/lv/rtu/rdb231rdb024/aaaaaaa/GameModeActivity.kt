package lv.rtu.rdb231rdb024.aaaaaaa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class GameModeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_mode)

        // sasaista UI elementus ar kodu
        val etPlayer1 = findViewById<EditText>(R.id.etPlayer1)
        val etPlayer2 = findViewById<EditText>(R.id.etPlayer2)

        // poga "player vs player"
        findViewById<Button>(R.id.btnPvP).setOnClickListener {
            // Iegūst ievadītos vārdus vai izmanto noklusējumus
            val player1 = etPlayer1.text.toString().takeIf { it.isNotEmpty() } ?: "Player 1"
            val player2 = etPlayer2.text.toString().takeIf { it.isNotEmpty() } ?: "Player 2"
            startGame(false, player1, player2)
        }

        // Poga "player vs computer"
        findViewById<Button>(R.id.btnPvC).setOnClickListener {
            // Datora režīmā 2. spēlētājs vienmēr ir "Dators"
            val player1 = etPlayer1.text.toString().takeIf { it.isNotEmpty() } ?: "Player"
            startGame(true, player1, "Bot")
        }
    }

    // Funkcija spēles sākšanai
    private fun startGame(vsComputer: Boolean, player1: String, player2: String) {
        // Nosaka sākuma spēlētāju no radio pogām
        val rgFirstTurn = findViewById<RadioGroup>(R.id.rgFirstTurn)
        val selectedOption = rgFirstTurn.checkedRadioButtonId

        // Noteikumi sākuma spēlētāja izvēlei
        val firstPlayer = when (selectedOption) {
            R.id.rbPlayerFirst -> "X"    // Spēlētājs sāk pirmais
            R.id.rbComputerFirst -> "O"   // Dators sāk pirmais
            R.id.rbRandom -> if (Math.random() < 0.5) "X" else "O" // Nejauša izvēle
            else -> "X" // Noklusējums - spēlētājs sāk
        }

        // Pārsūta datus uz galveno aktivitāti
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("VS_COMPUTER", vsComputer)   // Spēles režīma flags
            putExtra("PLAYER1", player1)          // 1. spēlētāja vārds
            putExtra("PLAYER2", player2)          // 2. spēlētāja vārds
            putExtra("FIRST_PLAYER", firstPlayer)  // Kurš sāks spēli
        }
        startActivity(intent) // Palaiž galveno aktivitāti
    }
}