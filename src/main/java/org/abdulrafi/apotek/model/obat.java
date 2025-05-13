package org.abdulrafi.apotek.model;

import java.math.BigDecimal;

public class obat {
    private int id;
    private String nama;
    private BigDecimal harga;
    private int stok;

    // Constructor
    public obat() {
    }

    public obat(int id, String nama, BigDecimal harga, int stok) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public BigDecimal getHarga() {
        return harga;
    }

    public void setHarga(BigDecimal harga) {
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    // Opsional: Override toString() untuk debugging atau tampilan sederhana
    @Override
    public String toString() {
        return "Obat{" +
               "id=" + id +
               ", nama='" + nama + '\'' +
               ", harga=" + harga +
               ", stok=" + stok +
               '}';
    }
}