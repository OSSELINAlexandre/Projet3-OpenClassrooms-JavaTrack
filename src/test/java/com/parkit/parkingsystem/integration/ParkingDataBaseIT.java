package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		int currentSpot = ticketDAO.getTicket("ABCDEF").getParkingSpot().getId();
		int nextSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

		assertTrue(ticketDAO.getTicket("ABCDEF").getInTime() != null);
		assertNotEquals(currentSpot, nextSpot);
		// TODO: check that a ticket is actualy saved in DB and Parking table is updated
		// with availability
	}

	// These tests aren't independant.
	@Test
	public void testParkingLotExit() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		// Necessary sleep so that the system understand.
		try {
			Thread.sleep(1000);
		} catch (Exception E) {

		}

		parkingService.processExitingVehicle();

		assertTrue(ticketDAO.getTicket("ABCDEF").getOutTime() != null);
		assertEquals(ticketDAO.getTicket("ABCDEF").getPrice(), 0);
		// TODO: check that the fare generated and out time are populated correctly in
		// the database
	}

	@Test
	public void testParkingLotShouldGetA5PercentDiscountIfCameMoreThanOnce() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		Long ended = System.currentTimeMillis() + (1000 * 45 * 60);
		Long started = System.currentTimeMillis();

		parkingService.processIncomingVehicle(started);

		parkingService.processExitingVehicle(ended);

		Double priceB = ticketDAO.getTicket("ABCDEF").getPrice();

		parkingService.processIncomingVehicle(started + (1000 * 60 * 47));

		parkingService.processExitingVehicle(ended + (1000 * 60 * 47));

		
		Double priceA = ticketDAO.getTicket("ABCDEF").getPrice();

		System.out.println("Debut : " + priceA + " || End : " + priceB);
		assertNotEquals(priceB, priceA);
		assertTrue((priceB * 0.95 == priceA));

	}

}
