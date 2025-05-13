package org.abdulrafi.apotek.model;

import java.util.Date;
import java.util.List;

public class resep {
    private int id;
    private int pasienId;
    private int dokterId; 
    private Date tanggal;
    private List<detailresep> detailResepList;

    // Constructor
    public resep() {
    }

    public resep(int id, int pasienId, int dokterId, Date tanggal) {
        this.id = id;
        this.pasienId = pasienId;
        this.dokterId = dokterId;
        this.tanggal = tanggal;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPasienId() {
        return pasienId;
    }

    public void setPasienId(int pasienId) {
        this.pasienId = pasienId;
    }

    public int getDokterId() {
        return dokterId;
    }

    public void setDokterId(int dokterId) {
        this.dokterId = dokterId;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public List<detailresep> getDetailResepList() {
        return detailResepList;
    }

    public void setDetailResepList(List<detailresep> detailResepList) {
        this.detailResepList = detailResepList;
    }

    @Override
    public String toString() {
        return "Resep{" +
               "id=" + id +
               ", pasienId=" + pasienId +
               ", dokterId=" + dokterId +
               ", tanggal=" + tanggal +
               '}';
    }
}