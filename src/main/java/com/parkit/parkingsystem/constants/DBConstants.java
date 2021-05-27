package com.parkit.parkingsystem.constants;

public class DBConstants {

	public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
	public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";

	public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)";
	public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";
	public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? order by t.IN_TIME DESC limit 1";// Attention
	public static final String CHECK_TICKET_IF_CUSTOMER = "SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ?";																																																											// à
																																																													// ce
// Donc l'erreur était que lorsque l'on cherchait à selectionner le ticket pour faire le calcul 
	// on triait la requête SQL par ordre INTIME (du plus vieux au plus récent) et
	// de ce fait, on prenait
	// toujours le premier ticket émis, et non pas le ticket dernier.
	// je
	// viens
	// de
	// modifier
	// !
}
