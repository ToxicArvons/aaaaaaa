package lv.rtu.rdb231rdb024.aaaaaaa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {


    private var curplayer = "X" // Pašreizējais spēlētājs
    private var inplayer = "X" // Sākotnējais spēlētājs
    private var gameactive = true // spēle aktīva?
    private var vscom = false // spēle dators?
    private lateinit var player1Name: String // 1. 1 sp vārds
    private lateinit var player2Name: String // 2. 2 sp vārds
    private lateinit var tvStatus: TextView // Statuss teksts
    private lateinit var btnreset: Button // reset poga
    private lateinit var gridlay: GridLayout // spēles režģis
    private val board = arrayOfNulls<String>(9) // spēles laukums

    // minimax  mainīgie
    private val humanplayer = "X" // cilvēka simbols
    private val pcplayer = "O" // datora simbols
    private val wincombos = arrayOf( // uzvaras kombinācijas
        intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),  
        intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),  
        intArrayOf(0, 4, 8), intArrayOf(2, 4, 6) 
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // inicializē spēlētāju vārdus
        player1Name = intent.getStringExtra("PLAYER1") ?: "Spēlētājs 1"
        player2Name = intent.getStringExtra("PLAYER2") ?: if (vscom) "Dators" else "Spēlētājs 2"

        // sveicieni spēlētājiem
        if (vscom) {
            Toast.makeText(this, "Laipni lūgti, $player1Name!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Laipni lūgti, $player1Name & $player2Name!", Toast.LENGTH_SHORT).show()
        }

        // sasaista UI elementus
        tvStatus = findViewById(R.id.tvStatus)
        btnreset = findViewById(R.id.btnreset)
        gridlay = findViewById(R.id.gridlay)

        // iestata spēles režīmu un sākuma spēlētāju
        vscom = intent.getBooleanExtra("VS_COMPUTER", false)
        inplayer = intent.getStringExtra("FIRST_PLAYER") ?: "X"
        curplayer = inplayer

        // reset poga
        btnreset.setOnClickListener { resetGame() }

        // atpakaļ poga
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // inicializē spēles laukumu
        inboard()
        updatestatus()

        // ja dators pirmais
        if (vscom && curplayer == pcplayer && gameactive) {
            Handler(Looper.getMainLooper()).postDelayed({
                val bestMove = bestmove()
                val button = gridlay.findViewById<Button>(
                    resources.getIdentifier("btn$bestMove", "id", packageName)
                )
                makemove(bestMove, button)
            }, 500)
        }
    }

    // inicializē spēles laukumu un pogas
    private fun inboard() {
        val buttons = Array(9) { i ->
            findViewById<Button>(resources.getIdentifier("btn$i", "id", packageName))!!
        }


        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (gameactive && board[index] == null) {
                    makemove(index, button)
                }
            }
        }
    }

    // veic gājienu un pārbauda spēles stāvokli
    private fun makemove(pos: Int, button: Button) {
        // pārbauda derīgu gājienu
        if (pos !in 0..8 || !gameactive || board[pos] != null) return

        // atjaunina spēles laukumu un pogas
        board[pos] = curplayer
        button.text = curplayer

        // pārbauda uzvaras un neizšķirtu gadījumus
        when {
            checkwinner() -> winap()
            isboardfull() -> draw()
            else -> {
                switchpl()
                updatestatus()

                // datora gājiens
                if (vscom && curplayer == pcplayer && gameactive) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val bestMove = bestmove()
                        if (bestMove != -1) {
                            val computerButton = gridlay.findViewById<Button>(
                                resources.getIdentifier("btn$bestMove", "id", packageName)
                            )
                            makemove(bestMove, computerButton)
                        }
                    }, 500)
                }
            }
        }
    }

    // apstrādā uzvaras
    private fun winap() {
        gameactive = false
        tvStatus.text = if (vscom) {
            if (curplayer == humanplayer) "$player1Name wins!" else "$player2Name wins!"
        } else {
            "${if (curplayer == "X") player1Name else player2Name} wins!"
        }
    }

    // apstrādā neizšķirtu
    private fun draw() {
        gameactive = false
        tvStatus.text = "Draw!"
    }

    // maina spēlētāju
    private fun switchpl() {
        curplayer = if (curplayer == "X") "O" else "X"
    }

    // atjaunina statusu
    private fun updatestatus() {
        tvStatus.text = when {
            !gameactive -> when {
                checkwin(humanplayer) -> "$player1Name wins!"
                checkwin(pcplayer) -> "$player2Name wins!"
                isboardfull() -> "Draw!"
                else -> tvStatus.text
            }
            else -> "${getcurplayerName()} move"
        }
    }

    // labāks gājiens datoram
    private fun bestmove(): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = -1

        val availableMoves = mutableListOf<Int>()
        for (i in board.indices) {
            if (board[i] == null) {
                availableMoves.add(i)
            }
        }

        if (availableMoves.isEmpty()) return -1

        for (i in availableMoves) {
            board[i] = pcplayer
            val score = minimax(false)
            board[i] = null

            if (score > bestScore) {
                bestScore = score
                bestMove = i
            }
        }

        return bestMove
    }

    // minimax algoritms
    private fun minimax(isMaximizing: Boolean): Int {
        if (checkwin(pcplayer)) return 1
        if (checkwin(humanplayer)) return -1
        if (isboardfull()) return 0

        return if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    board[i] = pcplayer
                    val score = minimax(false)
                    board[i] = null
                    bestScore = max(score, bestScore)
                }
            }
            bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    board[i] = humanplayer
                    val score = minimax(true)
                    board[i] = null
                    bestScore = min(score, bestScore)
                }
            }
            bestScore
        }
    }

    // pārbauda vai dotais spēlētājs ir uzvarējis
    private fun checkwin(player: String): Boolean {
        return wincombos.any { combination ->
            combination.all { board[it] == player }
        }
    }

    // pārbauda vai pašreizējais spēlētājs ir uzvarējis
    private fun checkwinner(): Boolean {
        return checkwin(curplayer)
    }

    // pārbauda vai visi lauki aizpildīti
    private fun isboardfull(): Boolean {
        return board.all { it != null }
    }

    // restartē spēli(ja ir random spēles uzsācējs, tad paliks pirmao gājiemu veiks, tas  kas pirmo spēli uzsāka)
    private fun resetGame() {
        curplayer = inplayer
        gameactive = true
        board.fill(null)

        val gridlay = findViewById<GridLayout>(R.id.gridlay)
        for (i in 0 until gridlay.childCount) {
            (gridlay.getChildAt(i) as Button).text = ""
        }

        updatestatus()

        // sāk datora gājienu
        if (vscom && curplayer == pcplayer && gameactive) {
            Handler(Looper.getMainLooper()).postDelayed({
                val bestMove = bestmove()
                if (bestMove != -1) {
                    val computerButton = gridlay.findViewById<Button>(
                        resources.getIdentifier("btn$bestMove", "id", packageName)
                    )
                    makemove(bestMove, computerButton)
                }
            }, 500)
        }
    }

    // palīgfunkcija pašreizējā spēlētāja vārda iegūšanai
    private fun getcurplayerName(): String {
        return if (curplayer == humanplayer) player1Name else player2Name
    }
}