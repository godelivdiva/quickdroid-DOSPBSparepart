package com.quick.dospbsparepart;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import java.sql.Connection;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CursorAdapterBagi extends android.widget.CursorAdapter {
    int urutan = 1;
    Connection mCon;
    dbHelp helper;
    Context mContext;
    Connection mConPostgre;
    ManagerSessionUserOracle session;
    SweetAlertDialog loadingData;
    AlertDialog.Builder bApi;
    AlertDialog aApi;

    public CursorAdapterBagi(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
        mCon = new ManagerSessionUserOracle(context).connectDb();
        helper = new dbHelp(mContext);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //Inflate view
        return LayoutInflater.from(context).inflate(R.layout.row_list_bagi, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        //set data
        CardView linear = (CardView) view.findViewById(R.id.cv_list);
        TextView tv_urut = (TextView) view.findViewById(R.id.tv_nomor);
        final TextView tv_HeadColly = (TextView) view.findViewById(R.id.tv_HeadColly);
        ImageButton ib_delete = (ImageButton) view.findViewById(R.id.ib_delete);

        final String NOMOR_COLLY = cursor.getString(cursor.getColumnIndexOrThrow("NOMOR_COLLY"));
        final String AUTO = cursor.getString(cursor.getColumnIndexOrThrow("AUTO"));

        if(AUTO.equals("Y")){
            linear.setEnabled(true); //false
            linear.setBackgroundColor(Color.parseColor("#f0f0f0"));
            ib_delete.setEnabled(false);
        } else {
            linear.setEnabled(true);
            linear.setBackgroundColor(Color.parseColor("#ffffff"));
            ib_delete.setEnabled(true);
        }

        tv_urut.setText("" + (cursor.getPosition() + 1));
        tv_HeadColly.setText(NOMOR_COLLY);

        ib_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof BagiCollyActivity) {
                    if(AUTO.equals("Y")){
                        Toast.makeText(mContext, "Packing otomatis tidak bisa dihapus", Toast.LENGTH_SHORT).show();
                    } else {
                        ((BagiCollyActivity) mContext).loadDelete();

                        Log.e("CEK NOMOR_COLLY", tv_HeadColly.getText().toString());
                        helper.deleteHeadByColly(tv_HeadColly.getText().toString());

                        ((BagiCollyActivity) mContext).deletePrc(NOMOR_COLLY);
                        ((BagiCollyActivity) mContext).commit();
                        ((BagiCollyActivity) mContext).refreshList();
                    }
                }
            }
        });

        linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof BagiCollyActivity) {
                    ((BagiCollyActivity) mContext).LoadingDialog(tv_HeadColly.getText().toString());
                }
            }
        });
    }
}
