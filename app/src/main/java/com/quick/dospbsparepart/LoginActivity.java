package com.quick.dospbsparepart;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.quick.dospbsparepart.Model.Login_Model;
import com.quick.dospbsparepart.Model.User_Model;
import com.quick.dospbsparepart.Rest.API_Client;
import com.quick.dospbsparepart.Rest.API_Link;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    Spinner sp_subinv;
    TextInputEditText et_username, et_password;
    RadioButton rb_dev, rb_prod;
    Button b_login;
    Koneksi konek;
    ManagerSessionUserOracle session;
    String cName, cPort, cSid, cUsername, cPassword, res = "0";
    ProgressDialog progressDialog;
    Connection connPostgre;
    private String array_spinner[];
    List<String> list_subinv;
    dbHelp helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        progressDialog = new ProgressDialog(this);
        new ModuleTool().allowNetworkOnMainThread();
        konek = new Koneksi();
        connPostgre = new KoneksiPostGre().getKoneksi();
        getSupportActionBar().hide();

        session = new ManagerSessionUserOracle(this);
        if (session.isUserLogin()) {
            Intent i = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(i);
            finish();
        }

        helper = new dbHelp(this);

        sp_subinv = (Spinner) findViewById(R.id.sp_subinv);
        et_username = (TextInputEditText) findViewById(R.id.et_username);
        et_password = (TextInputEditText) findViewById(R.id.et_password);
        rb_dev = (RadioButton) findViewById(R.id.rb_dev);
        rb_prod = (RadioButton) findViewById(R.id.rb_prod);
        b_login = (Button) findViewById(R.id.btn_login);

        rb_prod.setChecked(true);

        if (rb_dev.isChecked()) {
            cName = "192.168.7.3";
            cPort = "1522";
            cSid = "DEV";
            cUsername = "APPS";
            cPassword = "APPS";
            String usernamedev = "AA TECH TSR 01";
            String passwddev = "PARIS2020";
        } else {
            cName = "192.168.7.1";
            cPort = "1521";
            cSid = "PROD";
            cUsername = "APPS";
            cPassword = "APPS";
            String usernameprod = "AA TECH TSR 01";
            String passwdprod = "TOKYO2020";
        }

        list_subinv = new ArrayList<String>();

        list_subinv.clear();
        getSubInv();
        ArrayAdapter<String> sadapter = new ArrayAdapter<String>(
                LoginActivity.this, android.R.layout.simple_dropdown_item_1line, list_subinv);
        sadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sp_subinv.setAdapter(sadapter);

//        spinnersubinv();
        sp_subinv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ((TextView) view).setTextColor(Color.WHITE); //Change selected text color
                ((TextView) view).setGravity(Gravity.CENTER);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        b_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username, password;
                username = et_username.getText().toString().toUpperCase();
                password = et_password.getText().toString();
                if (et_username.getText().length() > 0) {
                    if (et_password.getText().length() > 0) {
                        helper.deleteUser();
                        loguser(username, password); //jalanin api select username di database
                    } else {
                        et_password.setError("Password harus diisi");
                    }
                } else {
                    et_username.setError("Username harus diisi");
                }
            }
        });
    }

    void loguser(final String user, final String passwd) {
        Log.d("username", user);
        API_Link a = API_Client.getClient().create(API_Link.class);
        Call<User_Model> call = a.loguser(user);

        call.enqueue(new Callback<User_Model>() {
            @Override
            public void onResponse(Call<User_Model> call, Response<User_Model> response) {
                if (response.body().getError() == false) {
                    Log.d("error1", response.body().getError().toString());
                    login(user, passwd); //function jika username terdaftar, menjalankan api untuk mengecek password
                } else {
                    Log.d("error2", response.body().getError().toString());
                    erroruser(); //function alert user tidak terdaftar
                }
            }

            @Override
            public void onFailure(Call<User_Model> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(LoginActivity.this, "login gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void erroruser() {
        new android.app.AlertDialog.Builder(LoginActivity.this)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Alert!")
                .setMessage(
                        "Username tidak terdaftar! Silahkan daftar akun erp terlebih dahulu")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        et_username.setText("");
                        et_password.setText("");
                    }

                }).show();
    }

    void login(final String user, String password) {
        Log.d("1", user);
        Log.d("2", password);
        API_Link a = API_Client.getClient().create(API_Link.class);
        Call<Login_Model> call = a.login(user, password);

        call.enqueue(new Callback<Login_Model>() {
            @Override
            public void onResponse(Call<Login_Model> call, Response<Login_Model> response) {
                if (!response.body().isError()) {
                    Log.d("coba", response.body().getUser());
                    checkkoneksi(user);
                    helper.inputUser(response.body().getUser_id(), response.body().getUser(), response.body().getEmployee().trim(), response.body().getKodesie(), response.body().getLokasi());
                    Toast.makeText(LoginActivity.this, "Selamat datang " + response.body().getUser(), Toast.LENGTH_SHORT).show();
                } else {
                    errorpasswd(); //function alert password salah
                }
            }
            @Override
            public void onFailure(Call<Login_Model> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(LoginActivity.this, "login gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void errorpasswd() {
        new android.app.AlertDialog.Builder(LoginActivity.this)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Alert!")
                .setMessage(
                        "password yang anda masukan salah")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        et_password.setText("");
                    }

                }).show();
    }

    void checkkoneksi(String username) {
        if (rb_dev.isChecked()) {
            cName = "192.168.7.3";
            cPort = "1522";
            cSid = "DEV";
            cUsername = "APPS";
            cPassword = "APPS";
            String usernamedev = "AA TECH TSR 01";
            String passwddev = "PARIS2020";
            session.createUserSession(usernamedev, passwddev, "5177", cName, cPort, cSid, cUsername, cPassword, username, sp_subinv.getSelectedItem().toString());
            recreate();
        } else {
            cName = "192.168.7.1";
            cPort = "1521";
            cSid = "PROD";
            cUsername = "APPS";
            cPassword = "APPS";
            String usernameprod = "AA TECH TSR 01";
            String passwdprod = "TOKYO2020";
            session.createUserSession(usernameprod, passwdprod, "5177", cName, cPort, cSid, cUsername, cPassword, username, sp_subinv.getSelectedItem().toString());
            recreate();
        }
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

    public void spinnersubinv() {
        array_spinner = new String[4];
        array_spinner[0] = "FG-DM";
        array_spinner[1] = "FG-TKS";
        array_spinner[2] = "MLATI-DM";
        array_spinner[3] = "SP-YSP";

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, array_spinner);
        sp_subinv.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    void getSubInv() {
//        list_subinv.add("-");
        Connection dbConnection = null;
        Statement stateuy = null;
        ResultSet theResultSet;
        String Query = "";
        Connection conn = new Koneksi().getConnection(cName, cPort, cSid, cUsername, cPassword);
        try {
            stateuy = conn.createStatement();
            Query = "SELECT   *\n" +
                    "    FROM khs_subinventory_do ksd\n" +
                    "   WHERE ksd.tipe = 'SPAREPART'\n" +
                    "ORDER BY ksd.subinventory";
            theResultSet = stateuy.executeQuery(Query);
            while (theResultSet.next()) { //looping
                list_subinv.add(theResultSet.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
