// src/org/abdulrafi/apotek/dao/ObatDAO.java
package org.abdulrafi.apotek.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.abdulrafi.apotek.config.DatabaseConnector;
import org.abdulrafi.apotek.model.obat;

public class ObatDAO {

    /**
     * Mengambil semua data obat dari database.
     *
     * @return List objek Obat, atau list kosong jika tidak ada data/error.
     */
    public List<obat> getAllObat() {
        List<obat> daftarObat = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_obat ORDER BY nama ASC";
        Connection conn = null; // Deklarasikan di luar try

        try {
            conn = DatabaseConnector.getConnection(); // Dapatkan koneksi
            if (conn == null || conn.isClosed()) { // Periksa apakah koneksi valid
                System.err.println("Error saat mengambil semua obat: Koneksi tidak valid atau sudah tertutup.");
                // Tambahkan kode debug Anda di sini jika ada
                if (conn != null) {
                    System.err.println("[DEBUG] ObatDAO: conn.isClosed() = " + conn.isClosed());
                } else {
                    System.err.println("[DEBUG] ObatDAO: conn adalah null");
                }
                return daftarObat; // Kembalikan list kosong
            }

            // Statement dan ResultSet tetap bisa menggunakan try-with-resources
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                // ... (looping ResultSet) ...
                while (rs.next()) {
                    // ... (map data ke objek obat) ...
                    obat obat = new obat();
                    obat.setId(rs.getInt("id"));
                    obat.setNama(rs.getString("nama"));
                    obat.setHarga(rs.getBigDecimal("harga"));
                    obat.setStok(rs.getInt("stok"));
                    daftarObat.add(obat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil semua obat: " + e.getMessage());
            e.printStackTrace(); // Penting untuk melihat detail error
        }
        return daftarObat;
    }

    /**
     * Mengambil satu data obat berdasarkan ID menggunakan koneksi baru.
     *
     * @param id ID obat yang dicari.
     * @return Objek Obat jika ditemukan, atau null jika tidak ditemukan/error.
     */
    public obat getObatById(int id) {
        obat obat = null;
        // Dapatkan koneksi baru untuk operasi ini
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Panggil metode helper yang menggunakan koneksi
            obat = getObatByIdWithConnection(conn, id);
        } catch (SQLException e) {
            System.err.println("Error saat mendapatkan koneksi untuk getObatById " + id + ": " + e.getMessage());
        }
        return obat;
    }

    /**
     * [MODIFIED/NEW] Mengambil satu data obat berdasarkan ID menggunakan
     * koneksi yang disediakan. Digunakan dalam operasi transaksional. Melempar
     * SQLException jika terjadi error.
     *
     * @param conn Koneksi database yang sudah ada.
     * @param id ID obat yang dicari.
     * @return Objek Obat jika ditemukan, atau null jika tidak.
     * @throws SQLException jika terjadi error database.
     */
    public obat getObatByIdWithConnection(Connection conn, int id) throws SQLException {
        obat obat = null;
        String sql = "SELECT * FROM kasir_apotek_pbo2_obat WHERE id = ?";

        // Tidak menggunakan try-with-resources untuk connection di sini karena dikelola dari luar
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    obat = new obat();
                    obat.setId(rs.getInt("id"));
                    obat.setNama(rs.getString("nama"));
                    obat.setHarga(rs.getBigDecimal("harga"));
                    obat.setStok(rs.getInt("stok"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL dalam getObatByIdWithConnection ID " + id + ": " + e.getMessage());
            throw e; // Lempar ulang exception agar transaksi bisa di-rollback
        }
        // Jika tidak ditemukan, kembalikan null
        return obat;
    }

    /**
     * Menambahkan obat baru ke database.
     *
     * @param obat Objek Obat yang akan ditambahkan (ID tidak perlu di-set, akan
     * auto-increment).
     * @return true jika berhasil menambahkan, false jika gagal.
     */
    public boolean addObat(obat obat) {
        String sql = "INSERT INTO kasir_apotek_pbo2_obat (nama, harga, stok) VALUES (?, ?, ?)";
        boolean result = false;

        // Validasi dasar sebelum insert
        if (obat == null || obat.getNama() == null || obat.getNama().trim().isEmpty() || obat.getHarga() == null || obat.getHarga().compareTo(BigDecimal.ZERO) < 0 || obat.getStok() < 0) {
            System.err.println("Data obat tidak valid untuk ditambahkan.");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, obat.getNama());
            pstmt.setBigDecimal(2, obat.getHarga());
            pstmt.setInt(3, obat.getStok());

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0; // Berhasil jika ada baris yang terpengaruh

        } catch (SQLException e) {
            System.err.println("Error saat menambahkan obat '" + obat.getNama() + "': " + e.getMessage());
            // Tangani error duplikasi nama jika perlu (SQLIntegrityConstraintViolationException)
            if (e.getErrorCode() == 1062) { // Error code untuk duplicate entry MySQL
                System.err.println("Nama obat '" + obat.getNama() + "' sudah ada.");
            }
        }
        return result;
    }

    /**
     * Memperbarui data obat yang sudah ada di database.
     *
     * @param obat Objek Obat yang akan diperbarui (harus memiliki ID yang
     * valid).
     * @return true jika berhasil memperbarui, false jika gagal.
     */
    public boolean updateObat(obat obat) {
        String sql = "UPDATE kasir_apotek_pbo2_obat SET nama = ?, harga = ?, stok = ? WHERE id = ?";
        boolean result = false;

        // Validasi dasar sebelum update
        if (obat == null || obat.getId() <= 0 || obat.getNama() == null || obat.getNama().trim().isEmpty() || obat.getHarga() == null || obat.getHarga().compareTo(BigDecimal.ZERO) < 0 || obat.getStok() < 0) {
            System.err.println("Data obat tidak valid untuk diperbarui.");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, obat.getNama());
            pstmt.setBigDecimal(2, obat.getHarga());
            pstmt.setInt(3, obat.getStok());
            pstmt.setInt(4, obat.getId()); // ID untuk klausa WHERE

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error saat memperbarui obat ID " + obat.getId() + ": " + e.getMessage());
            if (e.getErrorCode() == 1062) { // Error code untuk duplicate entry MySQL
                System.err.println("Gagal update: Nama obat '" + obat.getNama() + "' mungkin sudah digunakan oleh ID lain.");
            }
        }
        return result;
    }

    /**
     * Menghapus data obat dari database berdasarkan ID.
     *
     * @param id ID obat yang akan dihapus.
     * @return true jika berhasil menghapus, false jika gagal.
     */
    public boolean deleteObat(int id) {
        String sql = "DELETE FROM kasir_apotek_pbo2_obat WHERE id = ?";
        boolean result = false;

        if (id <= 0) {
            System.err.println("ID obat tidak valid untuk dihapus.");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error saat menghapus obat ID " + id + ": " + e.getMessage());
            // Tangani foreign key constraint violation jika obat sudah terkait di tabel lain
            // (misal detail_resep, detail_transaksi)
            // MySQL error code untuk foreign key constraint biasanya 1451
            if (e.getErrorCode() == 1451) {
                System.err.println("Gagal menghapus: Obat ID " + id + " masih digunakan dalam transaksi atau resep.");
            }
        }
        return result;
    }

    /**
     * Memperbarui stok obat setelah terjadi penjualan menggunakan koneksi baru.
     * Metode ini memastikan stok tidak menjadi negatif.
     *
     * @param idObat ID obat yang stoknya akan dikurangi.
     * @param jumlahDibeli Jumlah obat yang terjual/dibeli.
     * @return true jika stok berhasil diperbarui, false jika gagal (misal, stok
     * tidak cukup).
     */
    public boolean updateStok(int idObat, int jumlahDibeli) {
        boolean result = false;
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Panggil metode helper yang sebenarnya melakukan update
            result = updateStokWithConnection(conn, idObat, jumlahDibeli);
        } catch (SQLException e) {
            // Pesan error spesifik (stok tidak cukup dll) sudah ditangani di dalam
            // updateStokWithConnection jika exception dilempar dari sana.
            // Di sini kita bisa catat error koneksi jika terjadi.
            System.err.println("Error koneksi saat mencoba update stok untuk obat ID " + idObat + ": " + e.getMessage());
        }
        return result;
    }

    /**
     * [MODIFIED/NEW] Memperbarui stok obat menggunakan koneksi yang disediakan
     * (untuk transaksi). Metode ini memastikan stok tidak menjadi negatif dan
     * melempar SQLException jika gagal.
     *
     * @param conn Koneksi database yang sudah ada dan dikelola dari luar.
     * @param idObat ID obat yang stoknya akan dikurangi.
     * @param jumlahDibeli Jumlah obat yang terjual/dibeli.
     * @return true jika stok berhasil diperbarui.
     * @throws SQLException jika terjadi error database atau jika stok tidak
     * mencukupi.
     */
    public boolean updateStokWithConnection(Connection conn, int idObat, int jumlahDibeli) throws SQLException {
        // Query ini hanya akan berhasil jika stok >= jumlahDibeli
        String sql = "UPDATE kasir_apotek_pbo2_obat SET stok = stok - ? WHERE id = ? AND stok >= ?";
        boolean result = false;

        if (idObat <= 0 || jumlahDibeli <= 0) {
            // Di sini kita lempar SQLException agar bisa di-catch oleh pemanggil dan transaksi di-rollback
            throw new SQLException("ID obat (" + idObat + ") atau jumlah dibeli (" + jumlahDibeli + ") tidak valid untuk update stok.");
        }

        // Tidak menggunakan try-with-resources untuk connection
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jumlahDibeli);
            pstmt.setInt(2, idObat);
            pstmt.setInt(3, jumlahDibeli); // Kondisi WHERE stok >= jumlahDibeli

            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;

            if (!result) {
                // Jika tidak ada baris yang terpengaruh, mungkin karena stok tidak cukup atau ID tidak ada
                // Cek penyebabnya dan lempar exception agar transaksi dibatalkan
                obat obatCek = getObatByIdWithConnection(conn, idObat); // Cek dengan koneksi yang sama
                if (obatCek != null && obatCek.getStok() < jumlahDibeli) {
                    throw new SQLException("Stok obat '" + obatCek.getNama() + "' (ID: " + idObat + ") tidak mencukupi. Tersisa: " + obatCek.getStok() + ", Dibutuhkan: " + jumlahDibeli);
                } else if (obatCek == null) {
                    throw new SQLException("Obat dengan ID " + idObat + " tidak ditemukan untuk update stok.");
                } else {
                    // Kasus lain yang mungkin (jarang terjadi jika query benar)
                    throw new SQLException("Gagal update stok obat ID " + idObat + " karena alasan yang tidak diketahui (affectedRows=0).");
                }
            }
        } catch (SQLException e) {
            // Jika error bukan karena validasi stok di atas (misal, masalah koneksi, deadlock, dll)
            System.err.println("Error SQL dalam updateStokWithConnection ID " + idObat + ": " + e.getMessage());
            throw e; // Lempar ulang exception
        }
        return result; // Seharusnya selalu true jika tidak ada exception
    }

}
