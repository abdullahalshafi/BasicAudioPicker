package com.shafi.basic_audio_picker.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shafi.basic_audio_picker.R
import com.shafi.basic_audio_picker.model.AudioUtilConfig
import com.shafi.basic_audio_picker.model.BasicAudioData
import com.shafi.basic_audio_picker.util.AudioUtilHelper.Companion.PACKAGE_NAME

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Created by Shafi on 17/07/2023.
 */
class AudioUtilActivity : AppCompatActivity() {

    private lateinit var config: AudioUtilConfig

    private var bottomSheet: BottomSheetDialog? = null
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    private var recordTextTv: TextView? = null
    private var recordTimerCm: Chronometer? = null
    private var startRecordIv: ImageView? = null
    private var stopRecordIv: ImageView? = null
    private var deleteIv: ImageView? = null

    private var callingPackageName: String? = null

    private var audioName: String? = null
    private var audioPath: String? = null
    private var audioUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callingPackageName = intent.getStringExtra(PACKAGE_NAME)
        config =
            intent.getSerializableExtra(AudioUtilConfig::class.java.simpleName) as AudioUtilConfig

        if (config.isAudioPick) {

            audioPickResultLauncher.launch("audio/*")
        } else if (config.isRecord) {

            BottomSheetDialog(this, R.style.BottomSheetDialog).apply {
                setContentView(R.layout.audio_record_bottom_sheet)
                recordTextTv = findViewById(R.id.record_text_tv)
                recordTimerCm = findViewById(R.id.record_timer_cm)
                startRecordIv = findViewById(R.id.start_record_audio_iv)
                stopRecordIv = findViewById(R.id.stop_record_audio_iv)
                deleteIv = findViewById(R.id.delete_iv)

                setCancelable(false)
                show()
                bottomSheet = this
            }

            //start recording
            startRecordIv?.setOnClickListener {
                if (checkMicPermission()) {
                    startRecording()
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            //stop recording
            stopRecordIv?.setOnClickListener {
                stopRecorder()
            }

            //delete
            deleteIv?.setOnClickListener {
                resetRecorder()
                bottomSheet?.dismiss()
                sendResultCanceledAndFinish(false)
            }
        }
    }

    private val audioPickResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                audioUri = uri
                copyPickedAudioFileToInternalStorage()
            }else{
                sendResultCanceledAndFinish(false)
            }
        }

    private fun checkMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { micPermissionGranted: Boolean? ->

            if (micPermissionGranted == true) {
                startRecording()
            } else {
                requestMicPermission()
            }
        }

    private fun requestMicPermission() {

        when {

            checkMicPermission() -> {
                startRecording()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionAlert(
                    getString(R.string.record_audio_permission),
                    getString(R.string.you_can_not_record_audio_unless_you_give_permission),
                    getString(R.string.please_give_record_audio_permission)
                ) { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            }

            else -> {
                showPermissionAlert(
                    getString(R.string.record_audio_permission),
                    getString(R.string.you_can_not_record_audio_unless_you_give_permission),
                    getString(R.string.please_give_record_audio_permission)
                ) {
                    finish()
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        callingPackageName, null
                    )
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }
    }

    private fun createMediaRecorder(): MediaRecorder {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else MediaRecorder()
    }

    private fun createEmptyAudioFile() {
        audioFile = File(cacheDir, "${UUID.randomUUID()}.mp3")
    }

    private fun startRecording() {

        //update ui
        recordTextTv?.text = getString(R.string.recording)
        recordTimerCm?.base = SystemClock.elapsedRealtime()
        recordTimerCm?.visibility = View.VISIBLE
        recordTimerCm?.start()
        startRecordIv?.visibility = View.GONE
        stopRecordIv?.visibility = View.VISIBLE

        createEmptyAudioFile()
        createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(audioFile).fd)

            prepare()
            start()
            recorder = this
        }
    }

    private fun stopRecorder() {

        //update ui
        recordTextTv?.text = getString(R.string.tap_to_record)
        recordTimerCm?.visibility = View.GONE
        recordTimerCm?.stop()
        stopRecordIv?.visibility = View.GONE
        startRecordIv?.visibility = View.VISIBLE

        resetRecorder()
        bottomSheet?.dismiss()

        if (audioFile != null) {
            audioName = audioFile!!.name
            audioPath = audioFile!!.absolutePath
            audioUri = audioFile!!.toUri()
            sendResultOkAndFinish()
        } else {
            sendResultCanceledAndFinish(false)
        }
    }

    private fun resetRecorder() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }

    //send result to activity for image
    private fun sendResultOkAndFinish() {
        if (audioName != null && audioPath != null && audioUri != null) {

            val basicAudioData = BasicAudioData(audioName!!, audioPath!!, audioUri.toString())
            val intent = Intent()
            setResult(
                Activity.RESULT_OK,
                intent.putExtra(BasicAudioData::class.java.simpleName, basicAudioData)
            )
            finish()
        } else {
            sendResultCanceledAndFinish(true)
        }
    }

    //some error occurred
    private fun sendResultCanceledAndFinish(
        showToast: Boolean,
        message: String = getString(R.string.something_went_wrong_please_try_again)
    ) {
        if (showToast) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT)
                .show()
        }
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun copyPickedAudioFileToInternalStorage() {

        createEmptyAudioFile()
        try {
            FileOutputStream(audioFile).use { outputStream ->
                contentResolver.openInputStream(audioUri!!)?.use { input ->
                    input.copyTo(outputStream)
                }
            }
            audioName = audioFile!!.name
            audioPath = audioFile!!.absolutePath
            audioUri = audioFile!!.toUri()
            sendResultOkAndFinish()
        } catch (ex: IOException) {
            ex.printStackTrace()
            sendResultCanceledAndFinish(true)
        }

    }

    private fun showPermissionAlert(
        title: String,
        message: String,
        cancelMessage: String,
        function: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
                function.invoke()
                dialogInterface.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                //endResultCanceledAndFinish(true, cancelMessage)
            }
            .setCancelable(false)
            .show()
    }
}