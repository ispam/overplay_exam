package com.example.overplay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.overplay.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.Util
import java.lang.Math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()

    private var exoPlayer: ExoPlayer? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private var audioManager: AudioManager? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        viewModel.isRightGesture.observe(this) { isRightGesture ->
            if (isRightGesture) {
                showToast(getString(R.string.increase_volume))
                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            } else {
                showToast(getString(R.string.decrease_volume))
                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            }
        }
        viewModel.isUpwardGesture.observe(this) { isUpwardGesture ->
            if (isUpwardGesture) {
                showToast(getString(R.string.move_forward))
                exoPlayer?.seekTo(exoPlayer?.currentPosition?.plus(5000) ?: 0)
            } else {
                showToast(getString(R.string.rewind))
                exoPlayer?.seekTo(exoPlayer?.currentPosition?.minus(5000) ?: 0)
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        lastAcceleration = currentAcceleration

        // current accelerations with the help of fetched x,y,z values
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta: Float = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta

        if (acceleration > 12) {
            showToast(getString(R.string.pause))
            exoPlayer?.pause()
            return
        }

        // returns the theta tangent of the rectangular coordinates of X and Y or Z
        val xAngle = Math.atan2(x.toDouble(), y.toDouble()) / (Math.PI / 180)
        val zAngle = Math.atan2(z.toDouble(), y.toDouble()) / (Math.PI / 180)

        if (xAngle in -100F..-35F) {
            viewModel.isRightGestureDebounced(true)
        }

        if (xAngle in 35F..100F) {
            viewModel.isRightGestureDebounced(false)
        }

        if (zAngle in -100F..-35F) {
            viewModel.isUpwardGestureDebounced(true)
        }

        if (zAngle in 35F..100F) {
            viewModel.isUpwardGestureDebounced(false)
        }

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No-op
    }

    override fun onBackPressed() {
        super.onBackPressed()
        sensorManager?.unregisterListener(this)
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            this, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ), SensorManager.SENSOR_DELAY_NORMAL
        )
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.exoPlayerView.player = exoPlayer
                exoPlayer.setMediaItem(MediaItem.fromUri(VIDEO_URL))
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        exoPlayer?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            release()
        }
        exoPlayer = null
    }

    companion object {
        private const val VIDEO_URL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
    }
}