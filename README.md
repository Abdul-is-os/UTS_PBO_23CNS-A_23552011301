UTS Pemrograman Berorientasi Obyek 2

    Mata Kuliah: Pemrograman Berorientasi Obyek 2
    Dosen Pengampu: Muhammad Ikhwan Fathulloh

Profil

    Nama: m. abdul rafi
    NIM: 23552011301
    Studi Kasus: kasir apotek

Judul Studi Kasus

kasir apotek

Penjelasan 4 Pilar OOP dalam Studi Kasus
1.	Inheritance: tenagamedis, dokter, dan apoteker.
2.	Encapsulation: di obat.java
    public obat() {
    }
    // ...

    // Getter dan Setter publik untuk mengakses atribut private
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNama() {
        return nama;
    }
3.	Polimorfisme 
dokter.java
public class dokter extends tenagamedis {
    // ...
    @Override
    public String toString() {
        return "Dokter{"
                + "id=" + getId()
                + ", nama='" + getNama() + '\''
                + ", tipe=" + getTipe()
                + '}';
    }
}


apoteker.java
public class apoteker extends tenagamedis {
    @Override
    public String toString() {
        return "Apoteker{"
                + "id=" + getId()
                + ", nama='" + getNama() + '\''
                + ", username='" + getUsername() + '\''
                + ", tipe=" + getTipe()
                + '}';
    }
}
4.	Abstraction: 
Tenagamedis.java
public abstract class tenagamedis {
    private int id;
    private String nama;


Demo Proyek
https://drive.google.com/file/d/1PV6M5EiiobRIWOEzFqLKDv4NGU0B5RuU/view?usp=drive_link

https://github.com/Abdul-is-os/UTS_PBO_23CNS-A_23552011301
