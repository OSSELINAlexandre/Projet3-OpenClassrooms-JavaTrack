package com.parkit.parkingsystem.model;

import java.util.Calendar;
import java.util.Date;


/**
 * <b>Ticket est la classe modèle pour représenter les tickets de parking </b>
 * <p>
 * Une place de parking se représente avec :
 * <ul>
 * <li>Un numéro d'identification de ticket</li>
 * <li>Un parking associé</li>
 * <li>Un numéro d'immatriculation</li>
 * <li>Un prix</li>
 * <li>Une date d'entrée</li>
 * <li>Une date de sortie</li>
 * </ul>
 * </p>
 * 
 * @see ParkingSpot
 * 
 * @author Alexandre OSSELIN
 * @version 1.0
 */


public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private Date inTime;
    private Date outTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getInTime() {
        return inTime;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    public Date getOutTime() {
        return outTime;
    }

    public void setOutTime(Date outTime) {
        this.outTime = outTime;
    }
}
