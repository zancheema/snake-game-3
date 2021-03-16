package com.example.android.socialnetwork.recordvideo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecordVideoFragment : Fragment() {

    private var videoCapture: VideoCapture? = null

    private var videoIsRecorded = false
    private var frontCameraEnabled = true

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var startStopRecording: View
    private lateinit var cameraRotate: View
    private lateinit var cameraOptions: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewFinder = view.findViewById(R.id.viewFinder)
        startStopRecording = view.findViewById(R.id.startCamera)
        cameraRotate = view.findViewById(R.id.cameraRotate)
        cameraOptions = view.findViewById(R.id.cameraOptions)

        if (allPermissionsGranted()) {
            startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        startStopRecording.setOnClickListener { startOrStopRecording() }
        cameraRotate.setOnClickListener { rotateCamera() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun rotateCamera() {
        frontCameraEnabled = if (frontCameraEnabled) {
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            false
        } else {
            startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            true
        }
    }

    @SuppressLint("RestrictedApi")
    private fun startOrStopRecording() {
        // Stop recording if its already being done
        if (videoIsRecorded) {
            videoCapture?.stopRecording()
            cameraOptions.visibility = View.VISIBLE
            videoIsRecorded = false
            return
        }

        // Start recording the video
        val fileName: String = SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".mp4"
        val videoFile = File(
            outputDirectory,
            fileName
        )

        videoCapture?.startRecording(
            videoFile,
            ContextCompat.getMainExecutor(context),
            object : VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(file: File) {
                    val savedUri = Uri.fromFile(file)
                    val args = bundleOf(
                        "videoPath" to savedUri.path,
                        "videoName" to fileName
                    )
                    findNavController().navigate(
                        R.id.action_recordVideoFragment_to_postFragment,
                        args
                    )
                }

                override fun onError(videoCaptureError: Int, message: String, exc: Throwable?) {
                    Log.e(TAG, "Video recording failed: $message", exc)
                }
            }
        )
        cameraOptions.visibility = View.GONE
        videoIsRecorded = true
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }


            videoCapture = VideoCapture.Builder().build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}