package org.abdulrafi.apotek.model;

import java.math.BigDecimal;
import java.util.Date; // java.sql.Timestamp lebih presisi, tapi java.util.Date umum digunakan
import java.util.List;

public class transaksi {
    private int id;
    private Integer pasienId; // Integer agar bisa NULL
    private Integer resepId;  // Integer agar bisa NULL
    private BigDecimal totalTransaksi;
    private BigDecimal jumlahDibayar;
    private BigDecimal kembalian;
    private Date tanggalTransaksi; // Di DB: TIMESTAMP, di Java bisa Date atau Timestamp
    private Integer idApoteker; // Integer agar bisa NULL (jika ada kasus transaksi tanpa login apoteker)
                               // atau objek Apoteker apoteker;
    private List<detailtransaksi> detailTransaksiList; // Untuk menampung item-item obat dalam transaksi

    // Constructor
    public transaksi() {
    }

    public transaksi(int id, BigDecimal totalTransaksi, BigDecimal jumlahDibayar, BigDecimal kembalian, Date tanggalTransaksi) {
        this.id = id;
        this.totalTransaksi = totalTransaksi;
        this.jumlahDibayar = jumlahDibayar;
        this.kembalian = kembalian;
        this.tanggalTransaksi = tanggalTransaksi;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getPasienId() {
        return pasienId;
    }

    public void setPasienId(Integer pasienId) {
        this.pasienId = pasienId;
    }

    public Integer getResepId() {
        return resepId;
    }

    public void setResepId(Integer resepId) {
        this.resepId = resepId;
    }

    public BigDecimal getTotalTransaksi() {
        return totalTransaksi;
    }

    public void setTotalTransaksi(BigDecimal totalTransaksi) {
        this.totalTransaksi = totalTransaksi;
    }

    public BigDecimal getJumlahDibayar() {
        return jumlahDibayar;
    }

    public void setJumlahDibayar(BigDecimal jumlahDibayar) {
        this.jumlahDibayar = jumlahDibayar;
    }

    public BigDecimal getKembalian() {
        return kembalian;
    }

    public void setKembalian(BigDecimal kembalian) {
        this.kembalian = kembalian;
    }

    public Date getTanggalTransaksi() {
        return tanggalTransaksi;
    }

    public void setTanggalTransaksi(Date tanggalTransaksi) {
        this.tanggalTransaksi = tanggalTransaksi;
    }

    public Integer getIdApoteker() {
        return idApoteker;
    }

    public void setIdApoteker(Integer idApoteker) {
        this.idApoteker = idApoteker;
    }

    public List<detailtransaksi> getDetailTransaksiList() {
        return detailTransaksiList;
    }

    public void setDetailTransaksiList(List<detailtransaksi> detailTransaksiList) {
        this.detailTransaksiList = detailTransaksiList;
    }

    @Override
    public String toString() {
        return "Transaksi{" +
               "id=" + id +
               ", pasienId=" + pasienId +
               ", resepId=" + resepId +
               ", totalTransaksi=" + totalTransaksi +
               ", jumlahDibayar=" + jumlahDibayar +
               ", kembalian=" + kembalian +
               ", tanggalTransaksi=" + tanggalTransaksi +
               ", idApoteker=" + idApoteker +
               '}';
    }
}
