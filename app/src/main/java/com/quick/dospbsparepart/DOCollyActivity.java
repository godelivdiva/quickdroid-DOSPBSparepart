package com.quick.dospbsparepart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DOCollyActivity extends AppCompatActivity {
    Context context;
    dbHelp helper;
    CursorAdapterDO adapter;
    ManagerSessionUserOracle session;
    ImageView refresh;
    ListView hasildata;
    String mQuery, person, subinv;
    Connection mConn;
    int posisi, i;

    SweetAlertDialog sweetAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docolly);

        new ModuleTool().allowNetworkOnMainThread();
        session = new ManagerSessionUserOracle(this);
        if(session.getSID().equals("DEV")){
            setTitle("BAGI PACKING [ DEV ]");
        } else {
            setTitle("BAGI PACKING [ PROD ]");
        }

        context = this;

        Stetho.initializeWithDefaults(this);

        refresh = (ImageView) findViewById(R.id.refresh);
        hasildata = (ListView) findViewById(R.id.hasil_list);

        mConn = session.connectDb();

        HashMap<String, String> userData = session.getUserData();
        person = userData.get(ManagerSessionUserOracle.KEY_PERSON);
        subinv = userData.get(ManagerSessionUserOracle.KEY_SUBINV);

        helper = new dbHelp(this);
        adapter = new com.quick.dospbsparepart.CursorAdapterDO(this, helper.selectDO());
        goLoadingData();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });

        hasildata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                posisi = position;
                LoadingDialog();
            }

        });
    }

    void LoadingDialog() {
        Cursor c = helper.selectDO();
        c.moveToPosition(posisi);
        //        deleteKct(c.getString(c.getColumnIndexOrThrow("REQUEST_NUMBER")));
        //        commit();
        //        insertKct(c.getString(c.getColumnIndexOrThrow("REQUEST_NUMBER")));
        //        commit();
        String HEADER_ID = c.getString(c.getColumnIndexOrThrow("HEADER_ID"));
        if(isNotComplete(HEADER_ID)){
            alertNotComplete(); //grf
        } else {
            Intent i = new Intent(DOCollyActivity.this, BagiCollyActivity.class);
            i.putExtra("HEADER_ID", c.getString(c.getColumnIndexOrThrow("HEADER_ID")));
            i.putExtra("REQUEST_NUMBER", c.getString(c.getColumnIndexOrThrow("REQUEST_NUMBER")));
            startActivity(i);
        }
    }

    void goLoadingData() {
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("\nLoading Data. . .");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                helper.deleteDO();
                hasildata.setAdapter(adapter);
                if (IsAny()) {
                    insertDb();
                    showMessage();
                } else {
                    sweetAlertDialog.dismissWithAnimation();
                    alertDO();
                }
            }
        }, 1000);
    }

    public boolean IsAny() {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM khs_qdroid_colly_sp\n" +
                    "WHERE ASSIGNEE_ID = '"+person+"'";
            theResultSet = statement.executeQuery(mQuery);
            Log.d("Cek Status :", mQuery);
            System.out.println(" " + theResultSet + " ");
            if (theResultSet.next()) {
                ver = Integer.parseInt(theResultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (ver > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNotComplete(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
//            String mQuery = "SELECT count(*) FROM khs_detail_dospb_sp kddsp\n" +
//                    "WHERE kddsp.HEADER_ID = "+header+"\n" +
//                    "AND STATUS = 'A'";
            String mQuery = "SELECT count(*) FROM khs_detail_dospb_sp kddsp\n" +
                    "    WHERE kddsp.HEADER_ID = "+header+"\n" +
                    "    AND STATUS = 'A'\n" +
                    "    AND ALLOCATED_QUANTITY <> '0'";
            theResultSet = statement.executeQuery(mQuery);
            Log.d("Cek not complete :", mQuery);
            System.out.println(" " + theResultSet + " ");
            if (theResultSet.next()) {
                ver = Integer.parseInt(theResultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (ver > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void alertNotComplete() {
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Alert!")
                .setContentText("Masih ada item yang belum diverifikasi!\n" +
                        "Verifikasi semua item sebelum melakukan packing")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
//                        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                        finish();
                        sDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.show();
        sweetAlertDialog.setCanceledOnTouchOutside(false);
    }

    void insertDb() {
        Log.d("Conn", "." + mConn);
        if (mConn == null) {
            Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Statement statement = mConn.createStatement();
            mQuery = "SELECT * FROM khs_qdroid_colly_sp \n" +
                    "WHERE ASSIGNEE_ID = '"+person+"'";
            Log.d("QUERY", mQuery);
            ResultSet result = statement.executeQuery(mQuery);
            while (result.next()) {
                ContentValues values = new ContentValues();
                values.put("HEADER_ID", result.getString(1));
                values.put("REQUEST_NUMBER", result.getString(2));
                values.put("ASSIGNEE_ID", result.getString(3));
                values.put("NOT_VERIFIKASI", "");
                Log.d("VALUES", values.toString());
                helper.insertDO(values);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void showMessage() {
        Cursor c = helper.selectDO();
        adapter = new com.quick.dospbsparepart.CursorAdapterDO(this, c);
        hasildata.setAdapter(adapter);
        sweetAlertDialog.dismissWithAnimation();
    }

    public void alertDO() {
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Alert!")
                .setContentText("DO/SPB tidak tersedia!")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        sDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.show();
        sweetAlertDialog.setCanceledOnTouchOutside(false);
    }

    public void insertKct(String request) {
        Statement stmt = null;
        Connection conn = null;

            Integer ver = 0;
            ResultSet theResultSet;
            try {
                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("INSERT INTO KHS_COLLY_TAMPUNG\n" +
                                "select \n" +
                                "kdds.HEADER_ID\n" +
                                ",kdds.LINE_ID\n" +
                                ",kdds.REQUEST_NUMBER\n" +
                                ",kdds.ORGANIZATION_ID\n" +
                                ",msib.SEGMENT1 item\n" +
                                ",kdds.INVENTORY_ITEM_ID item_id\n" +
                                ",msib.DESCRIPTION\n" +
                                ",kdds.LINE_NUMBER\n" +
                                ",kdds.REQUIRED_QUANTITY\n" +
                                ",kdds.ALLOCATED_QUANTITY\n" +
                                ",kdds.STATUS\n" +
                                ",null colly_number\n" +
                                ",0 qty_packing\n" +
                                ",0 qty_input\n" +
                                ",null colly_flag\n" +
                                ",'N' item_flag\n" +
                                "from khs_detail_dospb_sp kdds\n" +
                                "    ,mtl_system_items_b msib \n" +
                                "where msib.INVENTORY_ITEM_ID = kdds.INVENTORY_ITEM_ID\n" +
                                "  and msib.ORGANIZATION_ID = kdds.ORGANIZATION_ID\n" +
                                "  and kdds.STATUS = 'V' -- verified\n" +
                                "  and kdds.REQUEST_NUMBER = '"+request+"'");

                stmt.executeUpdate("INSERT INTO KHS_COLLY_TAMPUNG\n" +
                                "select \n" +
                                "kdds.HEADER_ID\n" +
                                ",kdds.LINE_ID\n" +
                                ",kdds.REQUEST_NUMBER\n" +
                                ",kdds.ORGANIZATION_ID\n" +
                                ",msib.SEGMENT1 item\n" +
                                ",kdds.INVENTORY_ITEM_ID item_id\n" +
                                ",msib.DESCRIPTION\n" +
                                ",kdds.LINE_NUMBER\n" +
                                ",kdds.REQUIRED_QUANTITY\n" +
                                ",kdds.ALLOCATED_QUANTITY\n" +
                                ",kdds.STATUS\n" +
                                ",null colly_number\n" +
                                ",0 qty_packing\n" +
                                ",0 qty_input\n" +
                                ",null colly_flag\n" +
                                ",'N' item_flag\n" +
                                "from khs_detail_dospb_sp kdds\n" +
                                "    ,mtl_system_items_b msib \n" +
                                "where msib.INVENTORY_ITEM_ID = kdds.INVENTORY_ITEM_ID\n" +
                                "  and msib.ORGANIZATION_ID = kdds.ORGANIZATION_ID\n" +
                                "  and kdds.STATUS = 'V' -- verified\n" +
                                "  and kdds.REQUEST_NUMBER = '"+request+"'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
    }

    void commit() {
        try {
            Statement statement = mConn.createStatement();
            mQuery = "COMMIT";
            statement.executeUpdate(mQuery);
            Log.d("proses", "Commit " + mQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteKct(String request) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = mConn;
            stmt = conn.createStatement();
            System.out
                    .println("    DELETE FROM KHS_COLLY_TAMPUNG \n" +
                            "    WHERE REQUEST_NUMBER = '"+request+"'");

            stmt.executeUpdate("    DELETE FROM KHS_COLLY_TAMPUNG \n" +
                    "    WHERE REQUEST_NUMBER = '"+request+"'");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //ketika klik back
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
