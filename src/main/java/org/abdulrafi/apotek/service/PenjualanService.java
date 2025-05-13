package org.abdulrafi.apotek.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.abdulrafi.apotek.dao.ObatDAO;
import org.abdulrafi.apotek.dao.PasienDAO;
import org.abdulrafi.apotek.dao.TenagaMedisDAO;
import org.abdulrafi.apotek.model.detailresep;
import org.abdulrafi.apotek.model.detailtransaksi;
import org.abdulrafi.apotek.model.dokter;
import org.abdulrafi.apotek.model.obat;
import org.abdulrafi.apotek.model.pasien;
import org.abdulrafi.apotek.model.resep;

public class PenjualanService {

    private ObatDAO obatDAO;
    private PasienDAO pasienDAO;
    private TenagaMedisDAO tenagaMedisDAO;
    private Scanner scanner;

    public PenjualanService(Scanner scanner) {
        this.obatDAO = new ObatDAO();
        this.pasienDAO = new PasienDAO();
        this.tenagaMedisDAO = new TenagaMedisDAO();
        this.scanner = scanner; // Scanner di-pass dari luar agar bisa di-mock/diganti
    }

    // Struktur untuk menyimpan item yang sedang dibeli sementara
    // Bisa juga dibuat kelas terpisah 'Keranjang' atau 'TransaksiBuilder'
    private static class ItemPenjualan {

        obat obat;
        int jumlah;
        BigDecimal hargaSaatIni;
        BigDecimal subTotal;

        ItemPenjualan(obat obat, int jumlah) {
            this.obat = obat;
            this.jumlah = jumlah;
            this.hargaSaatIni = obat.getHarga(); // Ambil harga saat ini
            this.subTotal = this.hargaSaatIni.multiply(BigDecimal.valueOf(jumlah));
        }
    }

    /**
     * Memulai proses penjualan baru (pembelian langsung). Mengembalikan list
     * item yang siap dibayar.
     */
    public List<detailtransaksi> mulaiPembelianLangsung(pasien pasien) {
        System.out.println("\n--- Memulai Pembelian Langsung ---");
        List<ItemPenjualan> keranjang = new ArrayList<>();
        boolean tambahLagi = true;

        while (tambahLagi) {
            tampilkanDaftarObat();
            System.out.print("Masukkan ID Obat yang ingin dibeli (atau 0 untuk selesai): ");
            int idObat = Integer.parseInt(scanner.nextLine());

            if (idObat == 0) {
                tambahLagi = false;
                continue;
            }

            obat obatDipilih = obatDAO.getObatById(idObat);
            if (obatDipilih == null) {
                System.err.println("Obat dengan ID " + idObat + " tidak ditemukan.");
                continue;
            }

            System.out.print("Masukkan jumlah " + obatDipilih.getNama() + " yang ingin dibeli (Stok: " + obatDipilih.getStok() + "): ");
            int jumlahBeli = Integer.parseInt(scanner.nextLine());

            if (jumlahBeli <= 0) {
                System.err.println("Jumlah beli tidak valid.");
                continue;
            }
            if (jumlahBeli > obatDipilih.getStok()) {
                System.err.println("Stok obat tidak mencukupi. Stok tersedia: " + obatDipilih.getStok());
                continue;
            }

            // Tambahkan ke keranjang sementara
            keranjang.add(new ItemPenjualan(obatDipilih, jumlahBeli));
            System.out.println(obatDipilih.getNama() + " x" + jumlahBeli + " ditambahkan ke keranjang.");
            System.out.println("Total sementara: Rp" + hitungTotalKeranjang(keranjang));

            System.out.print("Tambah obat lain? (y/n): ");
            tambahLagi = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        // Konversi keranjang ke List<DetailTransaksi>
        List<detailtransaksi> detailList = new ArrayList<>();
        for (ItemPenjualan item : keranjang) {
            detailtransaksi dt = new detailtransaksi();
            dt.setObatId(item.obat.getId());
            dt.setJumlah(item.jumlah);
            dt.setHargaSaatTransaksi(item.hargaSaatIni);
            dt.setSubTotalObat(item.subTotal);
            detailList.add(dt);
        }

        System.out.println("--- Selesai Memilih Obat ---");
        return detailList;
    }

    /**
     * Memulai proses penjualan berdasarkan resep. Mengembalikan objek Resep
     * yang siap diproses pembayarannya.
     */
    public resep mulaiPenjualanResep(pasien pasien, dokter dokter) {
        System.out.println("\n--- Memulai Penjualan Dengan Resep ---");
        resep resep = new resep();
        resep.setPasienId(pasien.getId());
        resep.setDokterId(dokter.getId());
        resep.setTanggal(new Date()); // Tanggal hari ini

        List<detailresep> detailResepList = new ArrayList<>();
        boolean tambahLagi = true;

        while (tambahLagi) {
            tampilkanDaftarObat();
            System.out.print("Masukkan ID Obat dari resep (atau 0 untuk selesai): ");
            int idObat = Integer.parseInt(scanner.nextLine());

            if (idObat == 0) {
                tambahLagi = false;
                continue;
            }

            obat obatDipilih = obatDAO.getObatById(idObat);
            if (obatDipilih == null) {
                System.err.println("Obat dengan ID " + idObat + " tidak ditemukan.");
                continue;
            }

            System.out.print("Masukkan jumlah " + obatDipilih.getNama() + " sesuai resep (Stok: " + obatDipilih.getStok() + "): ");
            int jumlahResep = Integer.parseInt(scanner.nextLine());

            if (jumlahResep <= 0) {
                System.err.println("Jumlah resep tidak valid.");
                continue;
            }
            if (jumlahResep > obatDipilih.getStok()) {
                // Untuk resep, mungkin ada kebijakan berbeda (misal tetap proses sebagian)
                // Di sini kita anggap harus cukup
                System.err.println("Stok obat tidak mencukupi. Stok tersedia: " + obatDipilih.getStok());
                continue;
            }

            detailresep dr = new detailresep();
            dr.setObatId(obatDipilih.getId());
            dr.setJumlah(jumlahResep);
            detailResepList.add(dr);
            System.out.println(obatDipilih.getNama() + " x" + jumlahResep + " ditambahkan ke resep.");

            System.out.print("Tambah obat lain dari resep? (y/n): ");
            tambahLagi = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        if (detailResepList.isEmpty()) {
            System.err.println("Tidak ada obat yang dimasukkan untuk resep ini.");
            return null; // Resep kosong tidak valid
        }

        resep.setDetailResepList(detailResepList);
        System.out.println("--- Selesai Memproses Obat Resep ---");
        return resep;
    }

    // Helper methods
    private void tampilkanDaftarObat() {
        System.out.println("\n--- Daftar Obat Tersedia ---");
        List<obat> daftarObat = obatDAO.getAllObat();
        if (daftarObat.isEmpty()) {
            System.out.println("Tidak ada data obat tersedia.");
            return;
        }
        System.out.printf("%-5s %-30s %-15s %-5s%n", "ID", "Nama Obat", "Harga", "Stok");
        System.out.println("---------------------------------------------------------------");
        for (obat obat : daftarObat) {
            System.out.printf("%-5d %-30s Rp %-12s %-5d%n",
                    obat.getId(),
                    obat.getNama(),
                    obat.getHarga().toPlainString(), // Tampilkan harga
                    obat.getStok());
        }
        System.out.println("---------------------------------------------------------------");
    }

    private BigDecimal hitungTotalKeranjang(List<ItemPenjualan> keranjang) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPenjualan item : keranjang) {
            total = total.add(item.subTotal);
        }
        return total;
    }

    // Metode untuk mendapatkan/membuat pasien
    public pasien getOrAddPasien(String namaPasien) {
        List<pasien> foundPasien = pasienDAO.getPasienByNama(namaPasien);
        pasien pasien;
        if (foundPasien.isEmpty()) {
            System.out.println("Pasien '" + namaPasien + "' tidak ditemukan. Membuat data baru.");
            System.out.print("Masukkan umur pasien: ");
            int umur = Integer.parseInt(scanner.nextLine());
            pasien = new pasien(0, namaPasien, umur); // ID 0 akan di-generate DB
            int newId = pasienDAO.addPasien(pasien);
            if (newId == -1) {
                System.err.println("Gagal menambahkan pasien baru.");
                return null;
            }
            // pasien.setId(newId) sudah dihandle di addPasien
        } else if (foundPasien.size() == 1) {
            pasien = foundPasien.get(0);
            System.out.println("Pasien ditemukan: " + pasien.getNama() + " (ID: " + pasien.getId() + ")");
        } else {
            // Jika ada beberapa pasien dengan nama sama
            System.out.println("Ditemukan beberapa pasien dengan nama '" + namaPasien + "':");
            for (int i = 0; i < foundPasien.size(); i++) {
                System.out.println((i + 1) + ". " + foundPasien.get(i));
            }
            System.out.print("Pilih nomor pasien (atau 0 untuk batal): ");
            int pilihan = Integer.parseInt(scanner.nextLine());
            if (pilihan > 0 && pilihan <= foundPasien.size()) {
                pasien = foundPasien.get(pilihan - 1);
            } else {
                return null; // Batal
            }
        }
        return pasien;
    }

    // Metode untuk mendapatkan dokter
    public dokter getDokterByName(String namaDokter) {
        dokter dokter = tenagaMedisDAO.getDokterByNama(namaDokter);
        if (dokter == null) {
            System.err.println("Dokter dengan nama '" + namaDokter + "' tidak ditemukan atau bukan Dokter.");
            // Mungkin perlu opsi menambahkan dokter baru jika diperlukan
            return null;
        }
        System.out.println("Dokter ditemukan: " + dokter.getNama() + " (ID: " + dokter.getId() + ")");
        return dokter;
    }

}
