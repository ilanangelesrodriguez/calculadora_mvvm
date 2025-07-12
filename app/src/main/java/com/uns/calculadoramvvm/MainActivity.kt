package com.uns.calculadoramvvm

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.uns.calculadoramvvm.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
        setupBottomSheet()
    }

    private fun setupUI() {
        // Setup RecyclerView for history
        historyAdapter = HistoryAdapter { historyItem ->
            viewModel.useHistoryItem(historyItem)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
        }

    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.btnClearHistory.setOnClickListener {
            viewModel.clearHistory()
        }
    }

    private fun setupObservers() {
        viewModel.display.observe(this, Observer { display ->
            binding.tvDisplay.text = display
            animateDisplay()
        })

        viewModel.expression.observe(this, Observer { expression ->
            binding.tvExpression.text = expression
        })




        // Observe StateFlow for history
        lifecycleScope.launch {
            viewModel.history.collect { history ->
                historyAdapter.submitList(history)
                binding.tvHistoryEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        // Number buttons
        binding.btn0.setOnClickListener { viewModel.onNumberClick("0") }
        binding.btn1.setOnClickListener { viewModel.onNumberClick("1") }
        binding.btn2.setOnClickListener { viewModel.onNumberClick("2") }
        binding.btn3.setOnClickListener { viewModel.onNumberClick("3") }
        binding.btn4.setOnClickListener { viewModel.onNumberClick("4") }
        binding.btn5.setOnClickListener { viewModel.onNumberClick("5") }
        binding.btn6.setOnClickListener { viewModel.onNumberClick("6") }
        binding.btn7.setOnClickListener { viewModel.onNumberClick("7") }
        binding.btn8.setOnClickListener { viewModel.onNumberClick("8") }
        binding.btn9.setOnClickListener { viewModel.onNumberClick("9") }

        // Basic operators
        binding.btnAdd.setOnClickListener { viewModel.onOperatorClick("+") }
        binding.btnSubtract.setOnClickListener { viewModel.onOperatorClick("-") }
        binding.btnMultiply.setOnClickListener { viewModel.onOperatorClick("×") }
        binding.btnDivide.setOnClickListener { viewModel.onOperatorClick("÷") }

        // Special buttons
        binding.btnEquals.setOnClickListener { viewModel.onEqualsClick() }
        binding.btnDecimal.setOnClickListener { viewModel.onDecimalClick() }
        binding.btnClear.setOnClickListener { viewModel.onClearClick() }
        binding.btnClearEntry.setOnClickListener { viewModel.onClearEntryClick() }
        binding.btnDelete.setOnClickListener { viewModel.onDeleteClick() }
        binding.btnPlusMinus.setOnClickListener { viewModel.onPlusMinusClick() }

        // Scientific functions (solo si existen en el layout)
        setupScientificButtons()

        // Memory functions
        //binding.btnMemoryClear.setOnClickListener { viewModel.onMemoryClear() }

    }

    private fun setupScientificButtons() {
        // Solo configurar si los botones existen en el layout
        try {
            //binding.btnSquare.setOnClickListener { viewModel.onScientificFunction("x²") }
            //binding.btnCube.setOnClickListener { viewModel.onScientificFunction("x³") }
            //binding.btnReciprocal.setOnClickListener { viewModel.onScientificFunction("1/x") }
        } catch (e: Exception) {
            // Los botones científicos no existen en este layout
        }
    }

    private fun animateDisplay() {
        val animator = ObjectAnimator.ofFloat(binding.tvDisplay, "alpha", 0.5f, 1.0f)
        animator.duration = 150
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun toggleTheme() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
