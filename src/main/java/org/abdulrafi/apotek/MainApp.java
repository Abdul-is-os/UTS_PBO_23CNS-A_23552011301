package org.abdulrafi.apotek;

import java.util.List;
import java.util.Scanner;

import org.abdulrafi.apotek.config.DatabaseConnector;
import org.abdulrafi.apotek.model.apoteker;
import org.abdulrafi.apotek.model.detailtransaksi;
import org.abdulrafi.apotek.model.dokter;
import org.abdulrafi.apotek.model.pasien;
import org.abdulrafi.apotek.model.resep;
import org.abdulrafi.apotek.service.AuthService;
import org.abdulrafi.apotek.service.LaporanService;
import org.abdulrafi.apotek.service.PembayaranService;
import org.abdulrafi.apotek.service.PenjualanService;

public class MainApp {

    private static Scanner scanner = new Scanner(System.in);
    private static AuthService authService = new AuthService();
    private static PenjualanService penjualanService = new PenjualanService(scanner);
    private static PembayaranService pembayaranService = new PembayaranService(scanner);
    private static LaporanService laporanService = new LaporanService();

    private static apoteker apotekerLogin = null;

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("SELAMAT DATANG DI SISTEM KASIR APOTEK");
        System.out.println("======================================");

        boolean running = true;
        while (running) {
            if (apotekerLogin == null) {
                tampilkanMenuUtama();
                System.out.print("Pilih opsi: ");
                String pilihan = scanner.nextLine();
                switch (pilihan) {
                    case "1":
                        prosesLogin();
                        break;
                    case "2":
                        System.out.println("Fitur Register belum diimplementasikan.");
                        // prosesRegister(); // Jika ingin diimplementasikan
                        break;
                    case "3":
                        running = false;
                        System.out.println("Terima kasih telah menggunakan aplikasi ini.");
                        break;
                    default:
                        System.err.println("Pilihan tidak valid. Silakan coba lagi.");
                }
            } else {
                tampilkanMenuKasir();
                System.out.print("Pilih opsi kasir: ");
                String pilihanKasir = scanner.nextLine();
                switch (pilihanKasir.toUpperCase()) {
                    case "A":
                        menuPenjualan();
                        break;
                    case "B":
                        laporanService.tampilkanSemuaTransaksi();
                        break;
                    case "C":
                        apotekerLogin = null; // Logout
                        System.out.println("Anda telah logout.");
                        break;
                    default:
                        System.err.println("Pilihan kasir tidak valid.");
                }
            }
            if (running) {
                System.out.println("\nTekan Enter untuk melanjutkan...");
                scanner.nextLine(); // Memberi jeda
            }
        }
        scanner.close();
        DatabaseConnector.closeConnection(); // Tutup koneksi DB saat aplikasi keluar
    }

    private static void tampilkanMenuUtama() {
        System.out.println("\n--- MENU UTAMA ---");
        System.out.println("1. Login");
        System.out.println("2. Register (Belum Tersedia)");
        System.out.println("3. Keluar Aplikasi");
    }

    private static void prosesLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Masukkan Username (nama apoteker): ");
        String username = scanner.nextLine();
        System.out.print("Masukkan Password: ");
        String password = scanner.nextLine(); // Password saat ini belum divalidasi dengan benar

        apotekerLogin = authService.login(username, password);
        // Pesan sukses/gagal sudah dihandle di AuthService
    }

    private static void tampilkanMenuKasir() {
        System.out.println("\n--- HALAMAN KASIR ---");
        System.out.println("Selamat datang, " + (apotekerLogin != null ? apotekerLogin.getNama() : "Apoteker") + "!");
        System.out.println("A. Penjualan");
        System.out.println("B. Data Penjualan (Laporan)");
        System.out.println("C. Logout");
    }

    private static void menuPenjualan() {
        System.out.println("\n--- OPSI PENJUALAN ---");
        System.out.println("1. Dengan Resep Dokter");
        System.out.println("2. Pembelian Langsung");
        System.out.println("0. Kembali ke Menu Kasir");
        System.out.print("Pilih jenis penjualan: ");
        String pilihan = scanner.nextLine();

        switch (pilihan) {
            case "1":
                prosesPenjualanDenganResep();
                break;
            case "2":
                prosesPembelianLangsung();
                break;
            case "0":
                return; // Kembali
            default:
                System.err.println("Pilihan jenis penjualan tidak valid.");
        }
    }

    private static void prosesPembelianLangsung() {
        System.out.println("\n--- PENJUALAN: PEMBELIAN LANGSUNG ---");
        System.out.print("Masukkan Nama Pasien (kosongkan jika umum): ");
        String namaPasien = scanner.nextLine();
        pasien pasien = null;
        if (!namaPasien.trim().isEmpty()) {
            pasien = penjualanService.getOrAddPasien(namaPasien);
            if (pasien == null && !namaPasien.trim().isEmpty()) { // Jika getOrAddPasien gagal tapi nama diisi
                System.err.println("Gagal memproses data pasien. Pembelian dibatalkan.");
                return;
            }
        } else {
            System.out.println("Transaksi untuk pasien umum.");
        }

        List<detailtransaksi> detailBeli = penjualanService.mulaiPembelianLangsung(pasien);

        if (detailBeli != null && !detailBeli.isEmpty()) {
            pembayaranService.prosesPembayaranLangsung(apotekerLogin, pasien, detailBeli);
        } else {
            System.out.println("Tidak ada item yang dibeli. Pembelian dibatalkan.");
        }
    }

    private static void prosesPenjualanDenganResep() {
        System.out.println("\n--- PENJUALAN: DENGAN RESEP DOKTER ---");
        System.out.print("Masukkan Nama Pasien: ");
        String namaPasien = scanner.nextLine();
        pasien pasien = penjualanService.getOrAddPasien(namaPasien);
        if (pasien == null) {
            System.err.println("Gagal memproses data pasien. Penjualan resep dibatalkan.");
            return;
        }

        System.out.print("Masukkan Nama Dokter: ");
        String namaDokter = scanner.nextLine();
        dokter dokter = penjualanService.getDokterByName(namaDokter);
        if (dokter == null) {
            System.err.println("Gagal memproses data dokter. Penjualan resep dibatalkan.");
            return;
        }

        resep resep = penjualanService.mulaiPenjualanResep(pasien, dokter);

        if (resep != null && resep.getDetailResepList() != null && !resep.getDetailResepList().isEmpty()) {
            pembayaranService.prosesPembayaranResep(apotekerLogin, resep);
        } else {
            System.out.println("Tidak ada item pada resep atau resep gagal dibuat. Penjualan dibatalkan.");
        }
    }
}
