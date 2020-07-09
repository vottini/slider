package br.com.egretta.slider

import android.content.res.Resources
import android.widget.ImageView

class Block(view: ImageView, resources : Resources) {
    private val resources : Resources = resources
    var value : Int = -1

    private val view : ImageView = view
    fun image() : ImageView {
        return this.view
    }

    fun refresh() {
        val images = this.resources.obtainTypedArray(R.array.block_images)
        val idx = images.getResourceId(this.value, -1)
        this.view.setImageResource(idx)
        images.recycle()
    }

}