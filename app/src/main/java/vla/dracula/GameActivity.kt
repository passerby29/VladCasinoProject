package vla.dracula

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import vla.dracula.databinding.ActivityGameBinding
import vla.dracula.models.BoardSize
import vla.dracula.models.MemoryGame

class GameActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var clRoot: ConstraintLayout
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var adapter: MemoryBoardAdapter
    private val boardSize: BoardSize = BoardSize.SIZE
    private lateinit var binding: ActivityGameBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.reloadBtn.setOnClickListener {
            onResume()
        }
    }

    override fun onResume() {
        super.onResume()

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        tvNumPairs.setText(R.string.pairs_0_6)
        tvNumMoves.setText(R.string.moves_0)
        binding.reloadBtn.visibility = View.GONE

        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)
        adapter = MemoryBoardAdapter(
            this,
            boardSize,
            memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) {
                    updateGameWithFlip(position)
                }

            })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())

    }

    private fun updateGameWithFlip(position: Int) {
        // Error checking
        if (memoryGame.haveWonGame()) {
            // alert the user invalid move
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFacedUp(position)) {
            // alert the user invalid move
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
            return
        }
        //actually flipping over card
        if (memoryGame.flipCard(position)) {
            Log.i(TAG, "found a Match! Num Pairs found: ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int

            tvNumPairs.setTextColor(color)
            val pairs = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            tvNumPairs.text = pairs
            if (memoryGame.haveWonGame()) {
                Snackbar.make(clRoot, "You Won! Congratulations.", Snackbar.LENGTH_LONG).show()
                binding.reloadBtn.visibility = View.VISIBLE
            }
        }
        val moves = "Moves: ${memoryGame.getNumMoves()}"
        tvNumMoves.text = moves
        adapter.notifyDataSetChanged()

    }
}
