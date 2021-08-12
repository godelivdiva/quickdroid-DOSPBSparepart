package com.quick.dospbsparepart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BagiCollyActivity extends AppCompatActivity {
    Context context;
    dbHelp helper;
    CursorAdapterBagi adapter;
    FloatingActionButton fab_add, fab_remove, fab_next;
    TextView tv_reqNumber;
    ListView hasildata;
    String mQuery, HEADER_ID, REQUEST_NUMBER, person;
    Connection mConn;
    int posisi;
    AlertDialog.Builder bApi;
    AlertDialog aApi;
    ManagerSessionUserOracle session;
    SweetAlertDialog sweetAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bagi_colly);
        getSupportActionBar().hide();
        new ModuleTool().allowNetworkOnMainThread();
        helper = new dbHelp(this);

        helper.deleteHeaderColly();

        session = new ManagerSessionUserOracle(this);
        Stetho.initializeWithDefaults(this);
        mConn = session.connectDb();

        fab_add = findViewById(R.id.fab_add);
        fab_remove = findViewById(R.id.fab_remove);
        fab_next = findViewById(R.id.fab_next);
        tv_reqNumber = findViewById(R.id.tv_reqNumber);
        hasildata = findViewById(R.id.lv_data);

        HashMap<String, String> userData = session.getUserData();
        person = userData.get(ManagerSessionUserOracle.KEY_PERSON);

        HEADER_ID = getIntent().getStringExtra("HEADER_ID");
        REQUEST_NUMBER = getIntent().getStringExtra("REQUEST_NUMBER");

        tv_reqNumber.setText(REQUEST_NUMBER);

        adapter = new CursorAdapterBagi(this, helper.selectHeaderColly());
        hasildata.setAdapter(adapter);

        insertHeader(HEADER_ID);
        refreshList();

        fab_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(helper.jumlahHead() > 0){
                    int jumlahheader = helper.jumlahHead();
                    helper.removeHead(jumlahheader);
//                    helper.removeItemColly("C"+REQUEST_NUMBER+"-"+jumlahheader);
                    refreshList();
                }
            }
        });

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(helper.jumlahHead()==0){
                    int jumlahheader = helper.jumlahHead();
                    int urutan = jumlahheader + 1;
                    helper.inputHead("C"+REQUEST_NUMBER+"-"+urutan);
                } else {
                    int jumlahheader = helper.jumlahHead();
                    String headLast = helper.selectHeadLast(jumlahheader);
                    Log.e("CEK NO COLLY TERAKHIR", headLast);
                    String[] parts = headLast.split("-");
                    String part1 = parts[0];
                    String part2 = parts[1];
                    int urut = Integer.parseInt(part2) +1 ;
                    helper.inputHead("C"+REQUEST_NUMBER+"-"+urut);
                }
                refreshList();
            }
        });

        fab_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNotOk(HEADER_ID)){
                    alertItem();
                    Log.e("CEK", "BELOM FULL");
                } else {
                    fab_next.setVisibility(View.GONE);
                    confirmColly();
                    Log.e("CEK", "SUDAH FULL ITEM QTY");
                }
            }
        });

        hasildata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("CEK", "KLIK ITEM");
                posisi = position;
//                LoadingDialog(posisi);
            }

        });

    }


    void LoadingDialog(String colly) {
        Log.e("CEK KLIK", colly);
        Cursor c = helper.selectHeaderCollyByColly(colly);
        if(c.moveToFirst()){
            Intent i = new Intent(BagiCollyActivity.this, ItemCollyActivity.class);
            i.putExtra("HEADER_ID", HEADER_ID);
            i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
            i.putExtra("NOMOR_COLLY", c.getString(c.getColumnIndexOrThrow("NOMOR_COLLY")));
            i.putExtra("AUTO", c.getString(c.getColumnIndexOrThrow("AUTO")));
            startActivity(i);
        }
    }

    void showData() {
        Cursor c = helper.selectHeaderColly();
        adapter = new CursorAdapterBagi(this, c);
        hasildata.setAdapter(adapter);
    }

    void refreshList() {
        Cursor c = helper.selectHeaderColly();
        adapter.changeCursor(c);
        adapter.notifyDataSetChanged();
        showData();
    }

    public void deleteKct(String header) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = mConn;
            stmt = conn.createStatement();
            System.out
                    .println("DELETE FROM KHS_COLLY_TAMPUNG \n" +
                            "WHERE HEADER_ID = '"+header+"'");

            stmt.executeUpdate("DELETE FROM KHS_COLLY_TAMPUNG \n" +
                    "WHERE HEADER_ID = '"+header+"'");

        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean isNotOk(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
//            String mQuery = "select khs_count_colly_ok("+header+")\n" +
//                    "from dual";
            String mQuery = "select khsdroidfnc_compare_item("+header+")\n" +
                    "from dual";
            theResultSet = statement.executeQuery(mQuery);
            Log.d("Cek Status :", mQuery);
            System.out.println(" " + theResultSet + " ");
            if (theResultSet.next()) {
                ver = Integer.parseInt(theResultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (ver == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNotIn(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT COUNT(*) FROM KHS_DETAIL_DOSPB_SP kdds \n" +
                    "WHERE HEADER_ID = "+header+"\n" +
                    "AND kdds.STATUS = 'V'\n" +
                    "AND LINE_ID NOT IN (SELECT LINE_ID \n" +
                    "FROM KHS_COLLY_TAMPUNG kct \n" +
                    "WHERE kdds.HEADER_ID = kct.HEADER_ID)";
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

    public boolean isNotAllow(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM KHS_COLLY_TAMPUNG kct \n" +
                    "WHERE COLLY_FLAG = 'NF'\n" +
                    "AND HEADER_ID = "+header+"";
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

    public void confirmColly(){
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Confirmation")
                .setContentText("Apakah anda yakin akan melakukan packing?")
                .setCancelText("Tidak")
                .setConfirmText("Ya")
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        prosesColly();
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        fab_next.setVisibility(View.VISIBLE);
                    }
                });
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }

    void prosesColly() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View viewInflated = inflater.inflate(R.layout.activity_loading, (ViewGroup) findViewById(android.R.id.content), false);

        bApi = new AlertDialog.Builder(this);
        bApi.setView(viewInflated).setCancelable(false);

        aApi = bApi.create();
        aApi.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                insertProcedurFinal();
                commit();
                //insertColly();
            }
        }, 500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                updateStatusC();
                updateWeightKcds(HEADER_ID);
                commit();
            }
        }, 3000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                aApi.dismiss();
                alertSuccess();
            }
        }, 5000);
    }

    public void insertColly() {
        Statement stmt = null;
        Connection conn = null;

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("INSERT INTO KHS_COLLY_DOSPB_SP \n" +
                                "select kct.HEADER_ID\n" +
                                "      ,kct.REQUEST_NUMBER\n" +
                                "      ,kct.LINE_ID\n" +
                                "      ,kct.ITEM_ID\n" +
                                "      ,kct.QTY_INPUT\n" +
                                "      ,kct.COLLY_NUMBER\n" +
                                "      ,SYSDATE creation_date\n" +
                                "      ,'"+person+"' created_by -- diambil dari yang login\n" +
                                "      ,'N' verif_flag\n" +
                                "from khs_colly_tampung kct\n" +
                                "where kct.COLLY_FLAG = 'F'\n" +
                                "  and kct.ITEM_FLAG = 'Y'\n" +
                                "  and kct.REQUEST_NUMBER = '"+REQUEST_NUMBER+"'");

                stmt.executeUpdate("INSERT INTO KHS_COLLY_DOSPB_SP \n" +
                                "select kct.HEADER_ID\n" +
                                "      ,kct.REQUEST_NUMBER\n" +
                                "      ,kct.LINE_ID\n" +
                                "      ,kct.ITEM_ID\n" +
                                "      ,kct.QTY_INPUT\n" +
                                "      ,kct.COLLY_NUMBER\n" +
                                "      ,SYSDATE creation_date\n" +
                                "      ,'"+person+"' created_by -- diambil dari yang login\n" +
                                "      ,'N' verif_flag\n" +
                                "from khs_colly_tampung kct\n" +
                                "where kct.COLLY_FLAG = 'F'\n" +
                                "  and kct.ITEM_FLAG = 'Y'\n" +
                                "  and kct.REQUEST_NUMBER = '"+REQUEST_NUMBER+"'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
    }

    public void updateStatusC() {
        Statement stmt = null;
        Connection conn = null;

        try {

            conn = mConn;
            stmt = conn.createStatement();

            System.out
                    .println("UPDATE KHS_DETAIL_DOSPB_SP \n" +
                    "SET STATUS = 'C'\n" +
                    "WHERE HEADER_ID = "+HEADER_ID+"\n" +
                    "AND REQUEST_NUMBER = '"+REQUEST_NUMBER+"'");

            stmt.executeUpdate("UPDATE KHS_DETAIL_DOSPB_SP \n" +
                    "SET STATUS = 'C'\n" +
                    "WHERE HEADER_ID = "+HEADER_ID+"\n" +
                    "AND REQUEST_NUMBER = '"+REQUEST_NUMBER+"'");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void alertItem(){
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Alert")
                .setContentText("Masih ada item yang belum di packing!")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
//                        fab_next.setEnabled(true);
//                        Intent i = new Intent(getApplicationContext(), DOCollyActivity.class);
//                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.show();
        sweetAlertDialog.setCanceledOnTouchOutside(false);
    }

    public void alertColly(){
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Alert")
                .setContentText("Masih ada qty item yang belum di packing!")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
//                        fab_next.setEnabled(true);
//                        Intent i = new Intent(getApplicationContext(), DOCollyActivity.class);
//                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.show();
        sweetAlertDialog.setCanceledOnTouchOutside(false);
    }

    public void alertSuccess(){
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Proses packing berhasil!")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
//                        deleteKct(HEADER_ID);
//                        commit();
//                        Log.e("CEK", "DELETE KCT SETELAH INSERT KCDS");
                        fab_next.setVisibility(View.VISIBLE);
                        fab_next.setEnabled(true);
                        Intent i = new Intent(getApplicationContext(), DOCollyActivity.class);
                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.show();
        sweetAlertDialog.setCanceledOnTouchOutside(false);
    }

    public void insertProcedurFinal() {
        Statement stmt = null;
        Connection conn = null;

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("BEGIN\n" +
                                "\tkhsdroidprc_input_kcds(" + HEADER_ID + ",'" + person + "'); \n" +
                                "END;");

                stmt.executeUpdate("BEGIN\n" +
                                "\tkhsdroidprc_input_kcds(" + HEADER_ID + ",'" + person + "'); \n" +
                                "END;");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
    }

    public void updateWeightKcds(String header) {
        Statement stmt = null;
        Connection conn = null;

        try {

            conn = mConn;
            stmt = conn.createStatement();

//            System.out
//                    .println(" UPDATE KHS_COLLY_DOSPB_SP kcds \n" +
//                            "  SET kcds.BERAT = kcds.QUANTITY * (SELECT DISTINCT msib.UNIT_WEIGHT \n" +
//                            "  \t\t\t\t\tFROM mtl_system_items_b msib\n" +
//                            "  \t\t\t\t\tWHERE kcds.ITEM_ID = msib.INVENTORY_ITEM_ID)\n" +
//                            "  WHERE kcds.HEADER_ID = "+header+"\n" +
//                            "  AND kcds.AUTO = 'Y'");
//
//            stmt.executeUpdate(" UPDATE KHS_COLLY_DOSPB_SP kcds \n" +
//                    "  SET kcds.BERAT = kcds.QUANTITY * (SELECT DISTINCT msib.UNIT_WEIGHT \n" +
//                    "  \t\t\t\t\tFROM mtl_system_items_b msib\n" +
//                    "  \t\t\t\t\tWHERE kcds.ITEM_ID = msib.INVENTORY_ITEM_ID)\n" +
//                    "  WHERE kcds.HEADER_ID = "+header+"\n" +
//                    "  AND kcds.AUTO = 'Y'");

            System.out
                    .println("UPDATE khs_colly_dospb_sp kcds\n" +
                            "   SET kcds.berat =\n" +
                            "            kcds.quantity\n" +
                            "          * (SELECT NVL (msib.unit_weight, 0)\n" +
                            "               FROM mtl_system_items_b msib\n" +
                            "              WHERE kcds.item_id = msib.inventory_item_id\n" +
                            "                AND msib.organization_id = 81)\n" +
                            " WHERE kcds.header_id = "+header+"\n" +
                            "AND kcds.AUTO = 'Y'");

            stmt.executeUpdate("UPDATE khs_colly_dospb_sp kcds\n" +
                            "   SET kcds.berat =\n" +
                            "            kcds.quantity\n" +
                            "          * (SELECT NVL (msib.unit_weight, 0)\n" +
                            "               FROM mtl_system_items_b msib\n" +
                            "              WHERE kcds.item_id = msib.inventory_item_id\n" +
                            "                AND msib.organization_id = 81)\n" +
                            " WHERE kcds.header_id = "+header+"\n" +
                            "AND kcds.AUTO = 'Y'");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    void insertHeader(String header) {
        Log.d("Conn", "." + mConn);
        if (mConn == null) {
            Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Statement statement = mConn.createStatement();
//            mQuery = "SELECT DISTINCT COLLY_NUMBER, AUTO \n" +
//                    "FROM KHS_COLLY_TAMPUNG kct \n" +
//                    "WHERE HEADER_ID = "+header+"\n" +
//                    "ORDER BY 1 ASC";

            mQuery = "SELECT DISTINCT kct.colly_number, kct.auto , TO_NUMBER (TRIM (SUBSTR (kct.colly_number,\n" +
                    "                                            INSTR (kct.colly_number, '-', 1,\n" +
                    "                                                   1)\n" +
                    "                                          + 1,\n" +
                    "                                            LENGTH (kct.colly_number)\n" +
                    "                                          - INSTR (kct.colly_number, '-', 1,\n" +
                    "                                                   1)\n" +
                    "                                         )\n" +
                    "                                 )\n" +
                    "                           ) urut\n" +
                    "FROM KHS_COLLY_TAMPUNG kct \n" +
                    "WHERE HEADER_ID = "+header+"\n" +
                    "ORDER BY 3 ASC";

            Log.d("QUERY", mQuery);
            ResultSet result = statement.executeQuery(mQuery);
            while (result.next()) {
                ContentValues values = new ContentValues();
                values.put("NOMOR_COLLY", result.getString(1));
                values.put("AUTO", result.getString(2));
                Log.d("VALUES", values.toString());
                helper.insertHead(values);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePrc(String colly) {
        Statement stmt = null;
        Connection conn = null;

        try {

            conn = mConn;
            stmt = conn.createStatement();

            System.out
                    .println("BEGIN\n" +
                            "\tkhsqdroidprc_delete_kct('" + colly + "'); \n" +
                            "END;");

            stmt.executeUpdate("BEGIN\n" +
                    "\tkhsqdroidprc_delete_kct('" + colly + "'); \n" +
                    "END;");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public void loadDelete(){
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("\nLoading . . .");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sweetAlertDialog.dismissWithAnimation();
            }
        }, 3000);
    }

    @Override
    public void onBackPressed() {
        //ketika klik back
//        deleteKct(HEADER_ID);
//        commit();
//        Log.e("CEK", "DELETE COLLY TAMPUNGAN");
        Intent intent = new Intent(this, DOCollyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
