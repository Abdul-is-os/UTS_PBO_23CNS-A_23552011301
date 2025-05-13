package org.abdulrafi.apotek.model;

public abstract class tenagamedis {

    private int id;
    private String nama;
    private tipetenagamedis tipe;
    private String username;
    private String passwordHash;

    // Constructor default
    public tenagamedis() {
    }

    // Constructor dengan semua field termasuk yang baru
    public tenagamedis(int id, String nama, tipetenagamedis tipe, String username, String passwordHash) {
        this.id = id;
        this.nama = nama;
        this.tipe = tipe;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Constructor yang mungkin masih dipakai (tanpa username/password explisit saat membuat objek Dokter)
    public tenagamedis(int id, String nama, tipetenagamedis tipe) {
        this.id = id;
        this.nama = nama;
        this.tipe = tipe;
    }

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

    public tipetenagamedis getTipe() {
        return tipe;
    }

    public void setTipe(tipetenagamedis tipe) {
        this.tipe = tipe;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "TenagaMedis{"
                + "id=" + id
                + ", nama='" + nama + '\''
                + ", tipe=" + tipe
                + ", username='" + username + '\''
                + '}';
    }
}
