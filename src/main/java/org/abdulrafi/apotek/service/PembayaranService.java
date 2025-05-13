// src/org/abdulrafi/apotek/service/PembayaranService.java
package org.abdulrafi.apotek.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.abdulrafi.apotek.dao.ObatDAO;
import org.abdulrafi.apotek.dao.ResepDAO;
import org.abdulrafi.apotek.dao.TransaksiDAO;
import org.abdulrafi.apotek.model.apoteker;
import org.abdulrafi.apotek.model.detailresep;
import org.abdulrafi.apotek.model.detailtransaksi;
import org.abdulrafi.apotek.model.obat;
import org.abdulrafi.apotek.model.pasien;
import org.abdulrafi.apotek.model.resep;
import org.abdulrafi.apotek.model.transaksi;

public class PembayaranService {

    private TransaksiDAO transaksiDAO;
    private ResepDAO resepDAO; // Diperlukan jika ingin menyimpan resep setelah transaksi
    private ObatDAO obatDAO; // Diperlukan untuk mendapatkan harga jika memproses resep
    private Scanner scanner;

    public PembayaranService(Scanner scanner) {
        this.transaksiDAO = new TransaksiDAO();
        this.resepDAO = new ResepDAO();
        this.obatDAO = new ObatDAO();
        this.scanner = scanner;
    }

    /**
     * Memproses pembayaran untuk pembelian langsung.
     * @param apoteker Apoteker yang sedang login.
     * @param pasien Pasien yang bertransaksi (bisa null).
     * @param detailTransaksiList Daftar item yang dibeli.
     * @return true jika transaksi berhasil disimpan, false jika gagal.
     */
    public boolean prosesPembayaranLangsung(apoteker apoteker, pasien pasien, List<detailtransaksi> detailTransaksiList) {
        if (detailTransaksiList == null || detailTransaksiList.isEmpty()) {
            System.err.println("Tidak ada item untuk dibayar.");
            return false;
        }

        // 1. Hitung Total Awal
        BigDecimal totalAwal = BigDecimal.ZERO;
        for (detailtransaksi dt : detailTransaksiList) {
            totalAwal = totalAwal.add(dt.getSubTotalObat());
        }
        System.out.println("\n--- Proses Pembayaran ---");
        System.out.println("Total Belanja: Rp" + totalAwal.toPlainString());

        // 2. Input Diskon
        System.out.print("Masukkan diskon (Rp) [Enter jika tidak ada]: ");
        String diskonStr = scanner.nextLine();
        BigDecimal diskon = BigDecimal.ZERO;
        if (!diskonStr.trim().isEmpty()) {
            try {
                diskon = new BigDecimal(diskonStr).setScale(2, RoundingMode.HALF_UP);
                if (diskon.compareTo(BigDecimal.ZERO) < 0) {
                    System.err.println("Diskon tidak boleh negatif.");
                    diskon = BigDecimal.ZERO;
                }
            } catch (NumberFormatException e) {
                System.err.println("Format diskon tidak valid.");
            }
        }

        // 3. Hitung Total Akhir
        BigDecimal totalAkhir = totalAwal.subtract(diskon);
        if (totalAkhir.compareTo(BigDecimal.ZERO) < 0) {
            totalAkhir = BigDecimal.ZERO; // Total tidak boleh negatif
        }
        System.out.println("Diskon: Rp" + diskon.toPlainString());
        System.out.println("Total Setelah Diskon: Rp" + totalAkhir.toPlainString());

        // 4. Input Jumlah Bayar
        BigDecimal jumlahBayar = BigDecimal.ZERO;
        BigDecimal kembalian = BigDecimal.ZERO;
        boolean pembayaranCukup = false;
        while(!pembayaranCukup){
            System.out.print("Masukkan jumlah uang yang dibayar: Rp");
            String bayarStr = scanner.nextLine();
             try {
                jumlahBayar = new BigDecimal(bayarStr).setScale(2, RoundingMode.HALF_UP);
                if(jumlahBayar.compareTo(totalAkhir) >= 0){
                    pembayaranCukup = true;
                    kembalian = jumlahBayar.subtract(totalAkhir);
                } else {
                    System.err.println("Jumlah uang yang dibayarkan kurang.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Format jumlah bayar tidak valid.");
            }
        }

        System.out.println("Jumlah Dibayar: Rp" + jumlahBayar.toPlainString());
        System.out.println("Kembalian: Rp" + kembalian.toPlainString());

        // 5. Buat objek Transaksi
        transaksi transaksi = new transaksi();
        if (pasien != null) {
            transaksi.setPasienId(pasien.getId());
        }
        transaksi.setResepId(null); // Pembelian langsung
        transaksi.setTotalTransaksi(totalAkhir);
        transaksi.setJumlahDibayar(jumlahBayar);
        transaksi.setKembalian(kembalian);
        transaksi.setTanggalTransaksi(new Date()); // Waktu saat ini
        transaksi.setIdApoteker(apoteker != null ? apoteker.getId() : null); // ID apoteker yang login
        transaksi.setDetailTransaksiList(detailTransaksiList); // Lampirkan detailnya

        // 6. Simpan Transaksi (Ini akan update stok juga via DAO)
        System.out.println("Menyimpan transaksi...");
        int newTransaksiId = transaksiDAO.addTransaksi(transaksi);

        if (newTransaksiId != -1) {
            System.out.println("Transaksi berhasil disimpan dengan ID: " + newTransaksiId);
            System.out.println("--- Transaksi Selesai ---");
            return true;
        } else {
            System.err.println("Gagal menyimpan transaksi ke database.");
            return false;
        }
    }
    
     /**
     * Memproses pembayaran untuk penjualan berdasarkan resep.
     * @param apoteker Apoteker yang sedang login.
     * @param resep Objek Resep yang sudah berisi detail obat.
     * @return true jika transaksi berhasil disimpan, false jika gagal.
     */
    public boolean prosesPembayaranResep(apoteker apoteker, resep resep) {
         if (resep == null || resep.getDetailResepList() == null || resep.getDetailResepList().isEmpty()) {
            System.err.println("Data resep tidak valid atau kosong.");
            return false;
        }

         // 1. Hitung Total Awal berdasarkan harga obat saat ini dari detail resep
         BigDecimal totalAwal = BigDecimal.ZERO;
         List<detailtransaksi> detailTransaksiListForRecord = new ArrayList<>();

         for (detailresep dr : resep.getDetailResepList()) {
             obat obatInfo = obatDAO.getObatById(dr.getObatId());
             if (obatInfo == null) {
                 System.err.println("ERROR Kritis: Obat ID " + dr.getObatId() + " dari resep tidak ditemukan di database.");
                 // Seharusnya tidak terjadi jika divalidasi saat input resep
                 return false;
             }
             BigDecimal hargaSaatIni = obatInfo.getHarga();
             BigDecimal subTotal = hargaSaatIni.multiply(BigDecimal.valueOf(dr.getJumlah()));
             totalAwal = totalAwal.add(subTotal);

             // Buat DetailTransaksi untuk disimpan (mencatat harga saat itu)
             detailtransaksi dt = new detailtransaksi();
             dt.setObatId(dr.getObatId());
             dt.setJumlah(dr.getJumlah());
             dt.setHargaSaatTransaksi(hargaSaatIni);
             dt.setSubTotalObat(subTotal);
             detailTransaksiListForRecord.add(dt);
         }

         System.out.println("\n--- Proses Pembayaran Resep ---");
         System.out.println("Total Belanja (Resep): Rp" + totalAwal.toPlainString());

        // 2. Input Diskon (Sama seperti pembelian langsung)
        System.out.print("Masukkan diskon (Rp) [Enter jika tidak ada]: ");
        String diskonStr = scanner.nextLine();
        BigDecimal diskon = BigDecimal.ZERO;
        if (!diskonStr.trim().isEmpty()) {
            try {
                diskon = new BigDecimal(diskonStr).setScale(2, RoundingMode.HALF_UP);
                if (diskon.compareTo(BigDecimal.ZERO) < 0) diskon = BigDecimal.ZERO;
            } catch (NumberFormatException e) { /* abaikan */ }
        }

        // 3. Hitung Total Akhir
        BigDecimal totalAkhir = totalAwal.subtract(diskon);
        if (totalAkhir.compareTo(BigDecimal.ZERO) < 0) totalAkhir = BigDecimal.ZERO;
        System.out.println("Diskon: Rp" + diskon.toPlainString());
        System.out.println("Total Setelah Diskon: Rp" + totalAkhir.toPlainString());

        // 4. Input Jumlah Bayar (Sama seperti pembelian langsung)
        BigDecimal jumlahBayar = BigDecimal.ZERO;
        BigDecimal kembalian = BigDecimal.ZERO;
        boolean pembayaranCukup = false;
        while(!pembayaranCukup){
            System.out.print("Masukkan jumlah uang yang dibayar: Rp");
            String bayarStr = scanner.nextLine();
             try {
                jumlahBayar = new BigDecimal(bayarStr).setScale(2, RoundingMode.HALF_UP);
                if(jumlahBayar.compareTo(totalAkhir) >= 0){
                    pembayaranCukup = true;
                    kembalian = jumlahBayar.subtract(totalAkhir);
                } else { System.err.println("Jumlah uang yang dibayarkan kurang."); }
            } catch (NumberFormatException e) { System.err.println("Format jumlah bayar tidak valid."); }
        }

        System.out.println("Jumlah Dibayar: Rp" + jumlahBayar.toPlainString());
        System.out.println("Kembalian: Rp" + kembalian.toPlainString());

        // 5. Simpan Resep (jika belum disimpan) atau dapatkan IDnya
        // Asumsi: Resep belum disimpan ke DB sampai pembayaran.
        System.out.println("Menyimpan data resep...");
        int resepId = resepDAO.addResep(resep); // addResep harus mengembalikan ID
        if (resepId == -1) {
            System.err.println("Gagal menyimpan data resep ke database.");
            return false; // Transaksi tidak bisa dilanjutkan jika resep gagal disimpan
        }
        System.out.println("Resep berhasil disimpan dengan ID: " + resepId);


        // 6. Buat objek Transaksi
        transaksi transaksi = new transaksi();
        transaksi.setPasienId(resep.getPasienId());
        transaksi.setResepId(resepId); // Set ID Resep yang baru disimpan
        transaksi.setTotalTransaksi(totalAkhir);
        transaksi.setJumlahDibayar(jumlahBayar);
        transaksi.setKembalian(kembalian);
        transaksi.setTanggalTransaksi(new Date());
        transaksi.setIdApoteker(apoteker != null ? apoteker.getId() : null);
        // Set detail transaksi yang mencatat harga saat itu
        transaksi.setDetailTransaksiList(detailTransaksiListForRecord);

        // 7. Simpan Transaksi (Ini akan update stok juga via DAO)
        System.out.println("Menyimpan transaksi...");
        int newTransaksiId = transaksiDAO.addTransaksi(transaksi);

         if (newTransaksiId != -1) {
            System.out.println("Transaksi berhasil disimpan dengan ID: " + newTransaksiId);
            System.out.println("--- Transaksi Resep Selesai ---");
            return true;
        } else {
            System.err.println("Gagal menyimpan transaksi ke database.");
            // Idealnya, coba hapus resep yang sudah terlanjur disimpan jika transaksi gagal?
            return false;
        }
    }
}