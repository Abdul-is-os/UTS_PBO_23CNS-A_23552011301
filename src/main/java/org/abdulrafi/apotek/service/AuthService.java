// src/org/abdulrafi/apotek/service/AuthService.java
package org.abdulrafi.apotek.service;

import org.abdulrafi.apotek.dao.TenagaMedisDAO;
import org.abdulrafi.apotek.model.apoteker;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final TenagaMedisDAO tenagaMedisDAO;

    public AuthService() {
        this.tenagaMedisDAO = new TenagaMedisDAO();
    }

    public apoteker login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("Username dan password tidak boleh kosong.");
            return null;
        }

        // Ganti getApotekerByNama menjadi getApotekerByUsername
        apoteker apoteker = tenagaMedisDAO.getApotekerByUsername(username);

        if (apoteker == null) {
            System.err.println("Login gagal: Username '" + username + "' tidak ditemukan atau bukan Apoteker.");
            return null;
        }

        // Validasi Password menggunakan BCrypt
        if (apoteker.getPasswordHash() == null || !password.equals(apoteker.getPasswordHash())) { // Modifikasi jika password di DB adalah plain text
            System.err.println("Login gagal: Password salah.");
            return null;
        }

        System.out.println("Login berhasil! Selamat datang, " + apoteker.getNama());
        return apoteker;
    }

    /**
     * Contoh metode untuk register Apoteker (perlu ditambahkan ke menu CLI)
     *
     * @return true jika berhasil register, false jika gagal.
     */
    public boolean registerApoteker(String nama, String username, String password) {
        if (nama == null || nama.trim().isEmpty()
                || username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            System.err.println("Nama, username, dan password tidak boleh kosong untuk registrasi.");
            return false;
        }

        // Cek apakah username sudah ada
        if (tenagaMedisDAO.getApotekerByUsername(username) != null) {
            System.err.println("Registrasi gagal: Username '" + username + "' sudah digunakan.");
            return false;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        apoteker apotekerBaru = new apoteker(0, nama, username, hashedPassword); // ID 0 akan di-generate DB

        boolean berhasil = tenagaMedisDAO.addTenagaMedis(apotekerBaru);
        if (berhasil) {
            System.out.println("Apoteker '" + nama + "' berhasil diregistrasi dengan username '" + username + "'.");
        } else {
            System.err.println("Registrasi gagal karena masalah database.");
        }
        return berhasil;
    }
}
