// src/com/apotek/config/DatabaseConnector.java
package org.abdulrafi.apotek.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // Sesuaikan dengan konfigurasi database MySQL Anda
    private static final String URL = "jdbc:mysql://localhost:3306/kasir_apotek_pbo2"; // Ganti nama DB jika beda
    private static final String USER = "root"; // Ganti dengan username DB Anda
    private static final String PASSWORD = ""; // Ganti dengan password DB Anda

    private static Connection connection;

    // Method untuk mendapatkan koneksi
    public static Connection getConnection() {
        try {
            // Selalu cek apakah koneksi null ATAU sudah ditutup
            if (connection == null || connection.isClosed()) {
                // System.out.println("[DEBUG] DatabaseConnector: Membuat koneksi baru."); // Opsional debug
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Koneksi ke database gagal: " + e.getMessage());
            // e.printStackTrace(); // Aktifkan untuk debug detail
            return null; // Kembalikan null jika koneksi gagal
        }
        return connection;
    }

    // Method untuk menutup koneksi (opsional, bisa dipanggil saat aplikasi ditutup)
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Set ke null agar bisa dibuat ulang jika diperlukan
                // System.out.println("Koneksi ke database ditutup.");
            } catch (SQLException e) {
                System.err.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }

    // Main method untuk testing koneksi (opsional)
    public static void main(String[] args) {
        Connection conn = DatabaseConnector.getConnection();
        if (conn != null) {
            System.out.println("Tes koneksi berhasil!");
            DatabaseConnector.closeConnection();
        } else {
            System.out.println("Tes koneksi gagal.");
        }
    }
}
