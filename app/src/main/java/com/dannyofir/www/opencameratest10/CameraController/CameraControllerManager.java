package com.dannyofir.www.opencameratest10.CameraController;

public abstract class CameraControllerManager {
	public abstract int getNumberOfCameras();
	public abstract boolean isFrontFacing(int cameraId);
}
