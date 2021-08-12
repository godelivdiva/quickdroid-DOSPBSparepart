package com.quick.dospbsparepart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ItemAllocateActivity extends AppCompatActivity implements RecyclerItem.ItemClickListener{
    Context context;

    dbHelp helper;
    ManagerSessionUserOracle session;
    ImageView refresh, next;
    CheckBox cb_selectall;
    RecyclerView rv_item;
    RecyclerItem adapter;
    TextView tv_countSelected, tv_countTotal;
    ArrayList<DataRow> mDatSet;
    List<String> listCollyAuto;

    String mQuery, subinv;
    String HEADER_ID,REQUEST_NUMBER;
    Connection mConn;
    int posisi, i;

    SweetAlertDialog loadingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_allocate);
        session = new ManagerSessionUserOracle(this);
        new ModuleTool().allowNetworkOnMainThread();
        context = this;

        Stetho.initializeWithDefaults(this);

        next = findViewById(R.id.next);
        refresh = findViewById(R.id.refresh);
        rv_item = (RecyclerView) findViewById(R.id.rv_item);
        cb_selectall = (CheckBox) findViewById(R.id.cb_selectall);

        helper = new dbHelp(this);

        mConn = session.connectDb();

        mDatSet = getDataSet();
        adapter = new RecyclerItem(this, mDatSet, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        tv_countSelected = (TextView) findViewById(R.id.tv_countSelected);
        tv_countTotal = (TextView) findViewById(R.id.tv_countTotal);
        rv_item.hasFixedSize();
        rv_item.setLayoutManager(layoutManager);
        rv_item.setAdapter(adapter);

        HashMap<String, String> userData = session.getUserData();
        final String person = userData.get(ManagerSessionUserOracle.KEY_PERSON);
        subinv = userData.get(ManagerSessionUserOracle.KEY_SUBINV);

        HEADER_ID = getIntent().getStringExtra("HEADER_ID");
        REQUEST_NUMBER = getIntent().getStringExtra("REQUEST_NUMBER");

        if(session.getSID().equals("DEV")){
            setTitle("PELAYANAN [ DEV ] - "+REQUEST_NUMBER+"");
        }else {
            setTitle("PELAYANAN [ PROD ] - "+REQUEST_NUMBER+"");
        }

        listCollyAuto = new ArrayList<String>();

        goLoadingData(HEADER_ID,REQUEST_NUMBER);

        cb_selectall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int jmlData = mDatSet.size();
                if (compoundButton.isChecked()) {
                    for (int i = 0; i < jmlData; i++) {
                        mDatSet.get(i).setFlag("Y");
                    }
                    helper.updateFlagAll("Y");
                    adapter.notifyDataSetChanged();
                    setCountItem();
                } else {
                    int selectedTemp = 0;
                    int totalTemp = 0;

                    for (int c = 0; c < jmlData; c++) {
                        if (!mDatSet.get(c).getFlag().equals("Y")) {
                            selectedTemp = selectedTemp + 1;
                            if (selectedTemp > 0) {
                                adapter.notifyDataSetChanged();
                            }
                            setCountItem();
                        } else {
                            totalTemp = totalTemp + 1;
                            if (totalTemp == jmlData) {
                                for (int a = 0; a < jmlData; a++) {
                                    mDatSet.get(a).setFlag("N");
                                }
                                Toast.makeText(ItemAllocateActivity.this, "Unselected All", Toast.LENGTH_SHORT).show();
                                helper.updateFlagAll("N");
                                adapter.notifyDataSetChanged();
                                setCountItem();
                            }
                        }
                    }
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(isAnyAuto(HEADER_ID)){ //grf
//                    Log.e("CEK", "ADA AUTO PACKING");
//                    listCollyAuto.clear();
//                    collyAuto();
//                    alertAuto();
//                } else {
//                    Log.e("CEK", "TIDAK ADA AUTO PACKING");
//                    Intent intent = new Intent(getApplicationContext(), DOAllocateActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
                Intent intent = new Intent(getApplicationContext(), DOAllocateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    public void confirmAllocate() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Confirmation")
                .setContentText("Apakah anda yakin akan melakukan allocate?")
                .setCancelText("Tidak")
                .setConfirmText("Ya")
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        goLoadAllocate();
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .show();
    }

    void goLoadAllocate() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingData.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loadingData.setTitleText("\nLoading Data. . .");
        loadingData.setCancelable(false);
        loadingData.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusKddV();
                commit();
            }
        }, 1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("CEK", "FINISH");
                loadingData.dismissWithAnimation();
                alertSuccess();
            }
        }, 3000);
    }

    public void updateStatusKddV() {
        Statement stmt = null;
        Connection conn = null;

        Cursor c = helper.selectUpdateStatusV(); //sesuai checkbox

        while (c.moveToNext()) {
            String request = c.getString(0);
            String item_id = c.getString(1);
            String qty = c.getString(2);
            String line_num = c.getString(3);

            try {

                conn = mConn;
                stmt = conn.createStatement();

                System.out
                        .println("UPDATE khs_detail_dospb_sp\n" +
                                "SET STATUS = 'V'\n" +
                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                                "AND LINE_NUMBER = '" + line_num + "'");

                stmt.executeUpdate("UPDATE khs_detail_dospb_sp\n" +
                        "SET STATUS = 'V'\n" +
                        "WHERE REQUEST_NUMBER = '" + request + "' \n" +
                        "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
                        "AND LINE_NUMBER = '" + line_num + "'");

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

    public void alertSuccess() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Proses pelayanan berhasil!")
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

    @Override
    public void OnItemClick(int position, View itemView) {
        posisi = position;
//        dialog konfirmasi
//        confirmQty(posisi);
        Cursor c = helper.selectItem();
        c.moveToPosition(posisi);
        final String INVENTORY_ITEM_ID = c.getString(c.getColumnIndexOrThrow("INVENTORY_ITEM_ID"));
        final String SEGMENT1 = c.getString(c.getColumnIndexOrThrow("SEGMENT1"));
        final String DESCRIPTION = c.getString(c.getColumnIndexOrThrow("DESCRIPTION"));
        final String REQUIRED_QUANTITY = c.getString(c.getColumnIndexOrThrow("REQUIRED_QUANTITY"));
        final String ALLOCATED_QUANTITY = c.getString(c.getColumnIndexOrThrow("ALLOCATED_QUANTITY"));
        final String QUANTITY_DETAILED = c.getString(c.getColumnIndexOrThrow("QUANTITY_DETAILED"));
        final String STATUS = c.getString(c.getColumnIndexOrThrow("STATUS"));
        final String STD_PACK = c.getString(c.getColumnIndexOrThrow("STD_PACK"));

        if(STATUS.equals("V")){
            Toast.makeText(getApplicationContext(), "Sudah Verifikasi",
                    Toast.LENGTH_LONG).show();
//            Snackbar.make(itemView, R.string.verifikasi, Snackbar.LENGTH_SHORT)
//                    .show();
        } else {
            Intent i = new Intent(getApplicationContext(), VerifikasiAllocateActivity.class);
            i.putExtra("HEADER_ID", HEADER_ID);
            i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
            i.putExtra("INVENTORY_ITEM_ID", INVENTORY_ITEM_ID);
            i.putExtra("SEGMENT1", SEGMENT1);
            i.putExtra("DESCRIPTION", DESCRIPTION);
            i.putExtra("REQUIRED_QUANTITY", REQUIRED_QUANTITY);
            i.putExtra("ALLOCATED_QUANTITY", ALLOCATED_QUANTITY);
            i.putExtra("QUANTITY_DETAILED", QUANTITY_DETAILED);
            i.putExtra("STD_PACK", STD_PACK);
            i.putExtra("POSITION", posisi);
            i.putExtra("FLAG_QTY", "Y");
            startActivity(i);
        }

    }

    void goLoadingData(final String header_id, final String request) {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingData.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loadingData.setTitleText("\nLoading Data. . .");
        loadingData.setCancelable(false);
        loadingData.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                helper.deleteItem(); //delete tabel item (allocate)
                if (isAny(header_id)) {
                    insertDb(header_id);
                    setCountItem();
                    showMessage();
                } else {
                    loadingData.dismissWithAnimation();
                    alertItem();
                }


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

    public boolean isAnyAuto(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM khs_colly_tampung kct\n" +
                    "WHERE HEADER_ID = "+header+"\n" +
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

    public int countAuto(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM khs_colly_tampung kct\n" +
                    "WHERE HEADER_ID = "+header+" \n" +
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


    public String collyAuto() {
        Connection dbConnection = null;
        Statement statement = null;
        ResultSet theResultSet;
        String colly = "";

        dbConnection = mConn;
        try {
            statement = dbConnection.createStatement();
            System.out
                    .println("SELECT DISTINCT COLLY_NUMBER FROM khs_colly_tampung kct\n" +
                            "   WHERE HEADER_ID = "+HEADER_ID+"\n" +
                            "   AND AUTO = 'Y'\n" +
                            "   ORDER BY 1 ASC");
            theResultSet = statement
                    .executeQuery("SELECT DISTINCT COLLY_NUMBER FROM khs_colly_tampung kct\n" +
                            "   WHERE HEADER_ID = "+HEADER_ID+"\n" +
                            "   AND AUTO = 'Y'\n" +
                            "   ORDER BY 1 ASC");
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

    public void alertAuto() {
        loadingData = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Info")
                .setContentText(countAuto(HEADER_ID)+" packing otomatis terbentuk\n" +
                        " "+listCollyAuto)
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent intent = new Intent(getApplicationContext(), DOAllocateActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        loadingData.show();
        loadingData.setCanceledOnTouchOutside(false);
    }

    public boolean isAny(String header) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "select count(*)\n" +
                    "from khs_qdroid_allocate_detail_sp kqadsp\n" +
                    "where kqadsp.HEADER_ID = "+header+"\n" +
                    "order by kqadsp.REQUEST_NUMBER\n" +
                    "        ,kqadsp.LINE_NUMBER";
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

    void insertDb(String header) {
        Log.d("Conn", "." + mConn);
        if (mConn == null) {
            Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Statement statement = mConn.createStatement();

//            mQuery = "select kqadsp.*\n" +
//                    "from khs_qdroid_allocate_detail_sp kqadsp\n" +
//                    "where kqadsp.HEADER_ID = '"+header+"'\n" +
//                    "order by kqadsp.REQUEST_NUMBER\n" +
//                    "        ,kqadsp.LOKASI_SIMPAN\n" +
//                    "        ,kqadsp.LINE_NUMBER";

            mQuery = "select kqadsp.* , FLOOR(kqadsp.ALLOCATED_QUANTITY / kqadsp.STD_PACK)\n" +
                    "    from khs_qdroid_allocate_detail_sp kqadsp\n" +
                    "    where kqadsp.HEADER_ID = '"+header+"'\n" +
                    "    order by kqadsp.REQUEST_NUMBER\n" +
                    "            ,kqadsp.LOKASI_SIMPAN\n" +
                    "            ,kqadsp.LINE_NUMBER";
            Log.d("QUERY", mQuery);
            ResultSet result = statement.executeQuery(mQuery);
            while (result.next()) {
                ContentValues values = new ContentValues();
                values.put("REQUEST_NUMBER", result.getString(1));
                values.put("HEADER_ID", result.getString(2));
                values.put("LINE_NUMBER", result.getString(3));
                values.put("LINE_ID", result.getString(4));
                values.put("SEGMENT1", result.getString(5));
                values.put("INVENTORY_ITEM_ID", result.getString(6));
                values.put("DESCRIPTION", result.getString(7));
                values.put("LOKASI_SIMPAN", result.getString(8));
                values.put("REQUIRED_QUANTITY", result.getString(9));
                values.put("ALLOCATED_QUANTITY", result.getString(10));
                values.put("QUANTITY_DETAILED", result.getString(10));
                values.put("STATUS", result.getString(11));
                values.put("STD_PACK", result.getString(12));
//                values.put("JUMLAH_BAGI", Math.floor(result.getInt(9) / result.getInt(11)));
                if(result.getString(13)==null){
                    values.put("JUMLAH_BAGI", "");
                } else {
                    values.put("JUMLAH_BAGI", result.getString(13));
                }
                values.put("FLAG", "N");
                helper.insertItem(values);
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

    ArrayList<DataRow> getDataSet() {
        ArrayList<DataRow> dataSet = new ArrayList<>();

        Cursor c = helper.selectItem();
        while (c.moveToNext()) {
            DataRow data = new DataRow();
            data.setData(
                    c.getString(c.getColumnIndex(helper.REQUEST_NUMBER)),
                    c.getString(c.getColumnIndex(helper.HEADER_ID)),
                    c.getString(c.getColumnIndex(helper.LINE_NUMBER)),
                    c.getString(c.getColumnIndex(helper.SEGMENT1)),
                    c.getString(c.getColumnIndex(helper.INVENTORY_ITEM_ID)),
                    c.getString(c.getColumnIndex(helper.DESCRIPTION)),
                    c.getString(c.getColumnIndex(helper.LOKASI_SIMPAN)),
                    c.getString(c.getColumnIndex(helper.REQUIRED_QUANTITY)),
                    c.getString(c.getColumnIndex(helper.ALLOCATED_QUANTITY)),
                    c.getString(c.getColumnIndex(helper.QUANTITY_DETAILED)),
                    c.getString(c.getColumnIndex(helper.STATUS)),
                    c.getString(c.getColumnIndex(helper.FLAG)),
                    c.getString(c.getColumnIndex(helper._id))
            );
            dataSet.add(data);
        }

        return dataSet;
    }

//    public void confirmUpdate(){
//        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
//                .setTitleText("Confirmation")
//                .setContentText("Apakah anda yakin akan melakukan update?")
//                .setCancelText("Tidak")
//                .setConfirmText("Ya")
//                .showCancelButton(true)
//                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sDialog) {
//                        next.setVisibility(View.GONE);
//                        goLoadUpdateNonSerial();
//                        sDialog.dismissWithAnimation();
//                    }
//                })
//                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sDialog) {
//                        next.setVisibility(View.VISIBLE);
//                        sDialog.cancel();
//                    }
//                })
//                .show();
//    }

        public void confirmQty(final int posisi){
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Confirmation")
                .setContentText("Apakah anda akan mengubah quantity?")
                .setCancelText("Tidak")
                .setConfirmText("Ya")
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Cursor c = helper.selectItem();
                        c.moveToPosition(posisi);
                        final String SEGMENT1 = c.getString(c.getColumnIndexOrThrow("SEGMENT1"));
                        final String DESCRIPTION = c.getString(c.getColumnIndexOrThrow("DESCRIPTION"));
                        final String REQUIRED_QUANTITY = c.getString(c.getColumnIndexOrThrow("REQUIRED_QUANTITY"));
                        final String ALLOCATED_QUANTITY = c.getString(c.getColumnIndexOrThrow("ALLOCATED_QUANTITY"));
                        final String QUANTITY_DETAILED = c.getString(c.getColumnIndexOrThrow("QUANTITY_DETAILED"));
                        final String STATUS = c.getString(c.getColumnIndexOrThrow("STATUS"));

                        Intent i = new Intent(getApplicationContext(), VerifikasiAllocateActivity.class);
                        i.putExtra("HEADER_ID", HEADER_ID);
                        i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                        i.putExtra("SEGMENT1", SEGMENT1);
                        i.putExtra("DESCRIPTION", DESCRIPTION);
                        i.putExtra("REQUIRED_QUANTITY", REQUIRED_QUANTITY);
                        i.putExtra("ALLOCATED_QUANTITY", ALLOCATED_QUANTITY);
                        i.putExtra("QUANTITY_DETAILED", QUANTITY_DETAILED);
                        i.putExtra("POSITION", posisi);
                        i.putExtra("FLAG_QTY", "Y");
                        startActivity(i);
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Cursor c = helper.selectItem();
                        c.moveToPosition(posisi);
                        final String SEGMENT1 = c.getString(c.getColumnIndexOrThrow("SEGMENT1"));
                        final String DESCRIPTION = c.getString(c.getColumnIndexOrThrow("DESCRIPTION"));
                        final String REQUIRED_QUANTITY = c.getString(c.getColumnIndexOrThrow("REQUIRED_QUANTITY"));
                        final String ALLOCATED_QUANTITY = c.getString(c.getColumnIndexOrThrow("ALLOCATED_QUANTITY"));
                        final String QUANTITY_DETAILED = c.getString(c.getColumnIndexOrThrow("QUANTITY_DETAILED"));
                        final String STATUS = c.getString(c.getColumnIndexOrThrow("STATUS"));

                        if(STATUS.equals("V")){
                            Log.e("CEK","STATUS UDAH V GA UBAH QTY");
                            sDialog.cancel();
                        } else {
                            Log.e("CEK","STATUS BELUM V GA UBAH QTY");
                            Intent i = new Intent(getApplicationContext(), VerifikasiAllocateActivity.class);
                            i.putExtra("HEADER_ID", HEADER_ID);
                            i.putExtra("REQUEST_NUMBER", REQUEST_NUMBER);
                            i.putExtra("SEGMENT1", SEGMENT1);
                            i.putExtra("DESCRIPTION", DESCRIPTION);
                            i.putExtra("REQUIRED_QUANTITY", REQUIRED_QUANTITY);
                            i.putExtra("ALLOCATED_QUANTITY", ALLOCATED_QUANTITY);
                            i.putExtra("QUANTITY_DETAILED", QUANTITY_DETAILED);
                            i.putExtra("POSITION", posisi);
                            i.putExtra("FLAG_QTY", "N");
                            startActivity(i);
                            sDialog.cancel();
                        }
                    }
                })
                .show();
    }

//    void goLoadUpdateNonSerial(){
//        loadingData = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//        loadingData.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//        loadingData.setTitleText("\nLoading Data. . .");
//        loadingData.setCancelable(false);
//        loadingData.show();
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateStatusKdd();
//                commit();
//            }
//        }, 1000);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateQtyKddsp();
//                commit();
//            }
//        }, 3000);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("CEK","FINISH");
//                loadingData.dismissWithAnimation();
//                alertSuccess();
//            }
//        }, 5000);
//    }
//
//    public void updateStatusKdd() {
//        Statement stmt = null;
//        Connection conn = null;
//
//        Cursor c = helper.selectUpdateStatus(); //sesuai checkbox
//
//        while (c.moveToNext()) {
//            String request = c.getString(0);
//            String item_id = c.getString(1);
//            String qty = c.getString(2);
//            String line_num = c.getString(3);
//
//            try {
//
//                conn = mConn;
//                stmt = conn.createStatement();
//
//                System.out
//                        .println("UPDATE khs_detail_dospb_sp\n" +
//                                "SET STATUS = 'V'\n" +
//                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
//                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
//                                "AND LINE_NUMBER = '" + line_num + "'");
//
//                stmt.executeUpdate("UPDATE khs_detail_dospb_sp\n" +
//                                "SET STATUS = 'V'\n" +
//                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
//                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
//                                "AND LINE_NUMBER = '" + line_num + "'");
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//
//            }
//        }
//    }
//
//    public void updateQtyKddsp() {
//        Statement stmt = null;
//        Connection conn = null;
//
//        Cursor c = helper.selectUpdateQty(); //sesuai checkbox
//
//        while (c.moveToNext()) {
//            String request = c.getString(0);
//            String item_id = c.getString(1);
//            String qty = c.getString(2);
//            String line_num = c.getString(3);
//
//            try {
//
//                conn = mConn;
//                stmt = conn.createStatement();
//
//                System.out
//                        .println("UPDATE khs_detail_dospb_sp\n" +
//                                "SET ALLOCATED_QUANTITY = '"+qty+"'\n" +
//                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
//                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
//                                "AND LINE_NUMBER = '" + line_num + "'");
//
//                stmt.executeUpdate("UPDATE khs_detail_dospb_sp\n" +
//                                "SET ALLOCATED_QUANTITY = '"+qty+"'\n" +
//                                "WHERE REQUEST_NUMBER = '" + request + "' \n" +
//                                "AND INVENTORY_ITEM_ID = '" + item_id + "'\n" +
//                                "AND LINE_NUMBER = '" + line_num + "'");
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//
//            }
//        }
//    }
//
//    void commit() {
//        try {
//            Statement statement = mConn.createStatement();
//            mQuery = "COMMIT";
//            statement.executeUpdate(mQuery);
//            Log.d("proses", "Commit " + mQuery);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void alertSuccess(){
//        loadingData = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
//                .setTitleText("Success")
//                .setContentText("Proses allocate berhasil!")
//                .setConfirmText("OK")
//                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sDialog) {
//                        next.setVisibility(View.VISIBLE);
//                        Intent i = new Intent(getApplicationContext(), DOAllocateActivity.class);
//                        startActivity(i);
//                        sDialog.dismissWithAnimation();
//                    }
//                });
//        loadingData.show();
//        loadingData.setCanceledOnTouchOutside(false);
//    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, DOAllocateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        finish();
    }
}
