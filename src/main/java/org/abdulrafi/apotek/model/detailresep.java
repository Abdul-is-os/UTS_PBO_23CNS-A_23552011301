package org.abdulrafi.apotek.model;

public class detailresep {
    private int id;
    private int resepId; // Atau objek Resep resep;
    private int obatId;  // Atau objek Obat obat;
    private int jumlah;

    // Constructor
    public detailresep() {
    }

    public detailresep(int id, int resepId, int obatId, int jumlah) {
        this.id = id;
        this.resepId = resepId;
        this.obatId = obatId;
        this.jumlah = jumlah;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResepId() {
        return resepId;
    }

    public void setResepId(int resepId) {
        this.resepId = resepId;
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

    @Override
    public String toString() {
        return "DetailResep{" +
               "id=" + id +
               ", resepId=" + resepId +
               ", obatId=" + obatId +
               ", jumlah=" + jumlah +
               '}';
    }
}