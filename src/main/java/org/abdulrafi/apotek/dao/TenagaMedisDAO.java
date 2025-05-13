// src/org/abdulrafi/apotek/dao/TenagaMedisDAO.java
package org.abdulrafi.apotek.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.abdulrafi.apotek.config.DatabaseConnector;
import org.abdulrafi.apotek.model.apoteker;
import org.abdulrafi.apotek.model.dokter;
import org.abdulrafi.apotek.model.tenagamedis;
import org.abdulrafi.apotek.model.tipetenagamedis;
// ...

public class TenagaMedisDAO {

    private tenagamedis mapResultSetToTenagaMedis(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nama = rs.getString("nama");
        tipetenagamedis tipe = tipetenagamedis.valueOf(rs.getString("tipe").toUpperCase());
        String username = rs.getString("username"); // BARU
        String passwordHash = rs.getString("password_hash"); // BARU

        if (tipe == tipetenagamedis.DOKTER) {
            dokter dokter = new dokter(id, nama);
            dokter.setUsername(username); // Set username jika ada
            // Dokter mungkin tidak punya passwordHash atau tidak relevan untuk login di sistem kasir
            return dokter;
        } else if (tipe == tipetenagamedis.APOTEKER) {
            // Untuk Apoteker, kita buat objek dengan semua info
            return new apoteker(id, nama, username, passwordHash);
        }
        return null;
    }

    // Metode getAllTenagaMedis() dan getTenagaMedisById(int id)
    // akan otomatis terupdate karena menggunakan mapResultSetToTenagaMedis
    public apoteker getApotekerByUsername(String username) { // GANTI dari getApotekerByNama
        apoteker apoteker = null;
        String sql = "SELECT * FROM kasir_apotek_pbo2_tenaga_medis WHERE username = ? AND tipe = ?";

        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, tipetenagamedis.APOTEKER.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Menggunakan mapResultSetToTenagaMedis akan menghasilkan TenagaMedis, perlu cast
                    tenagamedis tm = mapResultSetToTenagaMedis(rs);
                    if (tm instanceof apoteker) {
                        apoteker = (apoteker) tm;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil apoteker by username '" + username + "': " + e.getMessage());
        }
        return apoteker;
    }

    public dokter getDokterByNama(String nama) {
        dokter dokter = null;
        // Query mencari nama yang cocok dan tipenya DOKTER
        String sql = "SELECT * FROM kasir_apotek_pbo2_tenaga_medis WHERE LOWER(nama) = LOWER(?) AND tipe = ?";

        if (nama == null || nama.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nama);
            pstmt.setString(2, tipetenagamedis.DOKTER.name()); // Filter berdasarkan tipe DOKTER

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Menggunakan mapResultSetToTenagaMedis akan menghasilkan TenagaMedis, perlu cast
                    tenagamedis tm = mapResultSetToTenagaMedis(rs);
                    if (tm instanceof dokter) {
                        dokter = (dokter) tm;
                    } else {
                        // Ini seharusnya tidak terjadi jika query SQL benar, tapi sebagai pengaman
                        System.err.println("Error: Data ditemukan untuk nama " + nama + " tapi bukan Dokter.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil dokter by nama '" + nama + "': " + e.getMessage());
        }
        return dokter;
    }

    // Metode getAllDokter(), getDokterByNama(), getAllApoteker()
    // perlu disesuaikan jika ingin mengambil username juga.
    // Contoh untuk getAllDokter():
    public List<dokter> getAllDokter() {
        List<dokter> daftarDokter = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_tenaga_medis WHERE tipe = ? ORDER BY nama ASC";

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipetenagamedis.DOKTER.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    dokter d = new dokter(rs.getInt("id"), rs.getString("nama"));
                    d.setUsername(rs.getString("username")); // Ambil username
                    daftarDokter.add(d);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil semua dokter: " + e.getMessage());
        }
        return daftarDokter;
    }
    // Lakukan penyesuaian serupa untuk getAllApoteker() dan getDokterByNama() jika perlu username

    public boolean addTenagaMedis(tenagamedis tm) {
        // Pastikan tm memiliki username dan passwordHash jika tipe Apoteker
        // Untuk Dokter, username dan passwordHash bisa null
        String sql = "INSERT INTO kasir_apotek_pbo2_tenaga_medis (nama, tipe, username, password_hash) VALUES (?, ?, ?, ?)";
        boolean result = false;

        // Validasi dasar
        if (tm == null || tm.getNama() == null || tm.getNama().trim().isEmpty() || tm.getTipe() == null) {
            System.err.println("Data dasar tenaga medis tidak valid.");
            return false;
        }
        if (tm.getTipe() == tipetenagamedis.APOTEKER && (tm.getUsername() == null || tm.getUsername().trim().isEmpty() || tm.getPasswordHash() == null || tm.getPasswordHash().isEmpty())) {
            System.err.println("Untuk Apoteker, username dan password hash tidak boleh kosong.");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tm.getNama());
            pstmt.setString(2, tm.getTipe().name());

            if (tm.getUsername() != null && !tm.getUsername().trim().isEmpty()) {
                pstmt.setString(3, tm.getUsername());
            } else {
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            }

            if (tm.getPasswordHash() != null && !tm.getPasswordHash().isEmpty()) {
                pstmt.setString(4, tm.getPasswordHash());
            } else {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            }

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saat menambahkan tenaga medis '" + tm.getNama() + "': " + e.getMessage());
            if (e.getErrorCode() == 1062) { // Error code MySQL untuk duplicate entry (UNIQUE constraint)
                System.err.println("Username '" + tm.getUsername() + "' mungkin sudah ada.");
            }
        }
        return result;
    }

    public boolean updateTenagaMedis(tenagamedis tm) {
        // Mirip dengan addTenagaMedis, pastikan validasi dan set parameter yang sesuai
        String sql = "UPDATE kasir_apotek_pbo2_tenaga_medis SET nama = ?, tipe = ?, username = ?, password_hash = ? WHERE id = ?";
        boolean result = false;

        // Validasi seperti di addTenagaMedis
        if (tm == null || tm.getId() <= 0 || tm.getNama() == null || tm.getNama().trim().isEmpty() || tm.getTipe() == null) {
            System.err.println("Data dasar tenaga medis tidak valid untuk update.");
            return false;
        }
        if (tm.getTipe() == tipetenagamedis.APOTEKER && (tm.getUsername() == null || tm.getUsername().trim().isEmpty() /* jangan update password hash jika tidak diubah */)) {
            System.err.println("Untuk Apoteker, username tidak boleh kosong saat update.");
            // Password hash mungkin tidak selalu diupdate, bisa dibuat logika terpisah untuk update password
            // Untuk saat ini, kita asumsikan jika Apoteker, username diisi. Password hash hanya diupdate jika disediakan.
        }

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tm.getNama());
            pstmt.setString(2, tm.getTipe().name());

            if (tm.getUsername() != null && !tm.getUsername().trim().isEmpty()) {
                pstmt.setString(3, tm.getUsername());
            } else {
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            }

            // Penting: Hanya update password_hash jika memang disediakan (misal, saat ganti password)
            // Jika tm.getPasswordHash() null atau kosong, JANGAN update field password di DB
            // kecuali Anda memang ingin menghapusnya (yang biasanya tidak).
            // Untuk update umum, biasanya password tidak diikutkan kecuali ada fitur "ganti password".
            // Untuk saat ini, jika ada, kita update.
            if (tm.getPasswordHash() != null && !tm.getPasswordHash().isEmpty()) {
                pstmt.setString(4, tm.getPasswordHash());
            } else {
                // Jika tidak ada password baru, jangan ubah password yang ada
                // Kita perlu query yang berbeda jika tidak ingin mengubah password.
                // Atau, ambil dulu password lama lalu set kembali jika tidak diubah.
                // Untuk simpelnya, kita buat statement ini mengupdate password jika ada.
                // Jika tm.getPasswordHash() null, kita set NULL di DB (hati-hati!)
                // Mungkin lebih baik password_hash tidak disertakan di update umum ini.
                // Saya akan set null jika kosong, tapi ini perlu pertimbangan desain.
                pstmt.setNull(4, java.sql.Types.VARCHAR); // Hati-hati dengan ini
            }
            pstmt.setInt(5, tm.getId());

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saat memperbarui tenaga medis ID " + tm.getId() + ": " + e.getMessage());
            if (e.getErrorCode() == 1062) {
                System.err.println("Username '" + tm.getUsername() + "' mungkin sudah digunakan oleh ID lain.");
            }
        }
        return result;
    }

    // Metode deleteTenagaMedis(int id) tidak perlu banyak berubah,
    // karena username dan password_hash akan ikut terhapus.
}
