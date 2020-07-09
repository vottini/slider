package br.com.egretta.slider

import android.view.MotionEvent
import android.view.View

class MoveListener(idx : Int, field : Playground) : View.OnTouchListener  {

    class ExecutedMove(newIdx : Int, move : Movement) {
        val newIdx = newIdx
        val move = move
    }

    private var grabbing : Boolean = true
    private val field : Playground = field
    private val index = idx

    private var executedMove : ExecutedMove? = null
    private var xOffset : Int = 0
    private var yOffset : Int = 0

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_MOVE) {
            if (!this.grabbing) return true
            checkNotNull(v)

            if (null == this.executedMove) {
                var detectedMovement : Movement? = null

                when {
                    (event.y < 0) -> detectedMovement = Movement.UP
                    (event.x < 0) -> detectedMovement = Movement.LEFT
                    (event.x > v.measuredWidth) -> detectedMovement = Movement.RIGHT
                    (event.y > v.measuredHeight) -> detectedMovement = Movement.DOWN
                }

                detectedMovement?.let { movement ->
                    this.field.calcNewIndex(this.index, movement)?.let { nextIdx ->
                        this.executedMove = ExecutedMove(nextIdx, movement)
                        this.field.move(this.index, nextIdx)

                        when (movement) {
                            Movement.UP    -> yOffset = -v.measuredHeight
                            Movement.DOWN  -> yOffset = +v.measuredHeight
                            Movement.LEFT  -> xOffset = -v.measuredWidth
                            Movement.RIGHT -> xOffset = +v.measuredWidth
                        }

                        return true
                    }

                    // there was some detected movement (outside the block) but
                    // it could not be carried, so release the block
                    this.grabbing = false
                }
            }

            else {
                val insideOriginal =
                    (event.y > 0) and (event.y < v.measuredHeight) and
                    (event.x > 0) and (event.x < v.measuredWidth)

                if (insideOriginal) {
                    this.field.move(this.executedMove!!.newIdx, this.index)
                    this.executedMove = null

                    this.yOffset = 0
                    this.xOffset = 0
                    return true
                }

                this.grabbing =
                    (event.y > yOffset) and (event.y < yOffset + v.measuredHeight) and
                    (event.x > xOffset) and (event.x < xOffset + v.measuredWidth)
            }

            return true
        }

        if (event?.action == MotionEvent.ACTION_UP) {
            this.grabbing = true
            this.executedMove = null
            this.yOffset = 0
            this.xOffset = 0
        }

        return true
    }

}