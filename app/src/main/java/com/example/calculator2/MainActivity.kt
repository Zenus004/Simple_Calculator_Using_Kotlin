package com.example.calculator2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator2.R

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var lastNumeric: Boolean = false
    private var stateError: Boolean = false
    private var lastDot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)

        val buttonIDs = intArrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btn00, R.id.btnDot, R.id.btnAdd, R.id.btnSubtract,
            R.id.btnMultiply, R.id.btnDivide, R.id.btnC, R.id.btnPercent,
            R.id.btnDelete, R.id.btnEquals
        )

        for (id in buttonIDs) {
            findViewById<Button>(id).setOnClickListener { onButtonClick(it as Button) }
        }
    }

    private fun onButtonClick(button: Button) {
        when (button.id) {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btn00 -> onDigit(button.text.toString())
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide -> onOperator(button.text.toString())
            R.id.btnDot -> onDecimalPoint()
            R.id.btnEquals -> onEqual()
            R.id.btnC -> onClear()
            R.id.btnDelete -> onDelete()
            R.id.btnPercent -> onPercent()
        }
    }

    private fun onDigit(digit: String) {
        if (stateError) {
            editText.setText(digit)
            stateError = false
        } else {
            editText.append(digit)
        }
        lastNumeric = true
    }

    private fun onOperator(operator: String) {
        if (lastNumeric && !stateError) {
            editText.append(" $operator ")
            lastNumeric = false
            lastDot = false
        }
    }

    private fun onDecimalPoint() {
        if (lastNumeric && !stateError && !lastDot) {
            editText.append(".")
            lastNumeric = false
            lastDot = true
        }
    }

    private fun onEqual() {
        if (lastNumeric && !stateError) {
            val text = editText.text.toString()
            val expression = text.replace("÷", "/").replace("×", "*")
            try {
                val result = evaluate(expression)
                editText.setText(result.toString())
                lastDot = true // Result contains a dot
            } catch (e: Exception) {
                editText.setText("Error")
                stateError = true
                lastNumeric = false
            }
        }
    }

    private fun onClear() {
        editText.setText("")
        lastNumeric = false
        stateError = false
        lastDot = false
    }

    private fun onDelete() {
        val text = editText.text.toString()
        if (text.isNotEmpty()) {
            editText.setText(text.dropLast(1))
            lastNumeric = text.last().isDigit()
        }
    }

    private fun onPercent() {
        if (lastNumeric && !stateError) {
            val text = editText.text.toString()
            try {
                val value = text.toDouble() / 100
                editText.setText(value.toString())
            } catch (e: Exception) {
                editText.setText("Error")
                stateError = true
                lastNumeric = false
            }
        }
    }

    private fun evaluate(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos] else '\u0000'
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: $ch")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+') -> x + parseTerm() // addition
                        eat('-') -> x - parseTerm() // subtraction
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*') -> x * parseFactor() // multiplication
                        eat('/') -> x / parseFactor() // division
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // unary plus
                if (eat('-')) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('(')) { // parentheses
                    x = parseExpression()
                    eat(')')
                } else if (ch in '0'..'9' || ch == '.') { // numbers
                    while (ch in '0'..'9' || ch == '.') nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: $ch")
                }

                return x
            }
        }.parse()
    }
}
