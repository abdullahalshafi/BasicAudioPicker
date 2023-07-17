package com.shafi.basicaudiopicker

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import com.shafi.basic_audio_picker.model.BasicAudioData
import com.shafi.basic_audio_picker.util.AudioUtilHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //record audio
        findViewById<Button>(R.id.record_btn).setOnClickListener {
            AudioUtilHelper.create(this, audioLauncher) {
                recordAudio()
                start()
            }
        }

        //pick audio
        findViewById<Button>(R.id.pick_btn).setOnClickListener {
            AudioUtilHelper.create(this, audioLauncher) {
                pickAudio()
                start()
            }
        }
    }

    private val audioLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {

                val basicAudioData: BasicAudioData =
                    it.data!!.getSerializableExtra(BasicAudioData::class.java.simpleName) as BasicAudioData

                //do stuffs with the image object
                Log.d("AUDIO_DATA", "name: ${basicAudioData.name} path: ${basicAudioData.path}")

            } else if (it.resultCode == Activity.RESULT_CANCELED) {
                //handle your own situation
            }
        }
}