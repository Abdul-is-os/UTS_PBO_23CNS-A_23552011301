package org.abdulrafi.apotek.model;

public class pasien {
    private int id;
    private String nama;
    private int umur;

    // Constructor
    public pasien() {
    }

    public pasien(int id, String nama, int umur) {
        this.id = id;
        this.nama = nama;
        this.umur = umur;
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

    public int getUmur() {
        return umur;
    }

    public void setUmur(int umur) {
        this.umur = umur;
    }

    @Override
    public String toString() {
        return "Pasien{" +
               "id=" + id +
               ", nama='" + nama + '\'' +
               ", umur=" + umur +
               '}';
    }
}
