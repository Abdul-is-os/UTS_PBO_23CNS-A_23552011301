// src/org/abdulrafi/apotek/dao/TransaksiDAO.java
package org.abdulrafi.apotek.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.abdulrafi.apotek.config.DatabaseConnector;
import org.abdulrafi.apotek.model.detailtransaksi;
import org.abdulrafi.apotek.model.obat;
import org.abdulrafi.apotek.model.transaksi;

public class TransaksiDAO {
    private ObatDAO obatDAO; // Untuk update stok

    public TransaksiDAO() {
        this.obatDAO = new ObatDAO(); // Inisialisasi ObatDAO
    }

    /**
     * Menambahkan transaksi baru beserta detailnya dan memperbarui stok obat.
     * Ini seharusnya idealnya dijalankan dalam satu transaksi database.
     * @param transaksi Objek Transaksi yang berisi detail transaksi.
     * @return ID transaksi yang baru dibuat jika berhasil, -1 jika gagal.
     */
    public int addTransaksi(transaksi transaksi) {
        String sqlTransaksi = "INSERT INTO kasir_apotek_pbo2_transaksi " +
                              "(pasien_id, resep_id, total_transaksi, jumlah_dibayar, kembalian, tanggal_transaksi, id_apoteker) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetailTransaksi = "INSERT INTO kasir_apotek_pbo2_detail_transaksi " +
                                    "(transaksi_id, obat_id, jumlah, harga_saat_transaksi, sub_total_obat) " +
                                    "VALUES (?, ?, ?, ?, ?)";
        int generatedTransaksiId = -1;
        Connection conn = null;

        if (transaksi == null || transaksi.getTotalTransaksi() == null || transaksi.getJumlahDibayar() == null ||
            transaksi.getKembalian() == null || transaksi.getTanggalTransaksi() == null ||
            (transaksi.getResepId() == null && (transaksi.getDetailTransaksiList() == null || transaksi.getDetailTransaksiList().isEmpty())) ) {
            System.err.println("Data transaksi tidak valid atau detail transaksi kosong (untuk non-resep).");
            return -1;
        }


        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi

            // 1. Insert ke tabel transaksi
            try (PreparedStatement pstmtTransaksi = conn.prepareStatement(sqlTransaksi, Statement.RETURN_GENERATED_KEYS)) {
                // Handle nullable foreign keys
                if (transaksi.getPasienId() != null && transaksi.getPasienId() > 0) {
                    pstmtTransaksi.setInt(1, transaksi.getPasienId());
                } else {
                    pstmtTransaksi.setNull(1, java.sql.Types.INTEGER);
                }
                if (transaksi.getResepId() != null && transaksi.getResepId() > 0) {
                    pstmtTransaksi.setInt(2, transaksi.getResepId());
                } else {
                    pstmtTransaksi.setNull(2, java.sql.Types.INTEGER);
                }
                pstmtTransaksi.setBigDecimal(3, transaksi.getTotalTransaksi());
                pstmtTransaksi.setBigDecimal(4, transaksi.getJumlahDibayar());
                pstmtTransaksi.setBigDecimal(5, transaksi.getKembalian());
                pstmtTransaksi.setTimestamp(6, new java.sql.Timestamp(transaksi.getTanggalTransaksi().getTime())); // Konversi ke Timestamp
                
                if (transaksi.getIdApoteker() != null && transaksi.getIdApoteker() > 0) {
                    pstmtTransaksi.setInt(7, transaksi.getIdApoteker());
                } else {
                     // Jika tidak ada ID apoteker (misal sistem tanpa login), bisa set NULL atau ID default
                    pstmtTransaksi.setNull(7, java.sql.Types.INTEGER);
                }


                int affectedRows = pstmtTransaksi.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Pembuatan transaksi gagal, tidak ada baris yang terpengaruh.");
                }

                try (ResultSet generatedKeys = pstmtTransaksi.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedTransaksiId = generatedKeys.getInt(1);
                        transaksi.setId(generatedTransaksiId);
                    } else {
                        throw new SQLException("Pembuatan transaksi gagal, tidak mendapatkan ID.");
                    }
                }
            }

            // 2. Insert ke tabel detail_transaksi (jika ada dan bukan dari resep yang sudah ada detailnya)
            // Jika transaksi berdasarkan resep, detail obatnya sudah ada di detail_resep.
            // Jika pembelian langsung, detailnya harus ada di transaksi.getDetailTransaksiList().
            if (transaksi.getDetailTransaksiList() != null && !transaksi.getDetailTransaksiList().isEmpty()) {
                try (PreparedStatement pstmtDetail = conn.prepareStatement(sqlDetailTransaksi)) {
                    for (detailtransaksi detail : transaksi.getDetailTransaksiList()) {
                        if(detail.getObatId() <= 0 || detail.getJumlah() <=0 || detail.getHargaSaatTransaksi() == null || detail.getSubTotalObat() == null) {
                            throw new SQLException("Data detail transaksi tidak valid.");
                        }
                        pstmtDetail.setInt(1, generatedTransaksiId);
                        pstmtDetail.setInt(2, detail.getObatId());
                        pstmtDetail.setInt(3, detail.getJumlah());
                        pstmtDetail.setBigDecimal(4, detail.getHargaSaatTransaksi());
                        pstmtDetail.setBigDecimal(5, detail.getSubTotalObat());
                        pstmtDetail.addBatch();

                        // 3. Update stok obat (harus berhasil untuk setiap item)
                        // Menggunakan koneksi yang sama dalam transaksi
                        boolean stokUpdated = obatDAO.updateStokWithConnection(conn, detail.getObatId(), detail.getJumlah());
                        if (!stokUpdated) {
                            obat obatInfo = obatDAO.getObatByIdWithConnection(conn, detail.getObatId());
                            String namaObat = obatInfo != null ? obatInfo.getNama() : "ID " + detail.getObatId();
                            throw new SQLException("Gagal memperbarui stok untuk obat: " + namaObat + ". Stok mungkin tidak mencukupi.");
                        }
                    }
                    pstmtDetail.executeBatch();
                }
            } else if (transaksi.getResepId() != null) {
                // Jika dari resep, stok obat dari resep perlu diupdate
                // Asumsi: ResepDAO atau Service layer telah mengambil detail resep
                // Ini bagian yang sedikit tricky, bagaimana detail resep di-pass ke sini
                // atau apakah update stok resep dilakukan di service layer.
                // Untuk DAO, kita bisa tambahkan method untuk mengambil detail resep
                // dan kemudian loop untuk update stok. Atau service yang melakukannya.
                // Contoh sederhana: kita asumsikan ini sudah di-handle atau detail resep di-pass.
                System.out.println("Transaksi dari resep ID: " + transaksi.getResepId() + ". Pastikan stok obat dari resep ini diupdate secara terpisah atau melalui service.");
                // Jika ingin update stok dari resep di sini, kita butuh akses ke detail resep.
                // ResepDAO resepDAO = new ResepDAO();
                // Resep resepTerkait = resepDAO.getResepByIdWithConnection(conn, transaksi.getResepId());
                // if (resepTerkait != null && resepTerkait.getDetailResepList() != null) {
                //     for (DetailResep dr : resepTerkait.getDetailResepList()) {
                //         boolean stokUpdated = obatDAO.updateStokWithConnection(conn, dr.getObatId(), dr.getJumlah());
                //         if (!stokUpdated) throw new SQLException("Gagal update stok obat dari resep.");
                //     }
                // }
            }


            conn.commit();
            System.out.println("Transaksi ID " + generatedTransaksiId + " dan detailnya berhasil ditambahkan.");

        } catch (SQLException e) {
            System.err.println("Error saat menambahkan transaksi: " + e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaksi dibatalkan (rollback).");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error saat rollback: " + ex.getMessage());
                }
            }
            generatedTransaksiId = -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error saat menutup koneksi setelah addTransaksi: " + e.getMessage());
                }
            }
        }
        return generatedTransaksiId;
    }

    // Perlu method di ObatDAO: updateStokWithConnection dan getObatByIdWithConnection
    // agar bisa memakai koneksi yang sama dalam satu transaksi.
    // Contoh di ObatDAO:
    /*
    public boolean updateStokWithConnection(Connection conn, int idObat, int jumlahDibeli) throws SQLException {
        String sql = "UPDATE kasir_apotek_pbo2_obat SET stok = stok - ? WHERE id = ? AND stok >= ?";
        boolean result = false;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jumlahDibeli);
            pstmt.setInt(2, idObat);
            pstmt.setInt(3, jumlahDibeli);
            int affectedRows = pstmt.executeUpdate();
            result = affectedRows > 0;
        }
        return result;
    }
    public Obat getObatByIdWithConnection(Connection conn, int id) throws SQLException {
        // ... implementasi mirip getObatById tapi pakai conn yang di-pass
    }
    */


    private List<detailtransaksi> getDetailTransaksiByTransaksiId(int transaksiId, Connection conn) throws SQLException {
        List<detailtransaksi> detailList = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_detail_transaksi WHERE transaksi_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transaksiId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    detailtransaksi detail = new detailtransaksi();
                    detail.setId(rs.getInt("id"));
                    detail.setTransaksiId(rs.getInt("transaksi_id"));
                    detail.setObatId(rs.getInt("obat_id"));
                    detail.setJumlah(rs.getInt("jumlah"));
                    detail.setHargaSaatTransaksi(rs.getBigDecimal("harga_saat_transaksi"));
                    detail.setSubTotalObat(rs.getBigDecimal("sub_total_obat"));
                    detailList.add(detail);
                }
            }
        }
        return detailList;
    }

    public transaksi getTransaksiById(int id) {
        transaksi transaksi = null;
        String sql = "SELECT * FROM kasir_apotek_pbo2_transaksi WHERE id = ?";

        try (Connection conn = DatabaseConnector.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        transaksi = new transaksi();
                        transaksi.setId(rs.getInt("id"));
                        transaksi.setPasienId(rs.getObject("pasien_id") != null ? rs.getInt("pasien_id") : null);
                        transaksi.setResepId(rs.getObject("resep_id") != null ? rs.getInt("resep_id") : null);
                        transaksi.setTotalTransaksi(rs.getBigDecimal("total_transaksi"));
                        transaksi.setJumlahDibayar(rs.getBigDecimal("jumlah_dibayar"));
                        transaksi.setKembalian(rs.getBigDecimal("kembalian"));
                        transaksi.setTanggalTransaksi(rs.getTimestamp("tanggal_transaksi")); // Mengambil sebagai Timestamp
                        transaksi.setIdApoteker(rs.getObject("id_apoteker") != null ? rs.getInt("id_apoteker") : null);

                        // Ambil detail transaksi jika bukan dari resep yang detailnya sudah ada
                        // atau jika kita selalu ingin menampilkan detail dari tabel detail_transaksi
                        List<detailtransaksi> detailList = getDetailTransaksiByTransaksiId(id, conn);
                        transaksi.setDetailTransaksiList(detailList);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil transaksi by ID " + id + ": " + e.getMessage());
        }
        return transaksi;
    }

    public List<transaksi> getAllTransaksi() {
        List<transaksi> daftarTransaksi = new ArrayList<>();
        String sql = "SELECT * FROM kasir_apotek_pbo2_transaksi ORDER BY tanggal_transaksi DESC, id DESC";

        try (Connection conn = DatabaseConnector.getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    transaksi transaksi = new transaksi();
                    transaksi.setId(rs.getInt("id"));
                    transaksi.setPasienId(rs.getObject("pasien_id") != null ? rs.getInt("pasien_id") : null);
                    transaksi.setResepId(rs.getObject("resep_id") != null ? rs.getInt("resep_id") : null);
                    transaksi.setTotalTransaksi(rs.getBigDecimal("total_transaksi"));
                    transaksi.setJumlahDibayar(rs.getBigDecimal("jumlah_dibayar"));
                    transaksi.setKembalian(rs.getBigDecimal("kembalian"));
                    transaksi.setTanggalTransaksi(rs.getTimestamp("tanggal_transaksi"));
                    transaksi.setIdApoteker(rs.getObject("id_apoteker") != null ? rs.getInt("id_apoteker") : null);

                    List<detailtransaksi> detailList = getDetailTransaksiByTransaksiId(transaksi.getId(), conn);
                    transaksi.setDetailTransaksiList(detailList);
                    daftarTransaksi.add(transaksi);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil semua transaksi: " + e.getMessage());
        }
        return daftarTransaksi;
    }
    
    // Metode lain seperti getTransaksiByTanggal, getTransaksiByPasienId, dll. bisa ditambahkan
}