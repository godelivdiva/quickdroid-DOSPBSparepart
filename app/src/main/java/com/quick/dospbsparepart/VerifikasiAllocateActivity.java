package com.quick.dospbsparepart;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.stetho.Stetho;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.client.android.CaptureActivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class VerifikasiAllocateActivity extends AppCompatActivity {
    Context context;
    dbHelp helper;
    ManagerSessionUserOracle session;
    ImageView iv_scann;
    String mQuery, person, HEADER_ID, REQUEST_NUMBER, INVENTORY_ITEM_ID, SEGMENT1, DESCRIPTION, REQUIRED_QUANTITY, ALLOCATED_QUANTITY, QUANTITY_DETAILED, STD_PACK, FLAG_QTY;
    Connection mConn;
    int posisi, i;
    AlertDialog.Builder bApi;
    AlertDialog aApi;
    List<String> listCollyAuto;

    SweetAlertDialog loadingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_allocate);
        getSupportActionBar().hide();
        context = this;
        session = new ManagerSessionUserOracle(getApplicationContext());
        new ModuleTool().allowNetworkOnMainThread();
        session = new ManagerSessionUserOracle(this);
        mConn = session.connectDb();
        HashMap<String, String> userData = session.getUserData();
        person = userData.get(ManagerSessionUserOracle.KEY_USEID);

        Stetho.initializeWithDefaults(this);

        listCollyAuto = new ArrayList<String>();

        iv_scann = (ImageView) findViewById(R.id.iv_scan);

        HEADER_ID = getIntent().getStringExtra("HEADER_ID");
        REQUEST_NUMBER = getIntent().getStringExtra("REQUEST_NUMBER");
        INVENTORY_ITEM_ID = getIntent().getStringExtra("INVENTORY_ITEM_ID");
        SEGMENT1 = getIntent().getStringExtra("SEGMENT1");
        DESCRIPTION = getIntent().getStringExtra("DESCRIPTION");
        REQUIRED_QUANTITY = getIntent().getStringExtra("REQUIRED_QUANTITY");
        ALLOCATED_QUANTITY = getIntent().getStringExtra("ALLOCATED_QUANTITY");
        QUANTITY_DETAILED = getIntent().getStringExtra("QUANTITY_DETAILED");
        STD_PACK = getIntent().getStringExtra("STD_PACK");
        posisi = getIntent().getIntExtra("POSITION", 0);
        FLAG_QTY = getIntent().getStringExtra("FLAG_QTY");

        helper = new dbHelp(this);
        runtimePermission();

    }

    void runtimePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 26);
        } else {
            Intent i = new Intent(this, CaptureActivity.class);
            i.putExtra("TITLE_SCAN", "Scan");
            i.putExtra("SAVE_HISTORY", false);
            i.setAction("com.google.zxing.client.android.SCAN");
            startActivityForResult(i, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 26 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Izinkan aplikasi mengakses kamera untuk melakukan SCANN", Toast.LENGTH_SHORT).show();
        } else {
            runtimePermission();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            final String scanContent = intent.getStringExtra("SCAN_RESULT");
            Log.e("Scan", scanContent);
            Log.e("SEGMENT1", SEGMENT1);
            if (getFinalCode(scanContent).equals(SEGMENT1)) {
//                dialog inputin qty
                Log.d("CEK ITEM", "COCOK");
                LoadingDialog();
            } else {
//                alert(); //muncul popup scan != no fpba
                alertNotMatch();
                Log.d("CEK ITEM", "TIDAK COCOK");
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, ItemAllocateActivity.class);  //kalo diback balik ke activity scan bukan recreate()
            i.putExtra("HEADER_ID", HEADER_ID);
            i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
            startActivity(i);
        }
    }

    void LoadingDialog() {
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
                dialogInputQty();
            }
        }, 2000);
    }

    void dialogInputQty() {
        aApi.dismiss();
        LayoutInflater inflater = LayoutInflater.from(this);
        View viewInflate = inflater.inflate(R.layout.activity_dialog_allocate, (ViewGroup) findViewById(android.R.id.content), false);
        final TextView tv_item = (TextView) viewInflate.findViewById(R.id.tv_Item);
        final TextView tv_desc = (TextView) viewInflate.findViewById(R.id.tv_Desc);
        final TextView tv_request = (TextView) viewInflate.findViewById(R.id.tv_Request);
        final TextView tv_allocate = (TextView) viewInflate.findViewById(R.id.tv_Allocate);
//        final TextView tv_verifikasi = (TextView) viewInflate.findViewById(R.id.tv_Verifikasi);
        final EditText et_verif = (EditText) viewInflate.findViewById(R.id.et_Verifikasi);
        final ImageButton btn_plus = (ImageButton) viewInflate.findViewById(R.id.btn_plus);
        final ImageButton btn_minus = (ImageButton) viewInflate.findViewById(R.id.btn_minus);
        final LinearLayout ll_qty = (LinearLayout) viewInflate.findViewById(R.id.ll_qty);
//        final LinearLayout ll_qtyinput = (LinearLayout) viewInflate.findViewById(R.id.ll_qtyinput);
//        final LinearLayout ll_alasan = (LinearLayout) viewInflate.findViewById(R.id.ll_alasan);
//        final TextInputEditText et_verifikasi = (TextInputEditText) viewInflate.findViewById(R.id.et_verifikasi);
//        final TextInputEditText et_alasan = (TextInputEditText) viewInflate.findViewById(R.id.et_alasan);

//        if(FLAG_QTY.equals("N")){
//            ll_qty.setVisibility(View.GONE);
//            ll_qtyinput.setVisibility(View.GONE);
//            ll_alasan.setVisibility(View.GONE);
//        }else {
//            ll_qty.setVisibility(View.VISIBLE);
//            ll_qtyinput.setVisibility(View.VISIBLE);
//            ll_alasan.setVisibility(View.VISIBLE);
//        }

        tv_item.setText(SEGMENT1);
        tv_desc.setText(DESCRIPTION);
        tv_request.setText(REQUIRED_QUANTITY);
        tv_allocate.setText(ALLOCATED_QUANTITY);
        et_verif.setText(QUANTITY_DETAILED);

        et_verif.setSelection(et_verif.getText().toString().length());

        btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int count = Integer.parseInt(et_verif.getText().toString());
                int count = 0;
                if(et_verif.length()==0){
                     count = 0;
                } else {
                    count = Integer.parseInt(et_verif.getText().toString());
                }
                if (count > 0) {
                    count--;
                    et_verif.setText(String.valueOf(count));
                    helper.update_qty_detail(et_verif.getText().toString(), posisi + 1);
                }
            }
        });

        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int count = Integer.parseInt(et_verif.getText().toString());
                int count = 0;
                if(et_verif.length()==0){
                    count = 0;
                } else {
                    count = Integer.parseInt(et_verif.getText().toString());
                }
                int request = Integer.parseInt(REQUIRED_QUANTITY);
                if (count >= request) {
                    Toast.makeText(v.getContext(), "qty allocate tidak boleh lebih dari request", Toast.LENGTH_SHORT).show();
                } else {
                    count++;
                    et_verif.setText(String.valueOf(count));
                    helper.update_qty_detail(et_verif.getText().toString(), posisi + 1);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflate)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            if(et_verif.length()==0){
                                finish();
                                Toast.makeText(getApplicationContext(),
                                        "Inputkan quantity!",
                                        Toast.LENGTH_LONG).show();
//                            } else if (et_verif.getText().toString().equals("0")) { //qty boleh 0
//                                finish();
//                                Toast.makeText(getApplicationContext(),
//                                        "Quantity tidak boleh 0",
//                                        Toast.LENGTH_LONG).show();
                            } else if (Integer.parseInt(et_verif.getText().toString()) > Integer.parseInt(tv_allocate.getText().toString())) {
                                finish();
                                Toast.makeText(getApplicationContext(),
                                        "Quantity tidak boleh melebihi request",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                if(et_verif.getText().toString().equals(ALLOCATED_QUANTITY)){
                                    goLoadUpdate(et_verif.getText().toString(), "");
                                } else {
                                    if(STD_PACK== null) {
                                        Log.e("CEK", "TIDAK ADA STD PACK");
                                        Log.d("cek", "munculin dialog alasan");
                                        dialogInputAlasan(et_verif.getText().toString());
                                    } else {
                                        double bagi_new = Math.floor(Integer.parseInt(et_verif.getText().toString()) / Integer.parseInt(STD_PACK));
                                        helper.update_jumlah_bagi(String.valueOf(bagi_new), posisi + 1);
                                        Log.d("cek bagi_new", String.valueOf(bagi_new));

                                        Log.d("cek", "munculin dialog alasan");
                                        dialogInputAlasan(et_verif.getText().toString());
                                    }

                                }
                            }
                    }

                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), ItemAllocateActivity.class);
                intent.putExtra("HEADER_ID", HEADER_ID);
                intent.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                startActivity(intent);
                dialogInterface.cancel();
            }
        })
                .setCancelable(false)
                .create().show();
    }

    void dialogInputAlasan(final String qty) {
        aApi.dismiss();
        LayoutInflater inflater = LayoutInflater.from(this);
        View viewInflate = inflater.inflate(R.layout.activity_dialog_alasan, (ViewGroup) findViewById(android.R.id.content), false);
        final LinearLayout ll_alasan = (LinearLayout) viewInflate.findViewById(R.id.ll_alasan);
        final TextInputEditText et_alasan = (TextInputEditText) viewInflate.findViewById(R.id.et_alasan);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflate)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            if(et_alasan.length()==0){
                                finish();
                                Toast.makeText(getApplicationContext(),
                                        "Inputkan alasan!",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                goLoadUpdate(qty, et_alasan.getText().toString());
                            }
                    }

                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), ItemAllocateActivity.class);
                intent.putExtra("HEADER_ID", HEADER_ID);
                intent.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                startActivity(intent);
                dialogInterface.cancel();
            }
        })
                .setCancelable(false)
                .create().show();
    }


    void goLoadUpdate(final String qty, final String alasan) {
        Log.e("CEK", "UPDATE QTY");
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingData.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loadingData.setTitleText("\nLoading Data. . .");
        loadingData.setCancelable(false);
        loadingData.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusKdd();
                commit();
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(alasan.equals("")){
                    updateQtyKddspInput(qty);
                    commit();
                } else {
                    updateQtyKddsp(alasan, qty);
                    commit();
                }

            }
        }, 3000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(STD_PACK == null){
                    Log.e("CEK", "TIDAK ADA STANDAR PACKING");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("CEK", "FINISH");
                            loadingData.dismissWithAnimation();
                            if(isAnyAuto(HEADER_ID, INVENTORY_ITEM_ID)){
                                Log.e("CEK", "ADA COLLY AUTO");
                                dialogAuto();
                            } else {
                                Log.e("CEK", "GA ADA COLLY AUTO");
                                alertSuccess();
                            }
                        }
                    }, 3000);
                } else {
//                    insertProcedur(); //grf
//                    commit();
                    Cursor c = helper.selectItem(); //sesuai checkbox

                    if (c.moveToPosition(posisi)) {
                        String std_pack = c.getString(13);

                        dialogPilihStd(std_pack);
                    }
                }
            }
        }, 5000);

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("CEK", "FINISH");
//                loadingData.dismissWithAnimation();
//                if(isAnyAuto(HEADER_ID, INVENTORY_ITEM_ID)){
//                    Log.e("CEK", "ADA COLLY AUTO");
////                    listCollyAuto.clear();
////                    collyAuto();
////                    alertAuto();
//                    dialogAuto();
//                } else {
//                    Log.e("CEK", "GA ADA COLLY AUTO");
//                    alertSuccess();
//                }
//
//            }
//        }, 7000);
    }

    void goLoadUpdateNoQty() {
        Log.e("CEK", "CUMA UPDATE STATUS V");
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingData.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loadingData.setTitleText("\nLoading Data. . .");
        loadingData.setCancelable(false);
        loadingData.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusKdd();
                commit();
            }
        }, 1000);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateQtyKddsp();
//                commit();
//            }
//        }, 3000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("CEK", "FINISH");
                loadingData.dismissWithAnimation();
                alertSuccess();
            }
        }, 3000);
    }

    public void updateStatusKdd() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectItem(); //sesuai checkbox

        if (c.moveToPosition(posisi)) {
            String request = c.getString(1);
            String item_id = c.getString(6);
            String qty = c.getString(10);
            String line_num = c.getString(3);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE khs_detail_dospb_sp\n" +
                                "SET STATUS = 'V'\n" + //AV
                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                                "AND LINE_NUMBER = '" + line_num + "'");

                stmt.executeUpdate("UPDATE khs_detail_dospb_sp\n" +
                        "SET STATUS = 'V'\n" + //AV
                        "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                        "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                        "AND LINE_NUMBER = '" + line_num + "'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void updateQtyKddspInput(String qtyinput) {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectItem(); //sesuai checkbox

        if (c.moveToPosition(posisi)) {
            String request = c.getString(1);
            String item_id = c.getString(6);
            String qty = c.getString(10);
            String line_num = c.getString(3);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE khs_detail_dospb_sp\n" +
                                "SET ALLOCATED_QUANTITY = '" + qtyinput + "'\n" +
                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                                "AND LINE_NUMBER = '" + line_num + "'");

                stmt.executeUpdate("UPDATE khs_detail_dospb_sp\n" +
                        "SET ALLOCATED_QUANTITY = '" + qtyinput + "'\n" +
                        "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                        "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                        "AND LINE_NUMBER = '" + line_num + "'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void updateQtyKddsp(String alasan, String qty_verif) {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectItem(); //sesuai checkbox

        if (c.moveToPosition(posisi)) {
            String request = c.getString(1);
            String item_id = c.getString(6);
            String qty = c.getString(10);
            String line_num = c.getString(3);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE khs_detail_dospb_sp\n" +
                                "SET ALLOCATED_QUANTITY = '" + qty_verif + "', REASON = '"+alasan+"'\n" +
                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                                "AND LINE_NUMBER = '" + line_num + "'");

                stmt.executeUpdate("UPDATE khs_detail_dospb_sp\n" +
                        "SET ALLOCATED_QUANTITY = '" + qty_verif + "',  REASON = '"+alasan+"'\n" +
                        "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                        "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                        "AND LINE_NUMBER = '" + line_num + "'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public String getFinalCode(String code) {
        String data = "";
        Connection dbConnection = null;
        Statement statement = null;
        ResultSet theResultSet;


        dbConnection = mConn;
        try {
            statement = dbConnection.createStatement();
//            mQuery = "SELECT msib.SEGMENT1 \n" +
//                    "  FROM mtl_system_items_b msib\n" +
//                    " WHERE msib.organization_id = 225\n" +
//                    "   AND msib.inventory_item_status_code = 'Active'\n" +
//                    "   AND msib.segment1 LIKE\n" +
//                    "             '%'\n" +
//                    "          || (SELECT CASE\n" +
//                    "                        WHEN LENGTH ('"+code+"') = 10\n" +
//                    "                           THEN    SUBSTR ('"+code+"', 1, 5)\n" +
//                    "                                || '-'\n" +
//                    "                                || SUBSTR ('"+code+"', 6, 5)\n" +
//                    "                        ELSE '"+code+"'\n" +
//                    "                     END kode\n" +
//                    "                FROM DUAL)\n" +
//                    "          || '%'";

            mQuery = "SELECT msib.SEGMENT1\n" +
                    "  FROM mtl_system_items_b msib\n" +
                    " WHERE msib.organization_id = 81\n" + //225
                    "   AND msib.inventory_item_status_code = 'Active'\n" +
                    "   AND msib.segment1 LIKE\n" +
                    "             '%'\n" +
                    "          || (SELECT CASE\n" +
                    "                        WHEN LENGTH ('"+code+"') > 25\n" +
                    "                           THEN    SUBSTR ('"+code+"', 1, 5)\n" +
                    "                                || '-'\n" +
                    "                                || SUBSTR ('"+code+"', 6, 5)\n" +
                    "                        WHEN LENGTH ('"+code+"') = 10\n" +
                    "                           THEN    SUBSTR ('"+code+"', 1, 5)\n" +
                    "                                || '-'\n" +
                    "                                || SUBSTR ('"+code+"', 6, 5)\n" +
                    "                        ELSE '"+code+"'\n" +
                    "                     END kode\n" +
                    "                FROM DUAL)\n" +
                    "          || '%'";

            Log.d("QUERY", mQuery);
            ResultSet result = statement.executeQuery(mQuery);
            if (result.next()) {
                data = result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;

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

    public void alertNotMatch() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Alert")
                .setContentText("Item tidak cocok!")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent i = new Intent(getApplicationContext(), ItemAllocateActivity.class);
                        i.putExtra("HEADER_ID", HEADER_ID);
                        i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

    public void alertSuccess() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Proses verifikasi berhasil!")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent i = new Intent(getApplicationContext(), ItemAllocateActivity.class);
                        i.putExtra("HEADER_ID", HEADER_ID);
                        i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

    /////////////////////////////////////////////////


    public boolean isAnyAuto(String header, String item_id) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM khs_colly_tampung kct\n" +
                    "WHERE HEADER_ID = "+header+"\n" +
                    "AND ITEM_ID = "+item_id+"\n" +
                    "AND AUTO = 'Y'";
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

    public String collyAuto() {
        listCollyAuto.add("klik disini untuk lihat detail");
        Connection dbConnection = null;
        Statement statement = null;
        ResultSet theResultSet;
        String colly = "";

        dbConnection = mConn;
        try {
            statement = dbConnection.createStatement();
//            System.out
//                    .println("SELECT DISTINCT COLLY_NUMBER FROM khs_colly_tampung kct\n" +
//                            "   WHERE HEADER_ID = "+HEADER_ID+"\n" +
//                            "   AND ITEM_ID = "+INVENTORY_ITEM_ID+"\n" +
//                            "   AND AUTO = 'Y'\n" +
//                            "   ORDER BY 1 ASC");
//            theResultSet = statement
//                    .executeQuery("SELECT DISTINCT COLLY_NUMBER FROM khs_colly_tampung kct\n" +
//                            "   WHERE HEADER_ID = "+HEADER_ID+"\n" +
//                            "   AND ITEM_ID = "+INVENTORY_ITEM_ID+"\n" +
//                            "   AND AUTO = 'Y'\n" +
//                            "   ORDER BY 1 ASC");
            System.out
                    .println("SELECT colly_number\n" +
                            "  FROM (SELECT kct.colly_number,\n" +
                            "                 TO_NUMBER (TRIM (SUBSTR (kct.colly_number,\n" +
                            "                                            INSTR (kct.colly_number, '-', 1,\n" +
                            "                                               1)\n" +
                            "                                      + 1,\n" +
                            "                                        LENGTH (kct.colly_number)\n" +
                            "                                      - INSTR (kct.colly_number, '-', 1,\n" +
                            "                                               1)\n" +
                            "                                     )\n" +
                            "                             )\n" +
                            "                       ) cek\n" +
                            "        FROM KHS_COLLY_TAMPUNG kct\n" +
                            "        WHERE kct.header_id = "+HEADER_ID+"\n" +
                            "        AND kct.item_id = "+INVENTORY_ITEM_ID+"\n" +
                            "        AND kct.auto = 'Y'\n" +
                            "        ORDER BY 2 ASC)");
            theResultSet = statement
                    .executeQuery("SELECT colly_number\n" +
                            "  FROM (SELECT kct.colly_number,\n" +
                            "                 TO_NUMBER (TRIM (SUBSTR (kct.colly_number,\n" +
                            "                                            INSTR (kct.colly_number, '-', 1,\n" +
                            "                                               1)\n" +
                            "                                      + 1,\n" +
                            "                                        LENGTH (kct.colly_number)\n" +
                            "                                      - INSTR (kct.colly_number, '-', 1,\n" +
                            "                                               1)\n" +
                            "                                     )\n" +
                            "                             )\n" +
                            "                       ) cek\n" +
                            "        FROM KHS_COLLY_TAMPUNG kct\n" +
                            "        WHERE kct.header_id = "+HEADER_ID+"\n" +
                            "        AND kct.item_id = "+INVENTORY_ITEM_ID+"\n" +
                            "        AND kct.auto = 'Y'\n" +
                            "        ORDER BY 2 ASC)");
            while (theResultSet.next()) {
                colly = theResultSet.getString(1);
                listCollyAuto.add(colly);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return colly;
    }

    void dialogAuto(){
        View customAlertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_auto, null, false);
        final Spinner sp_collyAuto = customAlertDialogView.findViewById(R.id.sp_collyAuto);
        final TextView tv_info = customAlertDialogView.findViewById(R.id.tv_info);

        tv_info.setText(countAuto(HEADER_ID, INVENTORY_ITEM_ID)+" packing otomatis terbentuk");

        listCollyAuto.clear();
        collyAuto();
        ArrayAdapter<String> sadapter = new ArrayAdapter<String>(
                VerifikasiAllocateActivity.this, android.R.layout.simple_spinner_dropdown_item, listCollyAuto);
        sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_collyAuto.setAdapter(sadapter);

        sp_collyAuto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if(i==0){
//                } else {
//
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        new MaterialAlertDialogBuilder(VerifikasiAllocateActivity.this)
                .setView(customAlertDialogView)
                .setIcon(R.drawable.ic_info)
                .setTitle("Info")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), ItemAllocateActivity.class);
                        intent.putExtra("HEADER_ID", HEADER_ID);
                        intent.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                        startActivity(intent);
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public void alertAuto() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText(countAuto(HEADER_ID, INVENTORY_ITEM_ID)+" packing otomatis terbentuk\n" +
                        " "+listCollyAuto)
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent i = new Intent(getApplicationContext(), ItemAllocateActivity.class);
                        i.putExtra("HEADER_ID", HEADER_ID);
                        i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

    public void dialogPilihStd(String std_pack) {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Standar packing : "+std_pack)
                .setContentText("Ingin membagi packing otomatis ?")
                .setConfirmText("Ya")
                .setCancelText("Tidak")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                insertProcedur(); //grf
                                commit();
                            }
                        }, 1000);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("CEK", "FINISH");
                                loadingData.dismissWithAnimation();
                                if(isAnyAuto(HEADER_ID, INVENTORY_ITEM_ID)){
                                    Log.e("CEK", "ADA COLLY AUTO");
                                    dialogAuto();
                                } else {
                                    Log.e("CEK", "GA ADA COLLY AUTO");
                                    alertSuccess();
                                }

                            }
                        }, 3000);
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("CEK", "FINISH");
                                loadingData.dismissWithAnimation();
                                if(isAnyAuto(HEADER_ID, INVENTORY_ITEM_ID)){
                                    Log.e("CEK", "ADA COLLY AUTO");
                                    dialogAuto();
                                } else {
                                    Log.e("CEK", "GA ADA COLLY AUTO");
                                    alertSuccess();
                                }

                            }
                        }, 3000);
                        sDialog.cancel();
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

    public int countAuto(String header, String item_id) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM khs_colly_tampung kct\n" +
                    "WHERE HEADER_ID = "+header+" \n" +
                    "AND ITEM_ID = "+item_id+"\n" +
                    "AND AUTO = 'Y'";
            theResultSet = statement.executeQuery(mQuery);
            Log.d("Cek Status :", mQuery);
            System.out.println(" " + theResultSet + " ");
            if (theResultSet.next()) {
                ver = Integer.parseInt(theResultSet.getString(1));
                return ver;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void insertProcedur() {
        Log.e("CEK PRC", "INSERT PROCEDURE VERIF");
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectItem(); //sesuai checkbox

        if (c.moveToPosition(posisi)) {
            String request = c.getString(1);
            String header_id = c.getString(2);
            String line_id = c.getString(4);
            String item_id = c.getString(6);
            String qty = c.getString(10);
            String line_num = c.getString(3);
            String std_pack = c.getString(13);
            int jumlah_bagi = c.getInt(14);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                for (int i = 0; i < jumlah_bagi; i++) {
                    if(countKct(header_id) == 0){
                        int jumlahheader = countKct(header_id);
                        int urutan = jumlahheader + 1;
                        System.out
                                .println("BEGIN\n" +
                                        "khsdroidprc_input_kct(" + header_id + ", " + line_id + ", " + std_pack + ", 'C"+request+"-"+urutan+"'); \n" +
                                        "END;");

                        stmt.executeUpdate("BEGIN\n" +
                                "khsdroidprc_input_kct(" + header_id + ", " + line_id + ", " + std_pack + ", 'C"+request+"-"+urutan+"'); \n" +
                                "END;");

                        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        //update auto -buat bedain yang colly otomatis sama manual
                        System.out
                                .println("UPDATE KHS_COLLY_TAMPUNG SET AUTO = 'Y'\n" +
                                        "WHERE COLLY_NUMBER = 'C"+request+"-"+urutan+"' \n" +
                                        "AND LINE_ID = "+line_id+"");

                        stmt.executeUpdate("UPDATE KHS_COLLY_TAMPUNG SET AUTO = 'Y'\n" +
                                "WHERE COLLY_NUMBER = 'C"+request+"-"+urutan+"' \n" +
                                "AND LINE_ID = "+line_id+"");

                    } else {
                        String colly_number = getLastNum(header_id);
                        String[] parts = colly_number.split("-");
                        String part1 = parts[0];
                        String part2 = parts[1];
                        int urutan = Integer.parseInt(part2) +1 ;

                        System.out
                                .println("BEGIN\n" +
                                        "khsdroidprc_input_kct(" + header_id + ", " + line_id + ", " + std_pack + ", 'C"+request+"-"+urutan+"'); \n" +
                                        "END;");

                        stmt.executeUpdate("BEGIN\n" +
                                "khsdroidprc_input_kct(" + header_id + ", " + line_id + ", " + std_pack + ", 'C"+request+"-"+urutan+"'); \n" +
                                "END;");

                        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        //update auto -buat bedain yang colly otomatis sama manual
                        System.out
                                .println("UPDATE KHS_COLLY_TAMPUNG SET AUTO = 'Y'\n" +
                                        "WHERE COLLY_NUMBER = 'C"+request+"-"+urutan+"' \n" +
                                        "AND LINE_ID = "+line_id+"");

                        stmt.executeUpdate("UPDATE KHS_COLLY_TAMPUNG SET AUTO = 'Y'\n" +
                                "WHERE COLLY_NUMBER = 'C"+request+"-"+urutan+"' \n" +
                                "AND LINE_ID = "+line_id+"");
                    }
                }
                //khs_input_colly_dospb_sp

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public int countKct(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(kct.COLLY_NUMBER ) \n" +
                    "FROM KHS_COLLY_TAMPUNG kct\n" +
                    "WHERE kct.HEADER_ID = '"+header+"'";
            theResultSet = statement.executeQuery(mQuery);
            Log.d("Cek Status :", mQuery);
            System.out.println(" " + theResultSet + " ");
            if (theResultSet.next()) {
                ver = Integer.parseInt(theResultSet.getString(1));
                return ver;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    String getLastNum(String header){
        try{
            String colly_num = "";
            Statement statement = mConn.createStatement();
//            mQuery = "SELECT MAX(kct.colly_number) COLLY_NUMBER \n" +
//                    "FROM khs_colly_tampung kct\n" +
//                    "WHERE kct.header_id = '"+header+"'\n" +
//                    "ORDER BY kct.colly_number DESC ";

            mQuery = "SELECT colly_number\n" +
                    "  FROM (SELECT kct.colly_number,\n" +
                    "                 TO_NUMBER (TRIM (SUBSTR (kct.colly_number,\n" +
                    "                                            INSTR (kct.colly_number, '-', 1,\n" +
                    "                                                   1)\n" +
                    "                                          + 1,\n" +
                    "                                            LENGTH (kct.colly_number)\n" +
                    "                                          - INSTR (kct.colly_number, '-', 1,\n" +
                    "                                                   1)\n" +
                    "                                         )\n" +
                    "                                 )\n" +
                    "                           ) cek\n" +
                    "            FROM KHS_COLLY_TAMPUNG kct\n" +
                    "           WHERE kct.header_id = '"+header+"'\n" +
                    "        ORDER BY 2 DESC)\n" +
                    " WHERE ROWNUM = 1";

            Log.d("get last collynum : ",mQuery);
            ResultSet result = statement.executeQuery(mQuery);
            if (result.next()){
                colly_num = result.getString(1);
                return colly_num;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return "";
    }

}
