package br.com.egretta.slider

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.*
import androidx.core.animation.doOnEnd

class Playground : AppCompatActivity() {

    private val emptyValue = 15
    private val blocks : Array <Block?> = arrayOfNulls(16)
    private var completed : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_playground)
        val blockTable = this.findViewById <TableLayout> (R.id.blockTable)
        var counter = 0

        for (i in 0..3) {
            val newRow = TableRow(this)
            newRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT)

            blockTable.addView(newRow)

            for (j in 0..3) {
                val newImage = ImageView(this)
                newImage.layoutParams = TableRow.LayoutParams(
                    resources.getDimension(R.dimen.block_size).toInt(),
                    resources.getDimension(R.dimen.block_size).toInt())

                newRow.addView(newImage)
                val newBlock = Block(newImage, this.resources)
                this.blocks[counter] = newBlock
                counter += 1
            }
        }

        var recovered = false
        val prefs = this.getSharedPreferences(SAVED, Context.MODE_PRIVATE)
        val extras =  checkNotNull(this.intent.extras)
        val toContinue = extras.getBoolean(CONTINUE)

        if (toContinue) {
            for (i in 0..15) {
                val block = checkNotNull(this.blocks[i])
                val value = prefs.getInt("field.$i", -1)
                if (-1 == value) break
                block.value = value
            }

            recovered = true
        }

        if (!recovered) {
            for (i in 0..15) {
                this.blocks[i]?.value = i
                this.blocks[i]?.refresh()
            }

            Shuffler(blocks, 3, 3).shuffle(368)
            this.animateNewGame()
            return
        }

        blocks.forEach { block: Block? -> block?.refresh() }
        this.attachListeners()
    }


    override fun onPause() {
        super.onPause()
        val editor = this.getSharedPreferences(SAVED, Context.MODE_PRIVATE).edit()
        editor.putBoolean("completed", this.completed)

        if (!this.completed) {
            for (i in 0..15) {
                val block = checkNotNull(this.blocks[i])
                editor.putInt("field.$i", block.value)
            }
        }

        editor.apply()
    }


    fun move(index1: Int, index2 : Int) {
        val blockA = checkNotNull(this.blocks[index1])
        val blockB = checkNotNull(this.blocks[index2])
        val savedValue = blockA.value

        blockA.value = blockB.value
        blockB.value = savedValue
        blockA.refresh()
        blockB.refresh()

        var prvValue = -1
        this.blocks.forEach{ block : Block? ->
            checkNotNull(block)
            if (block.value < prvValue) return
            prvValue = block.value
        }

        this.endGame()
    }


    fun calcNewIndex(index : Int, movement : Movement) : Int? {
        val currentBlock = checkNotNull(this.blocks[index])
        val currentValue = currentBlock.value
        if (emptyValue == currentValue) {
            return null
        }

        val row = index / 4
        val col = index % 4
        var newIndex : Int? = null

        when (movement) {
            Movement.UP    -> if (row > 0) newIndex = (index - 4)
            Movement.DOWN  -> if (row < 3) newIndex = (index + 4)
            Movement.LEFT  -> if (col > 0) newIndex = (index - 1)
            Movement.RIGHT -> if (col < 3) newIndex = (index + 1)
        }

        newIndex?.let { nonNullNewIndex ->
            val nextBlock = checkNotNull(this.blocks[nonNullNewIndex])
            val newValue = nextBlock.value
            if (emptyValue != newValue) {
                return null
            }
        }

        return newIndex
    }

    @SuppressLint("NewApi")
    private fun blur() {
        Handler().post {
            val blockTable = this.findViewById<TableLayout>(R.id.blockTable)
            val bitmap = Bitmap.createBitmap(
                blockTable.width, blockTable.height,
                Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            blockTable.draw(canvas)

            val rs = RenderScript.create(this)
            val outBitmap = Bitmap.createBitmap(
                bitmap.width, bitmap.height,
                Bitmap.Config.ARGB_8888)

            val allocIn = Allocation.createFromBitmap(rs, bitmap)
            val allocOut = Allocation.createFromBitmap(rs, outBitmap)
            ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).apply {
                setInput(allocIn)
                setRadius(17.0f)
                forEach(allocOut)
            }

            allocOut.copyTo(bitmap)
            rs.destroy()

            blockTable.removeAllViews()
            val blurredField = ImageView(this)
            blurredField.setImageBitmap(bitmap)
            blockTable.addView(blurredField)
        }
    }

    private fun endGame() {
        this.completed = true
        val endingBannerFrame = this.findViewById <FrameLayout> (R.id.endingBannerFrame)
        val blockTable = this.findViewById <TableLayout> (R.id.blockTable)
        val offset = blockTable.y + blockTable.height + endingBannerFrame.height

        ObjectAnimator.ofFloat(endingBannerFrame, "translationY", -offset).apply {
            duration = 1000
            start()
        }

        this.blur()
    }

    private fun attachListeners() {
        for ((idx,block) in this.blocks.withIndex()) {
            block?.image().let { newImage ->
                val listener = MoveListener(idx, this)
                newImage?.setOnTouchListener(listener)
            }
        }
    }

    private fun animateNewGame() {
        val shufflerLayout = this.findViewById<FrameLayout>(R.id.shufflerLayout)
        val shufflerView = this.findViewById<ImageView>(R.id.shufflerView)

        val move1 = Runnable {
            var offset: Float = (Resources.getSystem().displayMetrics.heightPixels +
                shufflerLayout.height) / 2.0f;

            ObjectAnimator.ofFloat(shufflerLayout, "translationY", offset).apply {
                doOnEnd { shufflerView.setImageResource(R.drawable.ic_shuffler_on) }
                duration = 1000
                start()
            }
        }

        val move2 = Runnable {
            shufflerView.setImageResource(R.drawable.ic_shuffler_off)
            var offset: Float = (Resources.getSystem().displayMetrics.heightPixels +
                shufflerLayout.height) / 2.0f;

            this.attachListeners()
            blocks.forEach { block: Block? -> block?.refresh() }
            ObjectAnimator.ofFloat(shufflerLayout, "translationY", -offset).apply {
                duration = 1000
                start()
            }
        }

        Handler().postDelayed(move1, 500)
        Handler().postDelayed(move2, 2300)
    }
}
