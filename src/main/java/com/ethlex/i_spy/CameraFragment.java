package com.ethlex.i_spy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback{

    Camera camera;
    Camera.PictureCallback jpegCallback;

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;

    final int CAMERA_REQUEST_CODE = 1;

    public static CameraFragment newInstance(){
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera , container, false);

        mSurfaceView = view.findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();

        if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[] {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }else{
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }


        Button mLogout = view.findViewById(R.id.logout);
        Button mCapture = view.findViewById(R.id.capture);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOut();
            }
        });

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });


        jpegCallback = new Camera.PictureCallback(){
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Intent intent = new Intent(getActivity(), ShowCaptureActivity.class);
                intent.putExtra("capture", data);
                startActivity(intent);
                return;
            }
        };

        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera = Camera.open();

        Camera.Parameters parameters;
        parameters  = camera.getParameters();

        camera.setDisplayOrientation(90);
        parameters.setPreviewFrameRate(30);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        int len = sizeList.size();
        for(int i = 0; i<len; ++i){
            if( (sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width*bestSize.height))
                bestSize = sizeList.get(i);
        }
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mSurfaceHolder.addCallback(this);
                    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }else{
                    Toast.makeText(getContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void LogOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getContext(), SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return;
    }

    private void captureImage(){
        camera.takePicture(null, null, jpegCallback);
    }
}
