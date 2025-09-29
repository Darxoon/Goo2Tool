package com.crazine.goo2tool.gamefiles.ball;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ball {
    
    public static record ImageIdInfo(String imageId) {}
    
    private String name = "";
    // ...
    
    // detonateParticleEffect
    // material
    // popSoundId
    // deathParticleEffect
    // fireworksParticleEffect
    // trailEffectId
    // trailParticleEffect
    
    
    private ImageIdInfo strandImageId;
    private ImageIdInfo strandInactiveImageId;
    private ImageIdInfo strandInactiveOverlayImageId;
    // ...
    
    private ImageIdInfo strandBurntImageId;
    private ImageIdInfo strandBackgroundImageId;
    private ImageIdInfo detachStrandImageId;
    // ...
    
    private ImageIdInfo dragMarkerImageId;
    private ImageIdInfo detachMarkerImageId;
    // ...
    
    private List<JsonNode> ballParts = new ArrayList<>();
    // ...
    
    // splatImageIds
    // soundEvents
    // particleEffects

    private ImageIdInfo thrusterStableFluidsImage;
    // ...
    
    private ImageIdInfo laserOverrideImage;
	
	// Unique to FistyLoader
    private ImageIdInfo editorButtonImage;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public ImageIdInfo getStrandImageId() {
		return strandImageId;
	}
	public void setStrandImageId(ImageIdInfo strandImageId) {
		this.strandImageId = strandImageId;
	}

	public ImageIdInfo getStrandInactiveImageId() {
		return strandInactiveImageId;
	}
	public void setStrandInactiveImageId(ImageIdInfo strandInactiveImageId) {
		this.strandInactiveImageId = strandInactiveImageId;
	}

	public ImageIdInfo getStrandInactiveOverlayImageId() {
		return strandInactiveOverlayImageId;
	}
	public void setStrandInactiveOverlayImageId(ImageIdInfo strandInactiveOverlayImageId) {
		this.strandInactiveOverlayImageId = strandInactiveOverlayImageId;
	}

	public ImageIdInfo getStrandBurntImageId() {
		return strandBurntImageId;
	}
	public void setStrandBurntImageId(ImageIdInfo strandBurntImageId) {
		this.strandBurntImageId = strandBurntImageId;
	}

	public ImageIdInfo getStrandBackgroundImageId() {
		return strandBackgroundImageId;
	}
	public void setStrandBackgroundImageId(ImageIdInfo strandBackgroundImageId) {
		this.strandBackgroundImageId = strandBackgroundImageId;
	}

	public ImageIdInfo getDetachStrandImageId() {
		return detachStrandImageId;
	}
	public void setDetachStrandImageId(ImageIdInfo detachStrandImageId) {
		this.detachStrandImageId = detachStrandImageId;
	}

	public ImageIdInfo getDragMarkerImageId() {
		return dragMarkerImageId;
	}
	public void setDragMarkerImageId(ImageIdInfo dragMarkerImageId) {
		this.dragMarkerImageId = dragMarkerImageId;
	}

	public ImageIdInfo getDetachMarkerImageId() {
		return detachMarkerImageId;
	}
	public void setDetachMarkerImageId(ImageIdInfo detachMarkerImageId) {
		this.detachMarkerImageId = detachMarkerImageId;
	}

	public List<JsonNode> getBallParts() {
		return ballParts;
	}
	public void setBallParts(List<JsonNode> ballParts) {
		this.ballParts = ballParts;
	}

	public ImageIdInfo getThrusterStableFluidsImage() {
		return thrusterStableFluidsImage;
	}
	public void setThrusterStableFluidsImage(ImageIdInfo thrusterStableFluidsImage) {
		this.thrusterStableFluidsImage = thrusterStableFluidsImage;
	}

	public ImageIdInfo getLaserOverrideImage() {
		return laserOverrideImage;
	}
	public void setLaserOverrideImage(ImageIdInfo laserOverrideImage) {
		this.laserOverrideImage = laserOverrideImage;
	}
	
	public ImageIdInfo getEditorButtonImage() {
		return editorButtonImage;
	}
	public void setEditorButtonImage(ImageIdInfo editorButtonImage) {
		this.editorButtonImage = editorButtonImage;
	}
    
}
