package com.hd.Hawkeye

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceView
import android.view.WindowManager
import org.opencv.android.*
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.*


class OpenCvActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2{
    private var mOpenCvCameraView: CameraBridgeViewBase? = null

    companion object {
        private val TAG = OpenCvActivity::class.java.simpleName
    }


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView?.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_open_cv)
        mOpenCvCameraView = findViewById(R.id.cameraView)
        mOpenCvCameraView?.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView?.setCvCameraViewListener(this)

    }


    override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase?>? {
        return Collections.singletonList(mOpenCvCameraView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView?.disableView()
    }



    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        //获取到显示Mat赋值给frame
        val frame = inputFrame.rgba()
        //判断横竖屏用于进行图像的旋转
        if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //判断是前置摄像头还是后置摄像头,翻转的角度不一样
            when (mOpenCvCameraView?.cameraIndex) {
                CameraBridgeViewBase.CAMERA_ID_FRONT->{
                    Core.rotate(frame, frame, Core.ROTATE_90_COUNTERCLOCKWISE)
                    Core.flip(frame, frame, 1);
                }
                CameraBridgeViewBase.CAMERA_ID_BACK->{
                    Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE)
                }else ->{
                    Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE)
                }
            }
            //把旋转后的Mat图像根据摄像头屏幕的大小进行缩放
            var size = Size(mOpenCvCameraView?.width!!.toDouble(), mOpenCvCameraView?.height!!.toDouble())
            Imgproc.resize(frame, frame, size,0.0,1.2)
        }
        return frame
    }
}