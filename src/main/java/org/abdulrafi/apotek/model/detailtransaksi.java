package org.abdulrafi.apotek.model;

import java.math.BigDecimal;

public class detailtransaksi {
    private int id;
    private int transaksiId; // Atau objek Transaksi transaksi;
    private int obatId;      // Atau objek Obat obat;
    private int jumlah;
    private BigDecimal hargaSaatTransaksi;
    private BigDecimal subTotalObat;

    // Constructor
    public detailtransaksi() {
    }

    public detailtransaksi(int id, int transaksiId, int obatId, int jumlah, BigDecimal hargaSaatTransaksi, BigDecimal subTotalObat) {
        this.id = id;
        this.transaksiId = transaksiId;
        this.obatId = obatId;
        this.jumlah = jumlah;
        this.hargaSaatTransaksi = hargaSaatTransaksi;
        this.subTotalObat = subTotalObat;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTransaksiId() {
        return transaksiId;
    }

    public void setTransaksiId(int transaksiId) {
        this.transaksiId = transaksiId;
    }

    public int getObatId() {
        return obatId;
    }

    public void setObatId(int obatId) {
        this.obatId = obatId;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public BigDecimal getHargaSaatTransaksi() {
        return hargaSaatTransaksi;
    }

    public void setHargaSaatTransaksi(BigDecimal hargaSaatTransaksi) {
        this.hargaSaatTransaksi = hargaSaatTransaksi;
    }

    public BigDecimal getSubTotalObat() {
        return subTotalObat;
    }

    public void setSubTotalObat(BigDecimal subTotalObat) {
        this.subTotalObat = subTotalObat;
    }

    @Override
    public String toString() {
        return "DetailTransaksi{" +
               "id=" + id +
               ", transaksiId=" + transaksiId +
               ", obatId=" + obatId +
               ", jumlah=" + jumlah +
               ", hargaSaatTransaksi=" + hargaSaatTransaksi +
               ", subTotalObat=" + subTotalObat +
               '}';
    }
}
