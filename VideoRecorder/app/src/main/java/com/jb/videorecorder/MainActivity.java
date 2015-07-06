package com.jb.videorecorder;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.FloatMath;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Button mCaptureButton;
    private Button mCheckButton;
    private Button mZoomX10Button;
    private Button mZoomMaxButton;
    private Button mUnZoomButton;
    private TextView mZoomSupported;
    private TextView mSmoothZoomSupported;
    private TextView mCurrentZoom;
    private TextView mMaxZoom;
    private Float mDist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button mCaptureButton = (Button)findViewById(R.id.button_capture);
        Button mCheckButton = (Button)findViewById(R.id.button_check);
        final Button mZoomX10Button = (Button)findViewById(R.id.button_zoomX10);
        final Button mZoomMaxButton = (Button)findViewById(R.id.button_zoommax);
        final Button mUnZoomButton = (Button)findViewById(R.id.button_unzoom);
        final TextView mZoomSupported = (TextView)findViewById(R.id.textview_zoomsupport);
        final TextView mSmoothZoomSupported = (TextView)findViewById(R.id.textview_smoothzoomsupport);
        final TextView mCurrentZoom = (TextView)findViewById(R.id.textview_currentzoom);
        final TextView mMaxZoom = (TextView)findViewById(R.id.textview_maxzoom);



        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(MainActivity.this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Camera.Parameters parameters = mCamera.getParameters();
                int action = MotionEventCompat.getActionMasked(event);

                if (event.getPointerCount() > 1) {
                    //handle multi-touch events
                    if(action == MotionEvent.ACTION_POINTER_DOWN){
                        mDist = getFingerSpacing(event);
                    } else if (action == MotionEvent.ACTION_MOVE && parameters.isZoomSupported()){
                        mCamera.cancelAutoFocus();
                        handleZoom(event, parameters);
                        Log.d("TAG", "Need to Zooom");
                        mCurrentZoom.setText("Current zoom: " + Integer.toString(parameters.getZoom()));
                    }
                } else {
                    //handle single touch events
                    if (action == MotionEvent.ACTION_UP) {
                        //handle focus
                        Log.d("TAG", "Need to Focus");
                    }
                }
                return true;
            }
        });


        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters parameters = mCamera.getParameters();
                if(parameters.isZoomSupported())
                {
                    mZoomSupported.setText(" Zoom supported: " + Boolean.toString(parameters.isZoomSupported()) + " ");
                    mSmoothZoomSupported.setText(" SmoothZoom supported: " + Boolean.toString(parameters.isSmoothZoomSupported()) + " ");
                    mCurrentZoom.setText("Current zoom: " + Integer.toString(parameters.getZoom()));
                    mMaxZoom.setText("Maximum zoom: " + Integer.toString(parameters.getMaxZoom()));
                    Log.d("is zoom supported", Boolean.toString(parameters.isZoomSupported()));
                    Log.d("is smoothzoom supported", Boolean.toString(parameters.isSmoothZoomSupported()));
                    Log.d("current zoom", Integer.toString(parameters.getZoom()));
                    Log.d("maximum zoom", Integer.toString(parameters.getMaxZoom()));
                }
            }
        });
        mZoomX10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setZoom(10);
                mCamera.setParameters(parameters);
                mCurrentZoom.setText("Current zoom: " + Integer.toString(parameters.getZoom()));
            }
        });

        mZoomMaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setZoom(parameters.getMaxZoom());
                mCamera.setParameters(parameters);
                mCurrentZoom.setText("Current zoom: " + Integer.toString(parameters.getZoom()));
            }
        });

        mUnZoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setZoom(0);
                mCamera.setParameters(parameters);
                mCurrentZoom.setText("Current zoom: " + Integer.toString(parameters.getZoom()));
            }
        });
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d("TAG", "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d("TAG", "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    private void handleZoom(MotionEvent event, Camera.Parameters parameters) {
        int maxZoom = parameters.getMaxZoom();
        int zoom = parameters.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        parameters.setZoom(zoom);
        mCamera.setParameters(parameters);

    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        Log.d("TAG: ", Float.toString(FloatMath.sqrt(x*x + y*y)));
        return FloatMath.sqrt(x*x + y*y);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
