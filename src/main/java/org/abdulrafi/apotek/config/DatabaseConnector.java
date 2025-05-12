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
        if (connection == null) {
            try {
                // Mendaftarkan driver JDBC MySQL
                // Class.forName("com.mysql.cj.jdbc.Driver"); // Untuk Connector/J versi 8+
                // Untuk versi lebih lama mungkin: Class.forName("com.mysql.jdbc.Driver");

                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                // System.out.println("Koneksi ke database berhasil!"); // Untuk testing awal
            } catch (SQLException e) {
                System.err.println("Koneksi ke database gagal: " + e.getMessage());
                // e.printStackTrace(); // Tampilkan stack trace untuk debug detail
                // Sebaiknya lempar custom exception atau tangani dengan lebih baik di aplikasi nyata
            } /*catch (ClassNotFoundException e) { // Aktifkan jika menggunakan Class.forName()
                System.err.println("Driver MySQL tidak ditemukan: " + e.getMessage());
            }*/
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