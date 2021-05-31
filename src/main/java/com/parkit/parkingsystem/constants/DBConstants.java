package com.parkit.parkingsystem.constants;


/**
 * <b>Centralise l'ensemble des requêtes SQL nécessaire pour communiquer avec les bases de données</b>
 * <p>Nous avons les requêtes liées à la base de donnée des places de parking</p>
 *  <p>Nous avons les requêtes liées à la base de donnée des tickets</p>
 */


public class DBConstants {

	public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
	public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";

	public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)";
	public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";
	public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? order by t.IN_TIME DESC limit 1";	/**
	 * J'ai changé la requête SQL pour séléctionner le ticket depuis la base de donnée. 
	 * En effet, si un même numéro de plaque d'immatriculation se présentait, alors la requête renvoyait le premier ticket et non pas l'actuel.
	 * J'ai donc modifié la requête pour renvoyé le résultat donc la date d'entrée est la plus récente dans le temps et non pas la plus ancienne.
	 * J'ai donc rajouté DESC
	 * @author Alexandre OSSELIN
	 */
	
	public static final String CHECK_TICKET_IF_CUSTOMER = "SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ?";																																																											// à

	/**
	 * Nouvelle requête pour vérifier le nombre d'occurence de l'apparition d'un numéro d'immatriculation dans la base de donnée.
	 * @author Alexandre OSSELIN
	 */
	


}
