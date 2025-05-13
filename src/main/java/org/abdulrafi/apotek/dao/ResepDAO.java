// src/org/abdulrafi/apotek/dao/ResepDAO.java
package org.abdulrafi.apotek.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.abdulrafi.apotek.config.DatabaseConnector;
import org.abdulrafi.apotek.model.detailresep;
import org.abdulrafi.apotek.model.resep;

public class ResepDAO {

    /**
     * Menambahkan resep baru beserta detailnya ke database.
     * Ini seharusnya idealnya dijalankan dalam satu transaksi database.
     * @param resep Objek Resep yang berisi detail resep.
     * @return ID resep yang baru dibuat jika berhasil, -1 jika gagal.
     */
    public int addResep(resep resep) {
        String sqlResep = "INSERT INTO kasir_apotek_pbo2_resep (pasien_id, dokter_id, tanggal) VALUES (?, ?, ?)";
        String sqlDetailResep = "INSERT INTO kasir_apotek_pbo2_detail_resep (resep_id, obat_id, jumlah) VALUES (?, ?, ?)";
        int generatedResepId = -1;
        Connection conn = null;

        if (resep == null || resep.getPasienId() <= 0 || resep.getDokterId() <= 0 || resep.getTanggal() == null || resep.getDetailResepList() == null || resep.getDetailResepList().isEmpty()) {
            System.err.println("Data resep tidak valid atau detail resep kosong.");
            return -1;
        }

        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi

            // 1. Insert ke tabel resep
            try (PreparedStatement pstmtResep = conn.prepareStatement(sqlResep, Statement.RETURN_GENERATED_KEYS)) {
                pstmtResep.setInt(1, resep.getPasienId());
                pstmtResep.setInt(2, resep.getDokterId());
                pstmtResep.setDate(3, new java.sql.Date(resep.getTanggal().getTime())); // Konversi java.util.Date ke java.sql.Date

                int affectedRows = pstmtResep.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Pembuatan resep gagal, tidak ada baris yang terpengaruh.");
                }

                try (ResultSet generatedKeys = pstmtResep.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedResepId = generatedKeys.getInt(1);
                        resep.setId(generatedResepId); // Set ID pada objek resep
                    } else {
                        throw new SQLException("Pembuatan resep gagal, tidak mendapatkan ID.");
                    }
                }
            }

            // 2. Insert ke tabel detail_resep
            try (PreparedStatement pstmtDetail = conn.prepareStatement(sqlDetailResep)) {
                for (detailresep detail : resep.getDetailResepList()){
                    if (detail.getObatId() <= 0 || detail.getJumlah() <= 0) {
                        throw new SQLException("Data detail resep tidak valid: obat_id atau jumlah salah.");
                    }
                    pstmtDetail.setInt(1, generatedResepId);
                    pstmtDetail.setInt(2, detail.getObatId());
                    pstmtDetail.setInt(3, detail.getJumlah());
                    pstmtDetail.addBatch(); // Tambahkan ke batch untuk eksekusi sekaligus
                }
                pstmtDetail.executeBatch(); // Eksekusi semua perintah insert detail
            }

            conn.commit(); // Jika semua berhasil, commit transaksi
            System.out.println("Resep ID " + generatedResepId + " dan detailnya berhasil ditambahkan.");

        } catch (SQLException e) {
            System.err.println("Error saat menambahkan resep: " + e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaksi dibatalkan (rollback).");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error saat rollback: " + ex.getMessage());
                }
            }
            generatedResepId = -1; // Tandai gagal
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Kembalikan ke mode auto-commit
                    conn.close(); // Tutup koneksi (try-with-resources tidak bisa dipakai jika ingin kontrol transaksi manual)
                } catch (SQLException e) {
                    System.err.println("Error saat menutup koneksi setelah addResep: " + e.getMessage());
                }
            }
        }
        return generatedResepId;
    }

    private List<detailresep> getDetailResepByResepId(int resepId, Connection conn) throws SQLException {
        List<detailresep> detailList = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_detail_resep WHERE resep_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, resepId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    detailresep detail = new detailresep();
                    detail.setId(rs.getInt("id"));
                    detail.setResepId(rs.getInt("resep_id"));
                    detail.setObatId(rs.getInt("obat_id"));
                    detail.setJumlah(rs.getInt("jumlah"));
                    detailList.add(detail);
                }
            }
        }
        return detailList;
    }

    public resep getResepById(int id) {
        resep resep = null;
        String sql = "SELECT * FROM kasir_apotek_pbo2_resep WHERE id = ?";

        try (Connection conn = DatabaseConnector.getConnection()) { // Koneksi baru untuk operasi ini
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        resep = new resep();
                        resep.setId(rs.getInt("id"));
                        resep.setPasienId(rs.getInt("pasien_id"));
                        resep.setDokterId(rs.getInt("dokter_id"));
                        resep.setTanggal(rs.getDate("tanggal")); // Mengambil sebagai java.sql.Date, bisa langsung di-assign ke java.util.Date

                        // Ambil detail resep
                        List<detailresep> detailList = getDetailResepByResepId(id, conn);
                        resep.setDetailResepList(detailList);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil resep by ID " + id + ": " + e.getMessage());
        }
        return resep;
    }

    public List<resep> getAllResep() {
        List<resep> daftarResep = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_resep ORDER BY tanggal DESC, id DESC";

        try (Connection conn = DatabaseConnector.getConnection()) { // Koneksi baru
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    resep resep = new resep();
                    resep.setId(rs.getInt("id"));
                    resep.setPasienId(rs.getInt("pasien_id"));
                    resep.setDokterId(rs.getInt("dokter_id"));
                    resep.setTanggal(rs.getDate("tanggal"));

                    // Ambil detail untuk setiap resep
                    List<detailresep> detailList = getDetailResepByResepId(resep.getId(), conn);
                    resep.setDetailResepList(detailList);
                    daftarResep.add(resep);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil semua resep: " + e.getMessage());
        }
        return daftarResep;
    }
    
    // Metode lain seperti getResepByPasienId, deleteResep (beserta detailnya) bisa ditambahkan
    // Delete resep juga perlu penanganan transaksi yang hati-hati.
}