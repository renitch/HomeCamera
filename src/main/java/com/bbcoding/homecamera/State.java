package com.bbcoding.homecamera;


public abstract class State {
	private CameraController cameraController;
	
	public State(CameraController cameraController) {
		this.cameraController = cameraController;
	}
	
	public abstract void connect();
	public abstract void disconnect();
}
