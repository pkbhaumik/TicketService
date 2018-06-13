package pb.ticketing.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SeatHold implements Serializable {

	/**
	 * Seat hold information.
	 * @author Pralay Bhaumik
	 */
	private static final long serialVersionUID = 1L;
	
	private final int seatHoldId;
	private final int numSeats;
	private final long expirationTime;
	
	private List<SeatInformation> seatshold;
	
	public SeatHold(int seatHoldId, int numSeats, long expirationTime) {
		this.seatHoldId = seatHoldId;
		this.numSeats  = numSeats;
		this.expirationTime = expirationTime;
		this.seatshold = new ArrayList<SeatInformation>();
	}
	
	
	public int getSeatHoldId() {
		return this.seatHoldId;
	}
	
	public List<SeatInformation> getSeatsHold() {
		List<SeatInformation> seats = new ArrayList<SeatInformation>(); 
		seats.addAll(this.seatshold);
		
		return seats;
	}
	
	
	public void setSeatsHold(List<SeatInformation> seatsHold) {
		this.seatshold.clear();
		
		if(seatsHold == null || seatsHold.size() == 0)
			return;
		this.seatshold.addAll(seatsHold);
	}
	
	public void addSeatHold(SeatInformation seatInfo) {
		this.seatshold.add(seatInfo);
	}
	
	public int getNumSeats() {
		return this.numSeats;
	}
	
	public long getExpiringAt() {
		return this.expirationTime;
	}
}
