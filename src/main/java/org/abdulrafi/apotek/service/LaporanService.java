package org.abdulrafi.apotek.service;

import java.text.SimpleDateFormat;
import java.util.List;

import org.abdulrafi.apotek.dao.ObatDAO;
import org.abdulrafi.apotek.dao.TransaksiDAO; // Untuk get nama obat di detail
import org.abdulrafi.apotek.model.detailtransaksi;
import org.abdulrafi.apotek.model.obat;
import org.abdulrafi.apotek.model.transaksi;

public class LaporanService {

    private final TransaksiDAO transaksiDAO;
    private final ObatDAO obatDAO; // Untuk mengambil nama obat

    public LaporanService() {
        this.transaksiDAO = new TransaksiDAO();
        this.obatDAO = new ObatDAO();
    }

    /**
     * Menampilkan semua data transaksi ke konsol.
     */
    public void tampilkanSemuaTransaksi() {
        System.out.println("\n--- Laporan Semua Transaksi ---");
        List<transaksi> daftarTransaksi = transaksiDAO.getAllTransaksi();

        if (daftarTransaksi.isEmpty()) {
            System.out.println("Belum ada data transaksi.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        // Header Tabel Transaksi Utama
        System.out.printf("%-5s %-20s %-10s %-10s %-15s %-15s %-15s %-10s%n",
                "ID", "Tanggal", "PasienID", "ResepID", "Total", "Dibayar", "Kembali", "ApotekerID");
        System.out.println("------------------------------------------------------------------------------------------------------------");

        for (transaksi t : daftarTransaksi) {
            System.out.printf("%-5d %-20s %-10s %-10s Rp %-12s Rp %-12s Rp %-12s %-10s%n",
                    t.getId(),
                    dateFormat.format(t.getTanggalTransaksi()),
                    t.getPasienId() != null ? String.valueOf(t.getPasienId()) : "N/A",
                    t.getResepId() != null ? String.valueOf(t.getResepId()) : "N/A",
                    t.getTotalTransaksi().toPlainString(),
                    t.getJumlahDibayar().toPlainString(),
                    t.getKembalian().toPlainString(),
                    t.getIdApoteker() != null ? String.valueOf(t.getIdApoteker()) : "N/A");

            // Tampilkan Detail Transaksi untuk setiap transaksi
            if (t.getDetailTransaksiList() != null && !t.getDetailTransaksiList().isEmpty()) {
                System.out.println("  Detail:");
                System.out.printf("  %-5s %-30s %-10s %-15s %-15s%n", " DtlID", "Obat", "Jumlah", "Harga Satuan", "Sub Total");
                 System.out.println("  ---------------------------------------------------------------------------");
                for (detailtransaksi dt : t.getDetailTransaksiList()) {
                    // Ambil nama obat berdasarkan ID
                    obat obatInfo = obatDAO.getObatById(dt.getObatId());
                    String namaObat = obatInfo != null ? obatInfo.getNama() : "Obat ID:" + dt.getObatId();

                    System.out.printf("  %-5d %-30s %-10d Rp %-12s Rp %-12s%n",
                            dt.getId(),
                            namaObat,
                            dt.getJumlah(),
                            dt.getHargaSaatTransaksi().toPlainString(),
                            dt.getSubTotalObat().toPlainString());
                }
                 System.out.println("  ---------------------------------------------------------------------------");
            }
             System.out.println(); // Baris kosong antar transaksi
        }
         System.out.println("------------------------------------------------------------------------------------------------------------");

    }

    // Anda bisa menambahkan metode lain, misalnya tampilkanTransaksiByTanggal(Date start, Date end)
}