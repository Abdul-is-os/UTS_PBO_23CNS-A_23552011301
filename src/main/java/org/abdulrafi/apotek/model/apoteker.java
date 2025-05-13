// src/org/abdulrafi/apotek/model/Apoteker.java
package org.abdulrafi.apotek.model;

public class apoteker extends tenagamedis {

    public apoteker() {
        super();
        setTipe(tipetenagamedis.APOTEKER);
    }

    // Constructor untuk membuat objek Apoteker dengan data lengkap (termasuk untuk login)
    public apoteker(int id, String nama, String username, String passwordHash) {
        super(id, nama, tipetenagamedis.APOTEKER, username, passwordHash);
    }

    // Constructor jika hanya butuh ID dan Nama (misal dari hasil query sederhana)
    public apoteker(int id, String nama) {
        super(id, nama, tipetenagamedis.APOTEKER);
    }

    @Override
    public String toString() {
        return "Apoteker{"
                + "id=" + getId()
                + ", nama='" + getNama() + '\''
                + ", username='" + getUsername() + '\''
                + // Tampilkan username
                ", tipe=" + getTipe()
                + '}';
    }
}
