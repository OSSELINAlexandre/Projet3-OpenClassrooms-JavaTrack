package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * <b>classe Service de l'application parking System.</b>
 * <p>
 * Parking Service permet le traitement métier des données. La classe permet
 * entre autre l'entrée et la sortie des véhicules des clients.
 * </p>
 * 
 * @see parkingService
 * 
 * @author Alexandre OSSELIN
 * @version 1.0
 */

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;
    private String regNumber;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
	this.inputReaderUtil = inputReaderUtil;
	this.parkingSpotDAO = parkingSpotDAO;
	this.ticketDAO = ticketDAO;
    }

    /**
     * processIncomingVehicle permet de trouver une place libre pour le client et de
     * créer le ticket d'entrée pour celui-ci.
     * 
     * A la fin de ce processus, la base de donnée à mis à jour les disponibilité de
     * parking. Un ticket avec l'heure d'entrée et le numéro d'imatriculation du
     * client a été crée.
     * 
     * @see TicketDAO
     * @see ParkingSpotDAO
     */
    public void processIncomingVehicle() {
	try {
	    ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
	    if (parkingSpot != null && parkingSpot.getId() > 0) {
		String vehicleRegNumber = getVehichleRegNumber();
		parkingSpot.setAvailable(false);
		parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
							  // false

		Date inTime = new Date();
		Ticket ticket = new Ticket();
		// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
		// ticket.setId(ticketID);
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber(vehicleRegNumber);
		ticket.setPrice(0);
		if (ticket.getInTime() == null)
		    ticket.setInTime(inTime);
		ticket.setOutTime(null);
		if (ticketDAO.checkCustomerProgram(vehicleRegNumber)) 
		    System.out.println("Welcome backdear customer ! a 5% discount would be applied !");
		ticketDAO.saveTicket(ticket);
		System.out.println("Generated Ticket and saved in DB");
		System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
		System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
	    }
	} catch (Exception e) {
	    logger.error("Unable to process incoming vehicle", e);
	}
    }

    /**
     * getVehichleRegNumber permet de récupérer le numéro d'immatriculation du
     * client. La méthode enregistre le numéro d'immatriculation dans un attribut de
     * classe.
     * 
     * @return regNumber
     */
    private String getVehichleRegNumber() throws Exception {
	System.out.println("Please type the vehicle registration number and press enter key");
	regNumber = inputReaderUtil.readVehicleRegistrationNumber();
	return regNumber;
    }

    /**
     * getNextParkingNumberIfAvailable vérifie la disponibilité du parking depuis la
     * base de donnée. Cette méthode permet d'obtenir le numéro d'identification
     * d'une place de parking libre, si elle existe. Si plusieurs places de parking
     * sont disponibles, la méthode renverra la place de parking dont l'identifiant
     * est le plus petit.
     * 
     * 
     * @throws Exception
     * @return parkingSpot
     */
    public ParkingSpot getNextParkingNumberIfAvailable() {
	int parkingNumber = 0;
	ParkingSpot parkingSpot = null;
	try {
	    ParkingType parkingType = getVehichleType();
	    parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
	    if (parkingNumber > 0) {
		parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
	    } else {
		throw new Exception("Error fetching parking number from DB. Parking slots might be full");
	    }
	} catch (IllegalArgumentException ie) {
	    logger.error("Error parsing user input for type of vehicle", ie);
	} catch (Exception e) {
	    logger.error("Error fetching next available parking slot", e);
	}
	return parkingSpot;
    }

//TODO faire un système différent de paiement si erreur au niveau du véhicule.	
    /**
     * getVehichleType demande à l'utilisateur le type de véhicule qu'il souhaite
     * garer. En fonction de sa réponse, un ENUM sera renvoyé de type CAR ou BIKE.
     * Si sa réponse ne correspond pas au choix proposés; alors une erreur sera
     * renvoyée.
     * 
     * @throws Exception
     * @return ParkingType
     */
    private ParkingType getVehichleType() {
	System.out.println("Please select vehicle type from menu");
	System.out.println("1 CAR");
	System.out.println("2 BIKE");
	int input = inputReaderUtil.readSelection();
	switch (input) {
	case 1: {
	    return ParkingType.CAR;
	}
	case 2: {
	    return ParkingType.BIKE;
	}
	default: {
	    System.out.println("Incorrect input provided");
	    throw new IllegalArgumentException("Entered input is invalid");
	}
	}
    }

    /**
     * processExitingVehicle permet au client de payer son ticket. La méthode permet
     * de générer un tarif en fonction de l'heure d'arrivée et de départ du client.
     * Cette méthode met également à jour la base de donnée du parking et du ticket.
     * Elle communique avec le client pour l'informer des différentes données
     * émanant de la trasaction. (prix, temps).
     * 
     * @see ParkingSpotDAO
     * @see ticketDAO
     */
    public void processExitingVehicle() {
	try {
	    String vehicleRegNumber = getVehichleRegNumber();
	    Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
	    Date outTime = new Date();

	    ticket.setOutTime(outTime);

	    fareCalculatorService.calculateFare(ticket);
	    if (ticketDAO.updateTicket(ticket)) {
		ParkingSpot parkingSpot = ticket.getParkingSpot();
		parkingSpot.setAvailable(true);
		parkingSpotDAO.updateParking(parkingSpot);
		System.out.println("Please pay the parking fare:" + ticket.getPrice());
		System.out.println(
			"Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
	    } else {
		System.out.println("Unable to update ticket information. Error occurred");
	    }
	} catch (Exception e) {
	    logger.error("Unable to process exiting vehicle", e);
	}
    }

    /**
     * Méthode de test, similaire à processExitingVehicle(), mais permettant de
     * générer des heures d'entrée différents du système.
     * 
     * @see ParkingSpotDAO
     * @see ticketDAO
     */
    public void processExitingVehicle(long Duration) {
	try {
	    String vehicleRegNumber = getVehichleRegNumber();
	    Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
	    Date outTime = new Date(Duration);

	    ticket.setOutTime(outTime);

	    fareCalculatorService.calculateFare(ticket);
	    if (ticketDAO.applyDiscountForCustomer(vehicleRegNumber)) {
		Double newPrice = ticket.getPrice() * 0.95;
		ticket.setPrice(newPrice);
	    }
	    if (ticketDAO.updateTicket(ticket)) {
		ParkingSpot parkingSpot = ticket.getParkingSpot();
		parkingSpot.setAvailable(true);
		parkingSpotDAO.updateParking(parkingSpot);
		System.out.println("Please pay the parking fare:" + ticket.getPrice());
		System.out.println(
			"Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
	    } else {
		System.out.println("Unable to update ticket information. Error occurred");
	    }
	} catch (Exception e) {
	    logger.error("Unable to process exiting vehicle", e);
	}
    }

//TODO fix it so you don't repeat the code ! Use maybe some mock .... 
    /**
     * Méthode de test, similaire à processIncomingVehicle(), mais permettant de
     * générer des heures d'entrée différents du système.
     * 
     * @see ParkingSpotDAO
     * @see ticketDAO
     */

    public void processIncomingVehicle(Long started) {
	try {
	    ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();

	    if (parkingSpot != null && parkingSpot.getId() > 0) {
		String vehicleRegNumber = getVehichleRegNumber();
		parkingSpot.setAvailable(false);
		parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
							  // false

		Date inTime = new Date(started);
		Ticket ticket = new Ticket();
		// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
		// ticket.setId(ticketID);
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber(vehicleRegNumber);
		ticket.setPrice(0);
		if (ticket.getInTime() == null)
		    ticket.setInTime(inTime);
		ticket.setOutTime(null);
		if (ticketDAO.checkCustomerProgram(vehicleRegNumber))
		    System.out.println("Welcome backdear customer ! a 5% discount would be applied !");
		ticketDAO.saveTicket(ticket);
		System.out.println("Generated Ticket and saved in DB");
		System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
		System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
	    }
	} catch (Exception e) {
	    logger.error("Unable to process incoming vehicle", e);
	}
    }
}
