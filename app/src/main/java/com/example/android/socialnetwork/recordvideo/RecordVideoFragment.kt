package com.example.android.socialnetwork.recordvideo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.filter.Filters
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordVideoFragment : Fragment() {

    private lateinit var cameraView: CameraView
    private lateinit var cameraShutter: View
    private lateinit var cameraRotate: View
    private lateinit var cameraOptions: View
    private lateinit var cameraFilters: View
    private lateinit var filtersList: View

    // camera filters
    private lateinit var grayscale: View
    private lateinit var hue: View
    private lateinit var blackAndWhite: View
    private lateinit var documentary: View
    private lateinit var temperature: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraView = view.findViewById(R.id.cameraView)
        cameraShutter = view.findViewById(R.id.cameraShutter)
        cameraRotate = view.findViewById(R.id.cameraRotate)
        cameraOptions = view.findViewById(R.id.cameraOptions)
        cameraFilters = view.findViewById(R.id.cameraFilters)
        filtersList = view.findViewById(R.id.filtersList)

        grayscale = view.findViewById(R.id.grayscale)
        hue = view.findViewById(R.id.hue)
        blackAndWhite = view.findViewById(R.id.blackAndWhite)
        documentary = view.findViewById(R.id.documentary)
        temperature = view.findViewById(R.id.temperature)

        setUpFilters()

        if (!allPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        if (allPermissionsGranted()) {
            setUpCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        cameraRotate.setOnClickListener { rotateCamera() }
        cameraFilters.setOnClickListener {
            filtersList.visibility = when (filtersList.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun setUpFilters() {
        grayscale.setOnClickListener {
            cameraView.filter = Filters.GRAYSCALE.newInstance()
            afterNewFilterSet()
        }
        hue.setOnClickListener {
            cameraView.filter = Filters.HUE.newInstance()
            afterNewFilterSet()
        }
        blackAndWhite.setOnClickListener {
            cameraView.filter = Filters.BLACK_AND_WHITE.newInstance()
            afterNewFilterSet()
        }
        documentary.setOnClickListener {
            cameraView.filter = Filters.DOCUMENTARY.newInstance()
            afterNewFilterSet()
        }
        temperature.setOnClickListener {
            cameraView.filter = Filters.TEMPERATURE.newInstance()
            afterNewFilterSet()
        }
    }

    private fun afterNewFilterSet() {
        filtersList.visibility = View.INVISIBLE
        cameraView.invalidate()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setUpCamera()
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

    private fun setUpCamera() {
        cameraView.apply {
            setLifecycleOwner(viewLifecycleOwner)
            addCameraListener(object : CameraListener() {
                override fun onVideoTaken(result: VideoResult) {
                    super.onVideoTaken(result)
                    val args = bundleOf(
                        "videoPath" to result.file.path,
                        "videoName" to result.file.name
                    )
                    findNavController().navigate(
                        R.id.action_recordVideoFragment_to_postFragment,
                        args
                    )
                }
            })
        }


        cameraShutter.setOnClickListener {
            if (cameraView.isTakingVideo) {
                cameraView.stopVideo()
                cameraOptions.visibility = View.VISIBLE
            } else {
                // Start recording the video
                val fileName: String = SimpleDateFormat(
                    FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".mp4"
                val videoFile = File(
                    getOutputDirectory(),
                    fileName
                )
                cameraView.takeVideoSnapshot(videoFile)
                cameraOptions.visibility = View.GONE
            }
        }
    }

    private fun rotateCamera() {
        cameraView.facing = when (cameraView.facing) {
            Facing.BACK -> Facing.FRONT
            else -> Facing.BACK
        }
        cameraView.invalidate()
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

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}