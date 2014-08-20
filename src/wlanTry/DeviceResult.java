package wlanTry;

public class DeviceResult {
	int packetTx,packetRx,packetTxFails,packetRxFails,access; //For calculation of throughput
	double sumDelay;  //For calculation of delay time
	final int timeLength;
	public DeviceResult(){
		this.access=0;
		this.packetRx=0;
		this.packetTx=0;
		this.packetTxFails=0;
		this.packetRxFails=0;
		this.sumDelay=0;
		this.timeLength=Param.simTimeLength;
	}
	public double getThroughputTx(){
		/*System.out.println(
		costTime+
		": MT "+this.id+
		" Tx "+(double)this.packetTx*12000.0/((double)time/1000000.0)/1000000.0+
		", Rx "+(double)this.packetRx*12000.0/((double)time/1000000.0)/1000000.0
		);*/
		return (double)this.packetTx*Param.sizeData/((double)timeLength);
	}

	public double getThroughputRx(){
		return (double)this.packetRx*Param.sizeData/((double)timeLength);
	}
	public double getPacketRxFails(){
		return this.packetRxFails;
	}
	public double getPacketTxFails(){
		return this.packetTxFails;
	}
	public double getPacketRx(){
		return this.packetRx;
	}
	public double getPacketTx(){
		return this.packetTx;
	}
	
	public double getDelayTime(){
		if (packetTx<1e-6)
			return 0;
		else
			return (double)this.sumDelay/(double)packetTx;
	}
	public void accessChannel(){
	}
	public void receiveACK(){
	}
	public void receiveNACK(){
	}
	public void receiveDATA(){
	}
	public void transmittingStart(){
		
	}
	public void retransmit(){
	}
	public void reply(PacketType type){
	}

	
	
}
