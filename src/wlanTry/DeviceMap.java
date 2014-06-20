package wlanTry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class Location {
	public double x;
	public double y;
	public Location(double x, double y){
		this.x=x;
		this.y=y;
	}
}

abstract class DeviceMap {
	final double distAP=Param.distAP;
	final double areaAP=Param.areaAP;
	public final int carrierSenseRange=Param.carrierSenseRange;
	protected ArrayList<Location> devices;
	//private ArrayList<Location> accessPoints; 

	/**
	 * The distance of carrier sense.
	 */
	public DeviceMap (){
		devices=new ArrayList<Location>();
		//accessPoints=new ArrayList<Location>();
	}
	protected void addDevice(double x, double y){
		//DebugOutput.output(this.devices.size()+": "+x+" "+y);
		devices.add(new Location(x,y));
	}
	/*
	public void addAccessPoints(double x, double y){
		accessPoints.add(new Location(x,y));
	}*/
	public ArrayList<Integer> getNeighbour(int a){
		ArrayList<Integer> ret=new ArrayList<Integer>();
		double x0=devices.get(a).x;
		double y0=devices.get(a).y;
		for (int i=0;i<devices.size();i++){
			if (i==a) continue; 
			double dx=devices.get(i).x-x0;
			double dy=devices.get(i).y-y0;
			if (Math.sqrt(dx*dx+dy*dy)<carrierSenseRange)
				ret.add(i);
		}
		DebugOutput.outputNeighbor(a, ret);
		return ret;
	}
	/**
	 * Create a map with square;
	 * @param n
	 */
	public abstract void createMap(int n);
	public abstract int getAPofIndex(int index);
	protected abstract boolean inAreaAP(double x,double y);
	public int getDeviceNum(){
		return devices.size();
	}
	public Set<Integer> getCenter(){
		Set<Integer> ret=new HashSet<Integer>();
		double size=distAP/2;
		for (int i=0;i<devices.size();i++){
			if (Math.abs(devices.get(i).x)<size && Math.abs(devices.get(i).y)<size)
				ret.add(i);
		}
		return ret;
	}

}
