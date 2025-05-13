-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 13, 2025 at 07:50 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `kasir_apotek_pbo2`
--

-- --------------------------------------------------------

--
-- Table structure for table `kasir_apotek_pbo2_detail_resep`
--

CREATE TABLE `kasir_apotek_pbo2_detail_resep` (
  `id` int(11) NOT NULL,
  `resep_id` int(11) NOT NULL,
  `obat_id` int(11) NOT NULL,
  `jumlah` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `kasir_apotek_pbo2_detail_transaksi`
--

CREATE TABLE `kasir_apotek_pbo2_detail_transaksi` (
  `id` int(11) NOT NULL,
  `transaksi_id` int(11) NOT NULL,
  `obat_id` int(11) NOT NULL,
  `jumlah` int(11) NOT NULL,
  `harga_saat_transaksi` decimal(10,2) NOT NULL,
  `sub_total_obat` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `kasir_apotek_pbo2_obat`
--

CREATE TABLE `kasir_apotek_pbo2_obat` (
  `id` int(11) NOT NULL,
  `nama` varchar(255) NOT NULL,
  `harga` decimal(10,2) NOT NULL,
  `stok` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `kasir_apotek_pbo2_obat`
--

INSERT INTO `kasir_apotek_pbo2_obat` (`id`, `nama`, `harga`, `stok`) VALUES
(1, 'Paracetamol 500mg Tablet', 5000.00, 100),
(2, 'Amoxicillin 500mg Kapsul', 12000.00, 75),
(3, 'Vitamin C 100mg Tablet Hisap', 25000.00, 120),
(4, 'Obat Batuk Sirup Anak 60ml', 18000.00, 50),
(5, 'Obat Flu & Batuk Dewasa Tablet', 9500.00, 80),
(6, 'Antasida Doen Suspensi 100ml', 15000.00, 60),
(7, 'Minyak Kayu Putih 60ml', 22000.00, 90),
(8, 'Betadine Antiseptik Sol 15ml', 13500.00, 40),
(9, 'Hansaplast Plester Roll', 8000.00, 150),
(10, 'Bodrex Migra Tablet', 7000.00, 65),
(11, 'Diapet Kapsul (Obat Diare)', 11000.00, 55),
(12, 'Neurobion Forte Tablet (Vitamin Saraf)', 35000.00, 30),
(13, 'Promag Tablet Kunyah (Obat Maag)', 6000.00, 110),
(14, 'Insto Tetes Mata Regular 7.5ml', 17000.00, 70),
(15, 'OBH Combi Batuk Berdahak Sirup 100ml', 23000.00, 45);

-- --------------------------------------------------------

--
-- Table structure for table `kasir_apotek_pbo2_pasien`
--

CREATE TABLE `kasir_apotek_pbo2_pasien` (
  `id` int(11) NOT NULL,
  `nama` varchar(255) NOT NULL,
  `umur` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `kasir_apotek_pbo2_pasien`
--

INSERT INTO `kasir_apotek_pbo2_pasien` (`id`, `nama`, `umur`) VALUES
(1, 'alphin', 20),
(2, 'alphin', 20),
(3, 'arizal', 22),
(4, 'arizal', 22),
(5, 'abdul', 20),
(6, 'abdul', 20);

-- --------------------------------------------------------

--
-- Table structure for table `kasir_apotek_pbo2_resep`
--

CREATE TABLE `kasir_apotek_pbo2_resep` (
  `id` int(11) NOT NULL,
  `pasien_id` int(11) NOT NULL,
  `dokter_id` int(11) NOT NULL,
  `tanggal` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `kasir_apotek_pbo2_tenaga_medis`
--

CREATE TABLE `kasir_apotek_pbo2_tenaga_medis` (
  `id` int(11) NOT NULL,
  `nama` varchar(255) NOT NULL,
  `tipe` enum('DOKTER','APOTEKER') NOT NULL,
  `username` varchar(100) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `kasir_apotek_pbo2_tenaga_medis`
--

INSERT INTO `kasir_apotek_pbo2_tenaga_medis` (`id`, `nama`, `tipe`, `username`, `password_hash`) VALUES
(6, 'abdul', 'APOTEKER', 'abdul935', 'admin123'),
(7, 'admin', 'APOTEKER', 'admin1', 'PassWordNyaIni123'),
(11, 'abcd', '', 'asdf', 'BulGaria1880'),
(13, 'asdf', 'APOTEKER', 'ASD', 'AsdfASDF123'),
(17, 'wafrr', 'APOTEKER', 'qwerty', 'AsdfASDF123');

-- --------------------------------------------------------

--
-- Table structure for table `kasir_apotek_pbo2_transaksi`
--

CREATE TABLE `kasir_apotek_pbo2_transaksi` (
  `id` int(11) NOT NULL,
  `pasien_id` int(11) DEFAULT NULL,
  `resep_id` int(11) DEFAULT NULL,
  `total_transaksi` decimal(12,2) NOT NULL,
  `jumlah_dibayar` decimal(12,2) NOT NULL,
  `kembalian` decimal(12,2) NOT NULL,
  `tanggal_transaksi` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_apoteker` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `kasir_apotek_pbo2_detail_resep`
--
ALTER TABLE `kasir_apotek_pbo2_detail_resep`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_detailresep_resep` (`resep_id`),
  ADD KEY `idx_detailresep_obat` (`obat_id`);

--
-- Indexes for table `kasir_apotek_pbo2_detail_transaksi`
--
ALTER TABLE `kasir_apotek_pbo2_detail_transaksi`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_detailtrans_transaksi` (`transaksi_id`),
  ADD KEY `idx_detailtrans_obat` (`obat_id`);

--
-- Indexes for table `kasir_apotek_pbo2_obat`
--
ALTER TABLE `kasir_apotek_pbo2_obat`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nama` (`nama`);

--
-- Indexes for table `kasir_apotek_pbo2_pasien`
--
ALTER TABLE `kasir_apotek_pbo2_pasien`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_pasien_nama` (`nama`);

--
-- Indexes for table `kasir_apotek_pbo2_resep`
--
ALTER TABLE `kasir_apotek_pbo2_resep`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_resep_pasien` (`pasien_id`),
  ADD KEY `idx_resep_dokter` (`dokter_id`);

--
-- Indexes for table `kasir_apotek_pbo2_tenaga_medis`
--
ALTER TABLE `kasir_apotek_pbo2_tenaga_medis`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `idx_tm_nama` (`nama`);

--
-- Indexes for table `kasir_apotek_pbo2_transaksi`
--
ALTER TABLE `kasir_apotek_pbo2_transaksi`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_transaksi_pasien` (`pasien_id`),
  ADD KEY `idx_transaksi_resep` (`resep_id`),
  ADD KEY `idx_transaksi_apoteker` (`id_apoteker`),
  ADD KEY `idx_transaksi_tanggal` (`tanggal_transaksi`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `kasir_apotek_pbo2_detail_resep`
--
ALTER TABLE `kasir_apotek_pbo2_detail_resep`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `kasir_apotek_pbo2_detail_transaksi`
--
ALTER TABLE `kasir_apotek_pbo2_detail_transaksi`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `kasir_apotek_pbo2_obat`
--
ALTER TABLE `kasir_apotek_pbo2_obat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `kasir_apotek_pbo2_pasien`
--
ALTER TABLE `kasir_apotek_pbo2_pasien`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `kasir_apotek_pbo2_resep`
--
ALTER TABLE `kasir_apotek_pbo2_resep`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `kasir_apotek_pbo2_tenaga_medis`
--
ALTER TABLE `kasir_apotek_pbo2_tenaga_medis`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `kasir_apotek_pbo2_transaksi`
--
ALTER TABLE `kasir_apotek_pbo2_transaksi`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `kasir_apotek_pbo2_detail_resep`
--
ALTER TABLE `kasir_apotek_pbo2_detail_resep`
  ADD CONSTRAINT `fk_detailresep_obat` FOREIGN KEY (`obat_id`) REFERENCES `kasir_apotek_pbo2_obat` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_detailresep_resep` FOREIGN KEY (`resep_id`) REFERENCES `kasir_apotek_pbo2_resep` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `kasir_apotek_pbo2_detail_transaksi`
--
ALTER TABLE `kasir_apotek_pbo2_detail_transaksi`
  ADD CONSTRAINT `fk_detailtrans_obat` FOREIGN KEY (`obat_id`) REFERENCES `kasir_apotek_pbo2_obat` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_detailtrans_transaksi` FOREIGN KEY (`transaksi_id`) REFERENCES `kasir_apotek_pbo2_transaksi` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `kasir_apotek_pbo2_resep`
--
ALTER TABLE `kasir_apotek_pbo2_resep`
  ADD CONSTRAINT `fk_resep_dokter` FOREIGN KEY (`dokter_id`) REFERENCES `kasir_apotek_pbo2_tenaga_medis` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_resep_pasien` FOREIGN KEY (`pasien_id`) REFERENCES `kasir_apotek_pbo2_pasien` (`id`) ON UPDATE CASCADE;

--
-- Constraints for table `kasir_apotek_pbo2_transaksi`
--
ALTER TABLE `kasir_apotek_pbo2_transaksi`
  ADD CONSTRAINT `fk_transaksi_apoteker` FOREIGN KEY (`id_apoteker`) REFERENCES `kasir_apotek_pbo2_tenaga_medis` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_transaksi_pasien` FOREIGN KEY (`pasien_id`) REFERENCES `kasir_apotek_pbo2_pasien` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_transaksi_resep` FOREIGN KEY (`resep_id`) REFERENCES `kasir_apotek_pbo2_resep` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
