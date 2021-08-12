package com.quick.dospbsparepart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class MenuActivity extends AppCompatActivity {

    ManagerSessionUserOracle session;
    ImageView iv_menu;
    TextView tv_Sub;
    String cName, cPort, cSid, cUsername, cPassword, res = "0", person, subinv;
    private String array_spinner[];
    dbHelp helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().hide(); //menhilangkan actionbar

        session = new ManagerSessionUserOracle(this);
        helper = new dbHelp(this);

        HashMap<String, String> userData = session.getUserData();
        person = userData.get(ManagerSessionUserOracle.KEY_PERSON);
        subinv = userData.get(ManagerSessionUserOracle.KEY_SUBINV);

        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        tv_Sub  = (TextView) findViewById(R.id.tv_Subinv);

        tv_Sub.setText(subinv);
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSubinv();
            }
        });
    }

    void dialogSubinv() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View viewInflate = inflater.inflate(R.layout.activity_change_subinv, (ViewGroup) findViewById(android.R.id.content), false);
        final Spinner sp_subinv = viewInflate.findViewById(R.id.sp_subinv);

        array_spinner = new String[4];
        array_spinner[0] = "FG-DM";
        array_spinner[1] = "FG-TKS";
        array_spinner[2] = "MLATI-DM";
        array_spinner[3] = "SP-YSP";

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, array_spinner);
        sp_subinv.setAdapter(adapter);

        if (subinv.equals("FG-DM")){
            sp_subinv.setSelection(0);
        } else if (subinv.equals("FG-TKS")){
            sp_subinv.setSelection(1);
        }  else if (subinv.equals("MLATI-DM")){
            sp_subinv.setSelection(2);
        } else {
            sp_subinv.setSelection(3);
        }

        sp_subinv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setGravity(Gravity.CENTER);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflate)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (session.getSID().equals("DEV")) {
                            cName = "192.168.7.3";
                            cPort = "1522";
                            cSid = "DEV";
                            cUsername = "APPS";
                            cPassword = "APPS";
                            String usernamedev = "AA TECH TSR 01";
                            String passwddev = "PARIS2020";
                            session.createUserSession(usernamedev, passwddev, getUserId(usernamedev), cName, cPort, cSid, cUsername, cPassword, person, sp_subinv.getSelectedItem().toString());
                            recreate();
                        } else {
                            cName = "192.168.7.1";
                            cPort = "1521";
                            cSid = "PROD";
                            cUsername = "APPS";
                            cPassword = "APPS";
                            String usernameprod = "AA TECH TSR 01";
                            String passwdprod = "TOKYO2020";
                            session.createUserSession(usernameprod, passwdprod, getUserId(usernameprod), cName, cPort, cSid, cUsername, cPassword, person, sp_subinv.getSelectedItem().toString());
                            recreate();
                        }
                    }

                }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    String getUserId(String username) {
        Connection conn = new Koneksi().getConnection(cName, cPort, cSid, cUsername, cPassword);
        Statement statement;
        ResultSet result;
        try {
            statement = conn.createStatement();
            String Query = "SELECT user_id FROM fnd_user \n" +
                    "WHERE user_name = '" + username + "'";
            result = statement.executeQuery(Query);
            if (result.next()) return result.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_awal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logoutApp();
                break;
            case R.id.menu_help:
                dialogHelp();
                break;
        }
        return true;
    }

    void dialogHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selamat Datang Di Quickdroid")
                .setMessage("Anda bisa pilih salah satu menu.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void logoutApp() {
        session.logoutUser();
        finish();
    }

    //button menu allocate
    public void allocate(View view) {
        Intent i = new Intent(MenuActivity.this, DOAllocateActivity.class); //ketika di klik maka akan pindah ke activity DOAllocateActivity
        startActivity(i);
    }

    //button menu transact
    public void bagicolly(View view) {
        Intent i = new Intent(MenuActivity.this, DOCollyActivity.class); //ketika di klik maka akan pindah ke activity Transact
        startActivity(i);
    }

    //button menu transact
//    public void transact(View view) {
//        Intent i = new Intent(MenuActivity.this, DOTransactActivity.class); //ketika di klik maka akan pindah ke activity Transact
//        startActivity(i);
//    }

    public void profile(View view) {
        //dialog
        Toast.makeText(MenuActivity.this, "kaming sun", Toast.LENGTH_SHORT).show();
        Cursor c = helper.selectUser();
        if (c.moveToFirst()) {
            Log.e("USER : ", c.getString(1) + " | " + c.getString(2) + " | " + c.getString(3).trim() + " | " + c.getString(4) + " | " + c.getString(5));
        }
    }

    public void logout(View view) {
        new AlertDialog.Builder(MenuActivity.this)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Perhatian!")
                .setMessage("Apakah anda yakin ingin logout? ")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        session.logoutUser();
                        finish();
                    }
                })
                .setNegativeButton("Tidak", null)
                .create().show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            new android.app.AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning)
                    .setTitle("Keluar Aplikasi")
                    .setMessage(
                            "Anda yakin ingin keluar dari aplikasi?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveTaskToBack(true);
                        }

                    }).setNegativeButton("Tidak", null).show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Keluar Aplikasi")
                .setMessage(
                        "Anda yakin ingin keluar dari aplikasi? ")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                    }

                }).setNegativeButton("Tidak", null).show();
    }
}
