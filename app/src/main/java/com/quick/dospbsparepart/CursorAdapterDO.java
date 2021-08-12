package com.quick.dospbsparepart;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.sql.Connection;

public class CursorAdapterDO extends android.widget.CursorAdapter {
    int urutan = 1;
    Connection mCon;
    dbHelp helper;
    Connection mConPostgre;
    ManagerSessionUserOracle session;

    public CursorAdapterDO(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mCon = new ManagerSessionUserOracle(context).connectDb();

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //Inflate view
        return LayoutInflater.from(context).inflate(R.layout.row_list_do, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //set data
        CardView linear = (CardView) view.findViewById(R.id.cv_list);
        TextView tv_urut = (TextView) view.findViewById(R.id.tv_nomor);
        TextView tv_DO = (TextView) view.findViewById(R.id.tv_DO);

        String REQUEST_NUMBER = cursor.getString(cursor.getColumnIndexOrThrow("REQUEST_NUMBER"));
        String NOT_VERIFIKASI = cursor.getString(cursor.getColumnIndexOrThrow("NOT_VERIFIKASI"));

        tv_urut.setText("" + (cursor.getPosition() + 1));
        tv_DO.setText(REQUEST_NUMBER);

        if (NOT_VERIFIKASI.equals("0")) {
            linear.setBackgroundColor(Color.parseColor("#a5d6a7"));
        } else {
            linear.setBackgroundColor(Color.parseColor("#ffffff"));
        }
    }
}
