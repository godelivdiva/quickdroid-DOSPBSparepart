package com.quick.dospbsparepart;

/**
 * Created by user on 10/9/17.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KoneksiPostGre {
    private Connection connect;
    private String driverName = "org.postgresql.Driver"; // Driver Untuk Koneksi Ke PostgreSQL
    private String jdbc = "jdbc:postgresql://";
    private String host = "dev.quick.com:"; // Host ini Bisa Menggunakan IP Anda, Contoh : 192.168.100.100
    private String port = "5432/"; // Port Default PostgreSQL
    private String database = "erp"; // Ini Database yang akan digunakan
    private String url = jdbc + host + port + database;
    private String username = "postgres"; //
    private String password = "password";

    public Connection getKoneksi() {

        System.out.println("URL = " + url);

        if (connect == null) {
            try {
                Class.forName(driverName);
                System.out.println("Class Driver Ditemukan");
                try {
                    connect = DriverManager.getConnection(url, username, password);
                    System.out.println("Koneksi Database Sukses");
                } catch (SQLException se) {
                    System.out.println("Koneksi Database Gagal : " + se);
                }
            } catch (ClassNotFoundException cnfe) {
                System.out.println("Class Driver Tidak Ditemukan, Terjadi Kesalahan Pada : " + cnfe);
            }
        }
        return connect;
    }
}
