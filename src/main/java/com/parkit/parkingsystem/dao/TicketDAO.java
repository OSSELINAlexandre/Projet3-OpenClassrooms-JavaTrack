package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * <b>TicketDAO est la classe qui regroupe l'accès aux données de la base de
 * donnée des Tickets. </b>
 * <p>
 * Cette classe regroupe l'ensemble des méthodes rentrant en contact avec la
 * base de donnée.
 * 
 * Ainsi, c'est par TicketDAO que le système enregistre, récupère, et met à jour
 * les tickets des clients.
 * </p>
 * 
 * @see ParkingSpot
 * 
 * @author Alexandre OSSELIN
 * @version 1.0
 */
public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * saveTicket permet d'enregistrer un ticket dans la base de donnée.
     * 
     * A la fin de ce processus, la base de donnée à mis à jour les disponibilité de
     * parking. Un ticket avec l'heure d'entrée et le numéro d'imatriculation du
     * client a été crée. La méthode retourne TRUE si le ticket a été correctement
     * créer, et FAUX si celui-ci n'a pas été crée. Cette méthode utilise comme
     * argument un ticket lui étant envoyé et enregistre toutes les informations du
     * ticket dans la base de donnée.
     * 
     * 
     * @return boolean
     * @see TicketDAO
     */

    public boolean saveTicket(Ticket ticket) {
	Connection con = null;
	try {
	    con = dataBaseConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
	    // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
	    // ps.setInt(1,ticket.getId());
	    ps.setInt(1, ticket.getParkingSpot().getId());
	    ps.setString(2, ticket.getVehicleRegNumber());
	    ps.setDouble(3, ticket.getPrice());
	    ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
	    ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
	    return ps.execute();
	} catch (Exception ex) {
	    logger.error("Error fetching next available slot", ex);
	} finally {
	    dataBaseConfig.closeConnection(con);
	    return false;
	}
    }

    /**
     * getTicket permet de récupérer un ticket de stationnement en fonction de
     * l'immatriculation de l'utilsateur.
     * 
     * La valeur de retour de cette méthode est un ticket regroupant une date une
     * date d'entrée, de sortie ainsi qu'un tarif.
     * 
     * @return Ticket
     */

    public Ticket getTicket(String vehicleRegNumber) {
	Connection con = null;
	Ticket ticket = null;
	try {
	    con = dataBaseConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET); // Je pense que là, on a selectionner
										 // le mauvais ticket
	    // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
	    ps.setString(1, vehicleRegNumber);
	    ResultSet rs = ps.executeQuery();
	    if (rs.next()) {
		ticket = new Ticket();
		ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
		ticket.setParkingSpot(parkingSpot);
		ticket.setId(rs.getInt(2));
		ticket.setVehicleRegNumber(vehicleRegNumber);
		ticket.setPrice(rs.getDouble(3));
		ticket.setInTime(rs.getTimestamp(4));
		ticket.setOutTime(rs.getTimestamp(5));
	    }
	    dataBaseConfig.closeResultSet(rs);
	    dataBaseConfig.closePreparedStatement(ps);
	} catch (Exception ex) {
	    logger.error("Error fetching next available slot", ex);
	} finally {
	    dataBaseConfig.closeConnection(con);
	    return ticket;
	}
    }

    /**
     * updateTicket permet de mettre à jour les information d'un ticket fournit en
     * argument de la méthode.
     * 
     * 
     * A la fin de ce processus, le ticket fournit en argument a été mis à jour dans
     * la base de donnée.
     * 
     * @return boolean
     * @see ParkingSpotDAO
     */

    public boolean updateTicket(Ticket ticket) {
	Connection con = null;
	try {
	    con = dataBaseConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
	    ps.setDouble(1, ticket.getPrice());
	    ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
	    ps.setInt(3, ticket.getId());
	    ps.execute();
	    return true;
	} catch (Exception ex) {
	    logger.error("Error saving ticket info", ex);
	} finally {
	    dataBaseConfig.closeConnection(con);
	}
	return false;
    }

    /**
     * checkCustomerProgram permet de vérifier si un client à déjà enregistré un
     * ticket dans la base de donnée. Si il s'est déjà présenté une fois, alors un
     * ticket existera dans le système, et ainsi, nous affichons dans l'interfaceque
     * nous avons pris conscience que le client est déjà venu.
     * 
     * 
     * A la fin de ce processus, le ticket fournit en argument a été mis à jour dans
     * la base de donnée.
     * 
     * @return boolean
     * @see ParkingSpotDAO
     */
    public boolean checkCustomerProgram(String vehicleRegNumber) {
	Connection con = null;
	try {
	    con = dataBaseConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_TICKET_IF_CUSTOMER);
	    ps.setString(1, vehicleRegNumber);
	    ResultSet rs = ps.executeQuery();
	    if (rs.next())
		return true;
	} catch (Exception ex) {
	    logger.error("Error saving ticket info", ex);
	} finally {
	    dataBaseConfig.closeConnection(con);
	}
	return false;
    }

    /**
     * applyDiscountForCustomer permet de vérifier si un numéro d'immatriculation a
     * déjà été enregsitré dans le système de base de donné de gestion des tickets.
     * Cette méthode permet de vérifier si le client peut bénéficier du programme de
     * fidélité.
     * 
     * 
     * En fonction du résultat, la valeur de retour stipule si oui (TRUE) ou non
     * (FALSE) le numéro d'immatriculation à déjà été enregistré dans le système.
     * 
     * @return boolean
     * @see ParkingSpotDAO
     */
    public boolean applyDiscountForCustomer(String vehicleRegNumber) {
	Connection con = null;
	try {
	    con = dataBaseConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_TICKET_IF_CUSTOMER);
	    ps.setString(1, vehicleRegNumber);
	    ResultSet rs = ps.executeQuery();
	    rs.next();
	    if (rs.next())
		return true;
	} catch (Exception ex) {
	    logger.error("Error saving ticket info", ex);
	} finally {
	    dataBaseConfig.closeConnection(con);
	}
	return false;
    }
}
