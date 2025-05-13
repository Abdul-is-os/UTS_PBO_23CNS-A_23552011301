package org.abdulrafi.apotek.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.abdulrafi.apotek.config.DatabaseConnector;
import org.abdulrafi.apotek.model.pasien;

public class PasienDAO {

    /**
     * Mengambil semua data pasien dari database.
     * @return List objek Pasien, atau list kosong jika tidak ada data/error.
     */
    public List<pasien> getAllPasien() {
        List<pasien> daftarPasien = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_pasien ORDER BY nama ASC";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pasien pasien = new pasien();
                pasien.setId(rs.getInt("id"));
                pasien.setNama(rs.getString("nama"));
                pasien.setUmur(rs.getInt("umur"));
                daftarPasien.add(pasien);
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil semua pasien: " + e.getMessage());
        }
        return daftarPasien;
    }

    /**
     * Mengambil satu data pasien berdasarkan ID.
     * @param id ID pasien yang dicari.
     * @return Objek Pasien jika ditemukan, atau null jika tidak ditemukan/error.
     */
    public pasien getPasienById(int id) {
        pasien pasien = null;
        String sql = "SELECT * FROM kasir_apotek_pbo2_pasien WHERE id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pasien = new pasien();
                    pasien.setId(rs.getInt("id"));
                    pasien.setNama(rs.getString("nama"));
                    pasien.setUmur(rs.getInt("umur"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil pasien by ID " + id + ": " + e.getMessage());
        }
        return pasien;
    }

    /**
     * Mencari pasien berdasarkan nama (case-insensitive).
     * Ini bisa mengembalikan banyak pasien jika namanya tidak unik.
     * @param nama Nama pasien yang dicari.
     * @return List objek Pasien yang cocok, atau list kosong.
     */
    public List<pasien> getPasienByNama(String nama) {
        List<pasien> daftarPasien = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_pasien WHERE LOWER(nama) LIKE LOWER(?) ORDER BY nama ASC"; // Pencarian case-insensitive

        if (nama == null || nama.trim().isEmpty()) {
            return daftarPasien; // Kembalikan list kosong jika nama tidak valid
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nama + "%"); // Gunakan LIKE untuk pencarian parsial

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pasien pasien = new pasien();
                    pasien.setId(rs.getInt("id"));
                    pasien.setNama(rs.getString("nama"));
                    pasien.setUmur(rs.getInt("umur"));
                    daftarPasien.add(pasien);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mencari pasien by nama '" + nama + "': " + e.getMessage());
        }
        return daftarPasien;
    }


    /**
     * Menambahkan pasien baru ke database.
     * @param pasien Objek Pasien yang akan ditambahkan (ID tidak perlu di-set).
     * @return ID pasien yang baru ditambahkan jika berhasil, atau -1 jika gagal.
     */
    public int addPasien(pasien pasien) {
        String sql = "INSERT INTO kasir_apotek_pbo2_pasien (nama, umur) VALUES (?, ?)";
        int generatedId = -1;

        if (pasien == null || pasien.getNama() == null || pasien.getNama().trim().isEmpty() || pasien.getUmur() <= 0) {
            System.err.println("Data pasien tidak valid untuk ditambahkan.");
            return -1;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, pasien.getNama());
            pstmt.setInt(2, pasien.getUmur());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                        pasien.setId(generatedId); // Set ID pada objek pasien juga
                        System.out.println("Pasien '" + pasien.getNama() + "' berhasil ditambahkan dengan ID: " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat menambahkan pasien '" + pasien.getNama() + "': " + e.getMessage());
        }
        return generatedId;
    }

    /**
     * Memperbarui data pasien yang sudah ada di database.
     * @param pasien Objek Pasien yang akan diperbarui (harus memiliki ID yang valid).
     * @return true jika berhasil memperbarui, false jika gagal.
     */
    public boolean updatePasien(pasien pasien) {
        String sql = "UPDATE kasir_apotek_pbo2_pasien SET nama = ?, umur = ? WHERE id = ?";
        boolean result = false;

        if (pasien == null || pasien.getId() <= 0 || pasien.getNama() == null || pasien.getNama().trim().isEmpty() || pasien.getUmur() <= 0) {
            System.err.println("Data pasien tidak valid untuk diperbarui.");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pasien.getNama());
            pstmt.setInt(2, pasien.getUmur());
            pstmt.setInt(3, pasien.getId());

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error saat memperbarui pasien ID " + pasien.getId() + ": " + e.getMessage());
        }
        return result;
    }

    /**
     * Menghapus data pasien dari database berdasarkan ID.
     * @param id ID pasien yang akan dihapus.
     * @return true jika berhasil menghapus, false jika gagal.
     */
    public boolean deletePasien(int id) {
        String sql = "DELETE FROM kasir_apotek_pbo2_pasien WHERE id = ?";
        boolean result = false;

        if (id <= 0) {
            System.err.println("ID pasien tidak valid untuk dihapus.");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error saat menghapus pasien ID " + id + ": " + e.getMessage());
            // Tangani foreign key constraint (misal, jika pasien memiliki resep atau transaksi)
            if (e.getErrorCode() == 1451) { // Error code MySQL untuk foreign key constraint
                System.err.println("Gagal menghapus: Pasien ID " + id + " masih memiliki data resep atau transaksi terkait.");
            }
        }
        return result;
    }
}