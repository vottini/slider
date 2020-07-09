package br.com.egretta.slider

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

const val SAVED = "br.com.egretta.slider.SAVED"
const val CONTINUE = "br.com.egretta.slider.CONTINUE"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val prefs = this.getSharedPreferences(SAVED, Context.MODE_PRIVATE)
        val completed = prefs.getBoolean("completed", true)
        findViewById <Button> (R.id.continue_button).apply {
            visibility = if (completed) View.GONE else View.VISIBLE
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun newGame(view: View) {
        val prefs = this.getSharedPreferences(SAVED, Context.MODE_PRIVATE)
        val resetBlinker = prefs.edit()
        resetBlinker.putBoolean("blinked", false)
        resetBlinker.apply()

        val intent = Intent(this, Playground::class.java)
        intent.putExtra(CONTINUE, false)
        this.startActivity(intent)
    }

    @Suppress("UNUSED_PARAMETER")
    fun continueGame(view: View) {
        val intent = Intent(this, Playground::class.java)
        intent.putExtra(CONTINUE, true)
        this.startActivity(intent)
    }

    @Suppress("UNUSED_PARAMETER")
    fun seePrivacy(view: View) {
        val intent = Intent(this, Policy::class.java)
        this.startActivity(intent)
    }
}
