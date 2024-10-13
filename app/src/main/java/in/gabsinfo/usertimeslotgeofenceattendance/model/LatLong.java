package in.gabsinfo.usertimeslotgeofenceattendance.model;

import java.io.Serializable;

/**
 * Created by Sir on 7/29/2015.
 */
public class LatLong implements Serializable {

	private double lat;
	private double lng;
	private float radius;
	private String branchCode;
	private String branchName;

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
}
