package com.quick.dospbsparepart;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.stetho.Stetho;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ItemCollyActivity extends AppCompatActivity implements RecyclerItem2.ItemClickListener {
    Context context;

    dbHelp helper;
    ManagerSessionUserOracle session;
    ImageView refresh, next;
    CheckBox cb_selectall;
    RecyclerView rv_item;
    RecyclerItem2 adapter;
    TextView tv_countSelected, tv_countTotal;
    ArrayList<DataRow2> mDatSet;

    String mQuery, subinv;
    String HEADER_ID, REQUEST_NUMBER, NOMOR_COLLY, AUTO;
    Connection mConn;
    int posisi, i;

    SweetAlertDialog loadingData;

    AlertDialog.Builder bApi;
    AlertDialog aApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_colly);
        session = new ManagerSessionUserOracle(this);
        new ModuleTool().allowNetworkOnMainThread();
        context = this;

        Stetho.initializeWithDefaults(this);

        next = findViewById(R.id.next);
        refresh = findViewById(R.id.refresh);
        rv_item = (RecyclerView) findViewById(R.id.rv_item);
        cb_selectall = (CheckBox) findViewById(R.id.cb_selectall2);

        HEADER_ID = getIntent().getStringExtra("HEADER_ID");
        REQUEST_NUMBER = getIntent().getStringExtra("REQUEST_NUMBER");
        NOMOR_COLLY = getIntent().getStringExtra("NOMOR_COLLY");
        AUTO = getIntent().getStringExtra("AUTO");

        Log.e("CEK AUTO", AUTO);

        helper = new dbHelp(this);

        mConn = session.connectDb();

        HashMap<String, String> userData = session.getUserData();
        final String person = userData.get(ManagerSessionUserOracle.KEY_PERSON);
        subinv = userData.get(ManagerSessionUserOracle.KEY_SUBINV);

        if (session.getSID().equals("DEV")) {
            setTitle("" + REQUEST_NUMBER + " | " + NOMOR_COLLY + " [ DEV ]");
        } else {
            setTitle("" + REQUEST_NUMBER + " | " + NOMOR_COLLY + " [ PROD ]");
        }

        mDatSet = getDataSet();
        adapter = new RecyclerItem2(this, mDatSet, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        tv_countSelected = (TextView) findViewById(R.id.tv_countSelected);
        tv_countTotal = (TextView) findViewById(R.id.tv_countTotal);
        rv_item.hasFixedSize();
        rv_item.setLayoutManager(layoutManager);
        rv_item.setAdapter(adapter);

        goLoadingData(REQUEST_NUMBER);

//        cb_selectall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                int jmlData = mDatSet.size();
//                if (compoundButton.isChecked()) {
//                    for (int i = 0; i < jmlData; i++) {
//                        mDatSet.get(i).setFlag("Y");
//                    }
//                    helper.updateFlagAll2("Y", NOMOR_COLLY);
//                    adapter.notifyDataSetChanged();
//                    setCountItem();
//                } else {
//                    int selectedTemp = 0;
//                    int totalTemp = 0;
//
//                    for (int c = 0; c < jmlData; c++) {
//                        if (!mDatSet.get(c).getFlag().equals("Y")) {
//                            selectedTemp = selectedTemp + 1;
//                            if (selectedTemp > 0) {
//                                adapter.notifyDataSetChanged();
//                            }
//                            setCountItem();
//                        } else {
//                            totalTemp = totalTemp + 1;
//                            if (totalTemp == jmlData) {
//                                for (int a = 0; a < jmlData; a++) {
//                                    mDatSet.get(a).setFlag("N");
//                                }
//                                Toast.makeText(ItemCollyActivity.this, "Unselected All", Toast.LENGTH_SHORT).show();
//                                helper.updateFlagAll2("N", NOMOR_COLLY);
//                                adapter.notifyDataSetChanged();
//                                setCountItem();
//                            }
//                        }
//                    }
//                }
//            }
//        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (helper.jumlahItemCollyY() == 0) {
                    Toast.makeText(ItemCollyActivity.this, "Tidak ada item yg dipilih", Toast.LENGTH_SHORT).show();
                } else {
                    next.setVisibility(View.GONE);
                    alertYaqueen();
                }
            }
        });
    }

    @Override
    public void OnItemClick(int position, View itemView) {
        posisi = position;
        if(AUTO.equals("Y")){
            Log.e("CEK", "Klik Cardview Auto");
        } else {
            dialogQty(posisi);
        }
    }

    void dialogQty(final int posisi) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View viewInflate = inflater.inflate(R.layout.activity_dialog_qty, (ViewGroup) findViewById(android.R.id.content), false);
        final LinearLayout ll_qty = (LinearLayout) viewInflate.findViewById(R.id.ll_qty);
        final TextView tv_item = (TextView) viewInflate.findViewById(R.id.tv_item);
        final TextView tv_desc = (TextView) viewInflate.findViewById(R.id.tv_desc);
        final EditText et_qtycolly = (EditText) viewInflate.findViewById(R.id.et_qtycolly);

        Cursor c = helper.selectItemColly(NOMOR_COLLY);
        c.moveToPosition(posisi);
        final String LINE_NUMBER = c.getString(c.getColumnIndexOrThrow("LINE_NUMBER"));
        final String SEGMENT1 = c.getString(c.getColumnIndexOrThrow("SEGMENT1"));
        final String DESCRIPTION = c.getString(c.getColumnIndexOrThrow("DESCRIPTION"));
        final String QTY_READY = c.getString(c.getColumnIndexOrThrow("QTY_READY"));

        tv_item.setText(SEGMENT1);
        tv_desc.setText(DESCRIPTION);
        et_qtycolly.setText(QTY_READY);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflate)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(et_qtycolly.length()==0){
                            Toast.makeText(getApplicationContext(),
                                    "Inputkan quantity!",
                                    Toast.LENGTH_LONG).show();
//                        } else if(et_qtycolly.getText().toString().equals("0")){ //qty boleh 0
//                            Toast.makeText(getApplicationContext(),
//                                    "Quantity tidak boleh 0!",
//                                    Toast.LENGTH_LONG).show();
                        } else if(Integer.parseInt(QTY_READY)<Integer.parseInt(et_qtycolly.getText().toString())){
                            Toast.makeText(getApplicationContext(),
                                    "Quantity tidak boleh melebihi allocate!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            //update qty
                            helper.update_qty_detail2(et_qtycolly.getText().toString(), LINE_NUMBER, NOMOR_COLLY);
                            showChanged();
                        }
                    }

                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        })
                .setCancelable(false)
                .create().show();
    }

    public void alertYaqueen() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Perhatian")
                .setContentText("Apakah anda yakin ingin melanjutkan proses?")
                .setConfirmText("Ya")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        next.setVisibility(View.VISIBLE);
                        prosesTampungan();
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelText("Tidak")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        next.setVisibility(View.VISIBLE);
                        sweetAlertDialog.cancel();
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

    void goLoadingData(final String req) {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingData.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loadingData.setTitleText("\nLoading Data. . .");
        loadingData.setCancelable(false);
        loadingData.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                helper.deleteItemColly(); //delete tabel item (colly)
                if(AUTO.equals("Y")){ //grf
                    insertDbAuto(req);
                } else {
                    insertDb(req);
                }
                setCountItem();
                showMessage();
                setCountItem();
                showMessage();
            }
        }, 1000);
    }

    public void alertItem() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Alert!")
                .setContentText("Item tidak tersedia!") //Semua DO / SPB sudah di Allocate
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent i = new Intent(getApplicationContext(), DOAllocateActivity.class);
                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

//    void insertDb(String request) {
//        Log.d("Conn", "." + mConn);
//        if (mConn == null) {
//            Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        try {
//            Statement statement = mConn.createStatement();
//
//            mQuery = "select kct.REQUEST_NUMBER\n" +
//                    "        \t,kct.HEADER_ID \n" +
//                    "        \t,kct.LINE_ID \n" +
//                    "        \t,kct.LINE_NUMBER\n" +
//                    "        \t,kct.ITEM\n" +
//                    "        \t,kct.ITEM_ID\n" +
//                    "        \t,kct.DESCRIPTION\n" +
//                    "        \t,kct.REQUIRED_QUANTITY\n" +
//                    "        \t,kct.ALLOCATED_QUANTITY\n" +
//                    "        \t,kct.QTY_PACKING\n" +
//                    "        \t,(kct.ALLOCATED_QUANTITY - kct.QTY_PACKING) qty_ready\n" +
//                    "        \t,kct.COLLY_FLAG \n" +
//                    "        \t,kct.ITEM_FLAG \n" +
//                    "    from khs_colly_tampung kct\n" +
//                    "    where kct.STATUS = 'V'\n" +
//                    "      --and kct.ITEM_FLAG = 'N'\n" +
//                    "      and nvl(kct.COLLY_FLAG,'NF') = 'NF'\n" +
//                    "       or kct.COLLY_NUMBER = '"+NOMOR_COLLY+"'\n" +
//                    "    order by decode ( kct.COLLY_FLAG,'F',1,'NF',2,3)\n" +
//                    "            ,kct.LINE_NUMBER";
//            Log.d("QUERY", mQuery);
//            ResultSet result = statement.executeQuery(mQuery);
//            while (result.next()) {
//                ContentValues values = new ContentValues();
//                values.put("REQUEST_NUMBER", result.getString(1));
//                values.put("HEADER_ID", result.getString(2));
//                values.put("LINE_ID", result.getString(3));
//                values.put("LINE_NUMBER", result.getString(4));
//                values.put("SEGMENT1", result.getString(5));
//                values.put("INVENTORY_ITEM_ID", result.getString(6));
//                values.put("DESCRIPTION", result.getString(7));
//                values.put("REQUIRED_QUANTITY", result.getString(8));
//                values.put("ALLOCATED_QUANTITY", result.getString(9));
//                values.put("QUANTITY_DETAILED", result.getString(11)); //10
//                values.put("QTY_PACKING", result.getString(10));
//                values.put("QTY_READY", result.getString(11));
//                values.put("COLLY_FLAG", result.getString(12));
//                values.put("ITEM_FLAG", result.getString(13));
//                values.put("NOMOR_COLLY", NOMOR_COLLY);
//                values.put("FLAG", "N");
//                helper.insertItemColly(values);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    void insertDb(String request) {
        Log.d("Conn", "." + mConn);
        if (mConn == null) {
            Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Statement statement = mConn.createStatement();

//            mQuery = "select *\n" +
//                    "from khs_qdroid_list_itemcolly kqli \n" +
//                    "where kqli.HEADER_ID = " + HEADER_ID + "\n" +
//                    "and (nvl (kqli.COLLY_FLAG,'NF') = 'NF'\n" +
//                    " or kqli.COLLY_NUMBER = '" + NOMOR_COLLY + "')\n" +
//                    "order by decode (kqli.COLLY_FLAG,'F',1,'NF',2)\n" +
//                    "        ,kqli.LINE_NUMBER";

            mQuery = "select distinct *\n" +
                    "from khs_qdroid_list_itemcolly kqli \n" +
                    "where kqli.HEADER_ID = "+HEADER_ID+"\n" +
                    "and kqli.STATUS = 'V'\n" +
                    "and kqli.ALLOCATED_QUANTITY <> '0'\n" +
                    "and (nvl (kqli.COLLY_FLAG,'NF') = 'NF'\n" +
                    " or kqli.COLLY_NUMBER = '"+NOMOR_COLLY+"')\n" +
                    "order by decode (kqli.COLLY_FLAG,'F',1,'NF',2)\n" +
                    "        ,kqli.LINE_NUMBER";

            Log.d("QUERY", mQuery);
            ResultSet result = statement.executeQuery(mQuery);
            while (result.next()) {
                ContentValues values = new ContentValues();
                values.put("HEADER_ID", result.getString(1));
                values.put("LINE_ID", result.getString(2));
                values.put("REQUEST_NUMBER", result.getString(3));
//                values.put("ORGANIZATION_ID", result.getString(4));
                values.put("SEGMENT1", result.getString(5));
                values.put("INVENTORY_ITEM_ID", result.getString(6));
                values.put("DESCRIPTION", result.getString(7));
                values.put("LOKASI_SIMPAN", result.getString(8));
                values.put("LINE_NUMBER", result.getString(9));
                values.put("REQUIRED_QUANTITY", result.getString(10));
                values.put("ALLOCATED_QUANTITY", result.getString(11));
//                values.put("STATUS", result.getString(12));
//                values.put("COLLY_NUMBER", result.getString(13));
                values.put("QTY_PACKING", result.getString(14));
                values.put("QTY_INPUT", result.getString(15));
                values.put("QTY_READY", result.getString(16));
                values.put("QUANTITY_DETAILED", result.getString(16)); //11
                if (result.getString(17) == null) {
                    values.put("COLLY_FLAG", "NF");
                } else {
                    values.put("COLLY_FLAG", result.getString(17));
                }
                values.put("ITEM_FLAG", result.getString(18));
                values.put("NOMOR_COLLY", NOMOR_COLLY);
                values.put("FLAG", "N");
                helper.insertItemColly(values);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void insertDbAuto(String request) {
        Log.d("Conn", "." + mConn);
        if (mConn == null) {
            Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Statement statement = mConn.createStatement();

            mQuery = "select distinct *\n" +
                    "from khs_qdroid_list_itemcolly kqli \n" +
                    "where kqli.HEADER_ID = "+HEADER_ID+"\n" +
                    "and kqli.STATUS = 'V'\n" +
                    "and kqli.COLLY_NUMBER = '"+NOMOR_COLLY+"'\n" +
                    "order by decode (kqli.COLLY_FLAG,'F',1,'NF',2)\n" +
                    "        ,kqli.LINE_NUMBER";

            Log.d("QUERY", mQuery);
            ResultSet result = statement.executeQuery(mQuery);
            while (result.next()) {
                ContentValues values = new ContentValues();
                values.put("HEADER_ID", result.getString(1));
                values.put("LINE_ID", result.getString(2));
                values.put("REQUEST_NUMBER", result.getString(3));
//                values.put("ORGANIZATION_ID", result.getString(4));
                values.put("SEGMENT1", result.getString(5));
                values.put("INVENTORY_ITEM_ID", result.getString(6));
                values.put("DESCRIPTION", result.getString(7));
                values.put("LOKASI_SIMPAN", result.getString(8));
                values.put("LINE_NUMBER", result.getString(9));
                values.put("REQUIRED_QUANTITY", result.getString(10));
                values.put("ALLOCATED_QUANTITY", result.getString(11));
//                values.put("STATUS", result.getString(12));
//                values.put("COLLY_NUMBER", result.getString(13));
                values.put("QTY_PACKING", result.getString(14));
                values.put("QTY_INPUT", result.getString(15));
                values.put("QTY_READY", result.getString(16));
                values.put("QUANTITY_DETAILED", result.getString(16)); //11
                if (result.getString(17) == null) {
                    values.put("COLLY_FLAG", "NF");
                } else {
                    values.put("COLLY_FLAG", result.getString(17));
                }
                values.put("ITEM_FLAG", result.getString(18));
                values.put("NOMOR_COLLY", NOMOR_COLLY);
                values.put("FLAG", "N");
                helper.insertItemColly(values);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //INI BARU NYOBA
    void showMessage() {
        mDatSet.clear();
        mDatSet.addAll(getDataSet());
        adapter.notifyDataSetChanged();
        loadingData.dismissWithAnimation();
    }

    void showChanged() {
        mDatSet.clear();
        mDatSet.addAll(getDataSet());
        adapter.notifyDataSetChanged();
    }

    void getMaxChecked() {
        int selectedTemp = 0;
        Log.e("MDATSET SIZE", String.valueOf(mDatSet.size()));
        for (int a = 0; a < mDatSet.size(); a++) {
            if (mDatSet.get(a).getFlag().equals("Y")) {
                selectedTemp = selectedTemp + 1;
            }
        }
        int totalTemp = mDatSet.size();
        if (selectedTemp == totalTemp) {
            cb_selectall.setChecked(true);
        } else {
            cb_selectall.setChecked(false);
        }
    }

    void setCountItem() {
        int selectedTemp = 0;
        String total = Integer.toString(mDatSet.size());
        Log.e("MDATSET SIZE", String.valueOf(mDatSet.size()));
        if (mDatSet.size() > 0)
            for (int i = 0; i < mDatSet.size(); i++) {
                tv_countTotal.setText(total);
                if (mDatSet.get(i).getFlag().equals("Y")) {
                    selectedTemp = selectedTemp + 1;
                }
            }
        tv_countSelected.setText(Integer.toString(selectedTemp));
    }

    //NYAMPE SINI

    ArrayList<DataRow2> getDataSet() {
        ArrayList<DataRow2> dataSet = new ArrayList<>();

        Cursor c = helper.selectItemColly(NOMOR_COLLY);
        while (c.moveToNext()) {
            DataRow2 data = new DataRow2();
            data.setData(
                    c.getString(c.getColumnIndex(helper.COLLY_REQUEST_NUMBER)),
                    c.getString(c.getColumnIndex(helper.COLLY_HEADER_ID)),
                    c.getString(c.getColumnIndex(helper.COLLY_LINE_ID)),
                    c.getString(c.getColumnIndex(helper.COLLY_LINE_NUMBER)),
                    c.getString(c.getColumnIndex(helper.COLLY_SEGMENT1)),
                    c.getString(c.getColumnIndex(helper.COLLY_INVENTORY_ITEM_ID)),
                    c.getString(c.getColumnIndex(helper.COLLY_DESCRIPTION)),
                    c.getString(c.getColumnIndex(helper.COLLY_LOKASI_SIMPAN)),
                    c.getString(c.getColumnIndex(helper.COLLY_REQUIRED_QUANTITY)),
                    c.getString(c.getColumnIndex(helper.COLLY_ALLOCATED_QUANTITY)),
                    c.getString(c.getColumnIndex(helper.COLLY_QUANTITY_DETAILED)),
                    c.getString(c.getColumnIndex(helper.COLLY_QTY_PACKING)),
                    c.getString(c.getColumnIndex(helper.COLLY_QTY_INPUT)),
                    c.getString(c.getColumnIndex(helper.COLLY_QTY_READY)),
                    c.getString(c.getColumnIndex(helper.COLLY_COLLY_FLAG)),
                    c.getString(c.getColumnIndex(helper.COLLY_ITEM_FLAG)),
                    c.getString(c.getColumnIndex(helper.COLLY_NOMOR_COLLY)),
                    c.getString(c.getColumnIndex(helper.COLLY_FLAG))
//                    c.getString(c.getColumnIndex(helper._id))
            );
            dataSet.add(data);
        }

        return dataSet;
    }

    public void updateCollyNumber() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectUpdateFlagCheck(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);
            String colly_num = c.getString(4);
            String header_id = c.getString(5);
            String line_id = c.getString(6);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE KHS_COLLY_TAMPUNG \n" +
                                "SET COLLY_NUMBER = '" + colly_num + "' \n" +
                                "WHERE HEADER_ID = '" + header_id + "'\n" +
                                "AND LINE_ID = '" + line_id + "'\n" +
                                "AND COLLY_NUMBER is NULL");

                stmt.executeUpdate("UPDATE KHS_COLLY_TAMPUNG \n" +
                        "SET COLLY_NUMBER = '" + colly_num + "' \n" +
                        "WHERE HEADER_ID = '" + header_id + "'\n" +
                        "AND LINE_ID = '" + line_id + "'\n" +
                        "AND COLLY_NUMBER is NULL");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void updateQtyInput() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectUpdateFlagCheck(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);
            String colly_num = c.getString(4);
            String header_id = c.getString(5);
            String line_id = c.getString(6);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE KHS_COLLY_TAMPUNG \n" +
                                "SET QTY_INPUT = '" + qty + "' \n" +
                                "WHERE HEADER_ID = '" + header_id + "'\n" +
                                "AND LINE_ID = '" + line_id + "'\n" +
                                "AND COLLY_NUMBER = '" + colly_num + "'");

                stmt.executeUpdate("UPDATE KHS_COLLY_TAMPUNG \n" +
                        "SET QTY_INPUT = '" + qty + "' \n" +
                        "WHERE HEADER_ID = '" + header_id + "'\n" +
                        "AND LINE_ID = '" + line_id + "'\n" +
                        "AND COLLY_NUMBER = '" + colly_num + "'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void updateQtyPacking() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectUpdateFlagCheck(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);
            String colly_num = c.getString(4);
            String header_id = c.getString(5);
            String line_id = c.getString(6);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE KHS_COLLY_TAMPUNG \n" +
                                "SET QTY_PACKING = QTY_PACKING + " + qty + " \n" +
                                "WHERE LINE_ID = " + line_id + "");

                stmt.executeUpdate("UPDATE KHS_COLLY_TAMPUNG \n" +
                        "SET QTY_PACKING = QTY_PACKING + " + qty + " \n" +
                        "WHERE LINE_ID = " + line_id + "");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void insertKctNF() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectInsertKctNF(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);
            String colly_num = c.getString(4);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("INSERT INTO KHS_COLLY_TAMPUNG \n" +
                                "select kct.HEADER_ID\n" +
                                "      ,kct.LINE_ID\n" +
                                "      ,kct.REQUEST_NUMBER\n" +
                                "      ,kct.DELIVERY_TYPE \n" +
                                "      ,kct.ORGANIZATION_ID \n" +
                                "      ,kct.ITEM\n" +
                                "      ,kct.ITEM_ID\n" +
                                "      ,kct.DESCRIPTION \n" +
                                "      ,kct.LINE_NUMBER \n" +
                                "      ,kct.REQUIRED_QUANTITY \n" +
                                "      ,kct.ALLOCATED_QUANTITY \n" +
                                "      ,kct.FROM_SUBINVENTORY_CODE \n" +
                                "      ,kct.TO_SUBINVENTORY_CODE \n" +
                                "      ,kct.TO_LOCATOR_ID \n" +
                                "      ,kct.STATUS \n" +
                                "      ,'" + NOMOR_COLLY + "' colly_number\n" +
                                "      ,kct.QTY_PACKING\n" +
                                "      ," + qty + " qty_input\n" +
                                "      ,'NF' colly_flag \n" +
                                "      ,'Y' item_flag\n" +
                                "from khs_colly_tampung kct\n" +
                                "WHERE kct.ITEM_ID = '" + item_id + "'\n" +
                                "AND kct.LINE_NUMBER = '" + line_num + "'\n" +
                                "AND kct.COLLY_FLAG = 'NF'");

                stmt.executeUpdate("INSERT INTO KHS_COLLY_TAMPUNG \n" +
                        "select kct.HEADER_ID\n" +
                        "      ,kct.LINE_ID\n" +
                        "      ,kct.REQUEST_NUMBER\n" +
                        "      ,kct.DELIVERY_TYPE \n" +
                        "      ,kct.ORGANIZATION_ID \n" +
                        "      ,kct.ITEM\n" +
                        "      ,kct.ITEM_ID\n" +
                        "      ,kct.DESCRIPTION \n" +
                        "      ,kct.LINE_NUMBER \n" +
                        "      ,kct.REQUIRED_QUANTITY \n" +
                        "      ,kct.ALLOCATED_QUANTITY \n" +
                        "      ,kct.FROM_SUBINVENTORY_CODE \n" +
                        "      ,kct.TO_SUBINVENTORY_CODE \n" +
                        "      ,kct.TO_LOCATOR_ID \n" +
                        "      ,kct.STATUS \n" +
                        "      ,'" + NOMOR_COLLY + "' colly_number\n" +
                        "      ,kct.QTY_PACKING\n" +
                        "      ," + qty + " qty_input\n" +
                        "      ,'NF' colly_flag \n" +
                        "      ,'Y' item_flag\n" +
                        "from khs_colly_tampung kct\n" +
                        "WHERE kct.ITEM_ID = '" + item_id + "'\n" +
                        "AND kct.LINE_NUMBER = '" + line_num + "'\n" +
                        "AND kct.COLLY_FLAG = 'NF'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void insertProcedur() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectInsertProcedure(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);
            String colly_num = c.getString(4);
            String header_id = c.getString(5);
            String line_id = c.getString(6);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                //khs_input_colly_dospb_sp
                System.out
                        .println("BEGIN\n" +
                                "\tkhsdroidprc_input_kct(" + header_id + ", " + line_id + ", " + qty + ", '" + colly_num + "'); \n" +
                                "END;");

                stmt.executeUpdate("BEGIN\n" +
                        "\tkhsdroidprc_input_kct(" + header_id + ", " + line_id + ", " + qty + ", '" + colly_num + "'); \n" +
                        "END;");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void updateCollyFlag() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectUpdateFlagCheck(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);
            String colly_num = c.getString(4);
            String header_id = c.getString(5);
            String line_id = c.getString(6);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("update khs_colly_tampung kct\n" +
                                "   set kct.COLLY_FLAG = case when (select sum (xx.QTY_INPUT)\n" +
                                "                                     from khs_colly_tampung xx \n" +
                                "                                    where xx.ITEM_ID = kct.ITEM_ID\n" +
                                "                                      and xx.LINE_NUMBER = kct.LINE_NUMBER\n" +
                                "                                      ) = kct.ALLOCATED_QUANTITY\n" +
                                "                             then 'F'\n" +
                                "                        else 'NF'\n" +
                                "                        end \n" +
                                "where kct.ITEM_ID = " + item_id + "\n" +
                                "  and kct.LINE_NUMBER = " + line_num + "");

                stmt.executeUpdate("update khs_colly_tampung kct\n" +
                        "   set kct.COLLY_FLAG = case when (select sum (xx.QTY_INPUT)\n" +
                        "                                     from khs_colly_tampung xx \n" +
                        "                                    where xx.ITEM_ID = kct.ITEM_ID\n" +
                        "                                      and xx.LINE_NUMBER = kct.LINE_NUMBER\n" +
                        "                                      ) = kct.ALLOCATED_QUANTITY\n" +
                        "                             then 'F'\n" +
                        "                        else 'NF'\n" +
                        "                        end \n" +
                        "where kct.ITEM_ID = " + item_id + "\n" +
                        "  and kct.LINE_NUMBER = " + line_num + "");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    public void updateItemFlag() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectUpdateFlagCheck(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);
            String colly_num = c.getString(4);
            String header_id = c.getString(5);
            String line_id = c.getString(6);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE KHS_COLLY_TAMPUNG \n" +
                                "SET ITEM_FLAG = 'Y' \n" +
                                "WHERE HEADER_ID = '" + header_id + "'\n" +
                                "AND LINE_ID = '" + line_id + "'");

                stmt.executeUpdate("UPDATE KHS_COLLY_TAMPUNG \n" +
                        "SET ITEM_FLAG = 'Y' \n" +
                        "WHERE HEADER_ID = '" + header_id + "'\n" +
                        "AND LINE_ID = '" + line_id + "'");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
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

    void prosesTampungan() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View viewInflated = inflater.inflate(R.layout.activity_loading, (ViewGroup) findViewById(android.R.id.content), false);

        bApi = new AlertDialog.Builder(this);
        bApi.setView(viewInflated).setCancelable(false);

        aApi = bApi.create();
        aApi.show();
        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateCollyNumber();
//                commit();
//            }
//        }, 500);
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateQtyInput();
//                commit();
//            }
//        }, 1000);
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateQtyPacking();
//                commit();
//            }
//        }, 1500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                insertProcedur();
                commit();
                Log.e("CEK", "INSERT PROCEDURE");
            }
        }, 1000);

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                insertKctNF();
//                commit();
//            }
//        }, 2000);
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateCollyFlag();
//                commit();
//            }
//        }, 4000);

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateItemFlag();
//                commit();
//            }
//        }, 5000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                aApi.dismiss();
                alertsuccess();
            }
        }, 7000);
    }

    public void alertsuccess() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Proses Berhasil!")
                .setConfirmText("Ya")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        next.setVisibility(View.VISIBLE);
//                        Intent i = new Intent(getApplicationContext(), BagiCollyActivity.class); // kembali ke list do
//                        i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
//                        startActivity(i);
                        finish();
                        sDialog.dismissWithAnimation();
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Not calling **super**, disables back button in current screen.
//        Intent i = new Intent(ItemCollyActivity.this, BagiCollyActivity.class);
//        i.putExtra("HEADER_ID", HEADER_ID);
//        i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
//        startActivity(i);
        finish();
    }
}
