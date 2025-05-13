package org.abdulrafi.apotek.model;

public class dokter extends tenagamedis {
    // Atribut spesifik Dokter jika ada, misal:
    // private String spesialisasi;

    public dokter() {
        super(); // Memanggil constructor superclass
        setTipe(tipetenagamedis.DOKTER); // Otomatis set tipe saat objek Dokter dibuat
    }

    public dokter(int id, String nama) {
        super(id, nama, tipetenagamedis.DOKTER);
    }

    // Getter dan Setter untuk atribut spesifik Dokter jika ada
    // public String getSpesialisasi() { return spesialisasi; }
    // public void setSpesialisasi(String spesialisasi) { this.spesialisasi = spesialisasi; }
    // Override metode abstrak dari TenagaMedis jika ada
    // @Override
    // public void berikanLayanan() {
    //     System.out.println("Dokter " + getNama() + " memberikan konsultasi medis.");
    // }
    @Override
    public String toString() {
        return "Dokter{"
                + "id=" + getId()
                + ", nama='" + getNama() + '\''
                + ", tipe=" + getTipe()
                + //   (spesialisasi != null ? ", spesialisasi='" + spesialisasi + '\'' : "") +
                '}';
    }
}
