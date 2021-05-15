package org.cocos2dx.lib;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SKCocos2dxCamera extends Activity implements SurfaceHolder.Callback, View.OnClickListener,
        Camera.PictureCallback, Camera.AutoFocusCallback
{
    private Camera camera = null;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private Button shotBtn;
	private int isFrontFace;
	private static final String TAG = "Fuck Android";
	private boolean shotPressed = false;
	private boolean surfaceIsCreated = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	
        super.onCreate(savedInstanceState);
		shotPressed = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        preview = (SurfaceView) findViewById(R.id.SurfaceView01);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        shotBtn = (Button) findViewById(R.id.Button01);
        shotBtn.setText("Shot");
        shotBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
		super.onResume();
		open_camera();
    }

	protected void open_camera()
	{	
		if (null == camera)
		{
	        try
	        {
				camera = Camera.open(0);
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo( 0, cameraInfo );
		        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) 
				{
					isFrontFace = 1;
		        }
				else
				{
					isFrontFace = 0;
				}
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
			
		}
		
		if (null == camera)
		{
	        try
	        {
				camera = Camera.open(1); // let us use super-power and secret knowledge
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo( 1, cameraInfo );
		        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) 
				{
					isFrontFace = 1;
		        }
				else
				{
					isFrontFace = 0;
				}
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
		}
		if (null == camera)
		{
			Log.e(TAG, "Can not open camera on this device!");
		}
	}

	protected void kill_camera()
	{
		synchronized (this) 
		{
			if (camera != null)
			{
				camera.setPreviewCallback(null);	
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}	
	}

    @Override
    protected void onPause()
    {
	    super.onPause();
		kill_camera();

    }

	@Override
	protected void onDestroy()
	{
		kill_camera();
	    super.onDestroy();	    
	}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
		synchronized(this)
		{
			if (!surfaceIsCreated)
			{
				surfaceIsCreated = true;
				start_preview();
			}
			else
			{
				camera.setPreviewCallback(null);	
				camera.stopPreview();
				
				start_preview();
			}
		}
    }


	void display_supported_picture_sizes()
	{
		if (null == camera)
		{
			return;
		}
		List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
		for (int i = 0; i < sizes.size(); ++i)
		{
			Camera.Size s = sizes.get(i);
			Log.e(TAG, i + " Supported format " + s.width + "x" + s.height);
		}
	}
	
	Camera.Size get_nearest_supported_picture_size()
	{
		if (null == camera)
		{
			return null;
		}
		List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
		
		Size previewSize = camera.getParameters().getPreviewSize();
		
		int w = preview.getWidth();
        int h = preview.getHeight();
		
		for (int i = 0; i < sizes.size(); ++i)
		{
			Camera.Size s = sizes.get(i);
			if (w == s.width && h == s.height)
			{
				return s;
			}
			//Log.e(TAG, i + " Supported format " + s.width + "x" + s.height);
		}
		int ind = 0;
		for (int i = 1; i < sizes.size(); ++i)
		{
			Camera.Size s = sizes.get(i);
			Camera.Size min = sizes.get(ind);
			if (s.width < min.width)
			{
				ind = i;
			}
		}
		return sizes.get(ind);
	}
	
	void start_preview()
	{
		try
        {
	
			open_camera();
			display_supported_picture_sizes();
            camera.setPreviewDisplay(surfaceHolder);

	        Size previewSize = camera.getParameters().getPreviewSize();
	        float aspect = (float) previewSize.width / previewSize.height;

	        int previewSurfaceWidth = preview.getWidth();
	        int previewSurfaceHeight = preview.getHeight();

	        LayoutParams lp = preview.getLayoutParams();

			int d = 0;
	        if(android.os.Build.VERSION.SDK_INT > 13 && isFrontFace != 0)
	        {
				d = 180;
	        }
	        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
	        {
	            camera.setDisplayOrientation(90 + d);
	            lp.height = previewSurfaceHeight;
	            lp.width = (int) (previewSurfaceHeight / aspect);
	        }
	        else
	        {
	            camera.setDisplayOrientation(0 + d);
	            lp.width = previewSurfaceWidth;
	            lp.height = (int) (previewSurfaceWidth / aspect);
	        }
	        preview.setLayoutParams(lp);

	
			Camera.Size picture_size = get_nearest_supported_picture_size();
			Log.e(TAG, "Selected picture size is " + picture_size.width + "x" + picture_size.height);
			Camera.Parameters p = camera.getParameters();
			p.setPictureSize(picture_size.width, picture_size.height);
			camera.setParameters(p);
			
			camera.startPreview();
			//Camera.Size size = camera.getParameters().getPictureSize();
			//
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
	
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
		kill_camera();
    }

    @Override
    public void onClick(View v)
    {
        if (v == shotBtn)
        {
			if (shotPressed)
			{
				return;
			}
			shotPressed = true;			
			if (camera != null)
			{
				 camera.autoFocus(this);
			}
			else
			{
				Log.e(TAG, "Camera is null, can not take picture!");
			}
        }
    }

    @Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera)
    {		
		Log.e(TAG, "onPictureTaken");
		Intent data = new Intent();

		Bundle params = new Bundle();
		params.putInt("frontcamera", isFrontFace);
		data.putExtra("photo", paramArrayOfByte);
		data.putExtra("params", params);

		if (getParent() == null) 
		{
			Log.e(TAG, "if (getParent() == null)");
			this.setResult(Activity.RESULT_OK, data);
			finish();
		} 
		else 
		{
			Log.e(TAG, "getParent().setResult(Activity.RESULT_OK, data);");
			getParent().setResult(Activity.RESULT_OK, data);
			getParent().finishFromChild(this);
		}
    }

	@Override
	public void onAutoFocus(boolean paramBoolean, Camera paramCamera)
	{
		Log.e(TAG, "onAutoFocus");
		// ignoring autofocus success at the moment
		paramCamera.setPreviewCallback(null);
		paramCamera.takePicture(null, null, null, this);
    }
}
