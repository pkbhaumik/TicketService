package pb.ticketing.service.model;

import java.io.Serializable;

public class SeatInformation implements Serializable {

	/**
	 * Information for a seat.
	 * @author Pralay Bhaumik
	 */
	private static final long serialVersionUID = 1L;
	
	private final int seatId;
	private final int levelId;
	private final int rowNumber;
	private final int seatNumber;
	
	public SeatInformation(int seatId, int levelId, int rowNumber, int seatNumber) {
		this.seatId = seatId;
		this.levelId = levelId;
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getSeatId() {
		return seatId;
	}

	public int getLevelId() {
		return levelId;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public int getSeatNumber() {
		return seatNumber;
	}
}
