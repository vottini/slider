package br.com.egretta.slider

class Shuffler(blocks : Array <Block?>, xPivot : Int, yPivot : Int ) {

    private val blocks: Array <Block?> =  blocks
    private var undoMovement : Movement = Movement.UP
    private var xPivot : Int = xPivot
    private var yPivot : Int = yPivot

    fun shuffle(rounds : Int) {
        for (i in 0..rounds) {
            val oldIndex = yPivot * 4 + xPivot
            val moves = mutableListOf <Movement> ()

            if ((undoMovement != Movement.UP)    && (yPivot != 0)) moves.add(Movement.UP)
            if ((undoMovement != Movement.DOWN)  && (yPivot != 3)) moves.add(Movement.DOWN)
            if ((undoMovement != Movement.LEFT)  && (xPivot != 0)) moves.add(Movement.LEFT)
            if ((undoMovement != Movement.RIGHT) && (xPivot != 3)) moves.add(Movement.RIGHT)

            when (moves.random()) {
                Movement.UP    -> { undoMovement = Movement.DOWN;  yPivot -= 1 }
                Movement.DOWN  -> { undoMovement = Movement.UP;    yPivot += 1 }
                Movement.LEFT  -> { undoMovement = Movement.RIGHT; xPivot -= 1 }
                Movement.RIGHT -> { undoMovement = Movement.LEFT;  xPivot += 1 }
            }

            val newIndex = yPivot * 4 + xPivot
            val oldValue = this.blocks[oldIndex]!!.value
            val newValue = this.blocks[newIndex]!!.value

            this.blocks[newIndex]!!.value = oldValue
            this.blocks[oldIndex]!!.value = newValue
        }
    }
}