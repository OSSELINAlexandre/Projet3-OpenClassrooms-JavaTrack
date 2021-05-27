package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		// Was getTime().getHours() for both, which lead to a duration of 0.
		long inHour = ticket.getInTime().getTime();
		long outHour = ticket.getOutTime().getTime();

		double duration = (outHour - inHour) / (1000.0 * 60.0 * 60.0); // Transforming to minutes
//		duration /= 60.00; // Transforming to hours

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			if (duration >= 0.5) {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
			} 
			else {
				ticket.setPrice(0);
			}
			break;
		}
		case BIKE: {
			if (duration >= 0.5) {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
			} else {
				ticket.setPrice(0);
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}
	}
}