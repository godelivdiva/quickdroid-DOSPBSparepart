package com.quick.dospbsparepart;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

class RecyclerItem2 extends RecyclerView.Adapter<RecyclerItem2.ViewHplderItem> {
    ArrayList<DataRow2> mDataRow;
    Context mContext;
    ManagerSessionUserOracle session;
    Connection mConn;
    ItemClickListener mItemClickListen;
    dbHelp helper;

    public RecyclerItem2(Context context, ArrayList<DataRow2> dataRow, ItemClickListener listener) {
        mContext = context;
        mItemClickListen = listener;
        mDataRow = dataRow;
        helper = new dbHelp(mContext);
        session = new ManagerSessionUserOracle(mContext);
        mConn = session.connectDb();
    }

    @Override
    public int getItemCount() {
        return mDataRow.size();
    }

    @Override
    public ViewHplderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_list_item_colly, parent, false);
        return new ViewHplderItem(view);
    }

    @Override
    public void onBindViewHolder(final ViewHplderItem holder, final int position) {
        final DataRow2 dataRow = mDataRow.get(position);
        holder.tv_urut.setText(dataRow.C_LINE_NUMBER);
        holder.tv_Item.setText(dataRow.C_SEGMENT1);
        holder.tv_Desc.setText(dataRow.C_DESCRIPTION);
        holder.tv_Loc.setText(dataRow.C_LOKASI_SIMPAN);
        holder.tv_ReqQty.setText(dataRow.C_REQUIRED_QUANTITY);
        holder.tv_Allocate.setText(dataRow.C_QUANTITY_DETAILED);


        if(isAuto(dataRow.C_NOMOR_COLLY, dataRow.C_INVENTORY_ITEM_ID)){
            holder.ll_qtypacking.setVisibility(View.GONE);
            holder.tv_InQty.setText(dataRow.C_QTY_INPUT);
        } else {
            holder.ll_qtypacking.setVisibility(View.VISIBLE);
            holder.tv_PackQty.setText(dataRow.C_QTY_PACKING); //count qty pack per item
            if(isAnyPackInThisColly(dataRow.C_NOMOR_COLLY, dataRow.C_INVENTORY_ITEM_ID)){
                holder.tv_InQty.setText(dataRow.C_QTY_INPUT);
            } else {
                holder.tv_InQty.setText("0");
            }
        }

        holder.tv_Done.setText(dataRow.C_ALLOCATED_QUANTITY);

//        holder.tv_qtyedit.setText(dataRow.C_QUANTITY_DETAILED);
        holder.tv_qtyedit.setText(dataRow.C_QUANTITY_DETAILED);

//        holder.et_qtyedit.setSelection(holder.et_qtyedit.getText().length());

        holder.cb_pilih.setOnCheckedChangeListener(null);

        if(isAuto(dataRow.C_NOMOR_COLLY, dataRow.C_INVENTORY_ITEM_ID)){
//            holder.cb_pilih.setEnabled(false);
            holder.ll_qtyinput.setVisibility(View.GONE);
            Log.e("CEK" ,"AUTO");
        } else {
//            holder.cb_pilih.setEnabled(true);
            holder.ll_qtyinput.setVisibility(View.VISIBLE);
        }

        if (mDataRow.get(position).C_COLLY_FLAG.equals("F")) {
            holder.cb_pilih.setEnabled(false);
            holder.cv_data.setBackgroundColor(Color.parseColor("#a5d6a7"));
        } else {
            holder.cv_data.setBackgroundColor(Color.parseColor("#ffffff"));
            if(isAuto(dataRow.C_NOMOR_COLLY, dataRow.C_INVENTORY_ITEM_ID)){
                holder.cb_pilih.setEnabled(false);
//                holder.ll_qtyinput.setVisibility(View.GONE);
                Log.e("CEK" ,"AUTO");
            } else {
                holder.cb_pilih.setEnabled(true);
//                holder.ll_qtyinput.setVisibility(View.VISIBLE);
            }
        }

        if (mDataRow.get(position).C_FLAG.equals("Y")) {
            holder.cb_pilih.setChecked(true);
            mDataRow.get(position).setFlag("Y");
        } else {
            holder.cb_pilih.setChecked(false);
            mDataRow.get(position).setFlag("N");
        }

        holder.cb_pilih.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    helper.updateFlag2("Y", dataRow.C_INVENTORY_ITEM_ID, dataRow.C_LINE_NUMBER, dataRow.C_NOMOR_COLLY);
                    mDataRow.get(position).setFlag("Y");
                    if (mContext instanceof ItemCollyActivity) {
//                        ((ItemCollyActivity) mContext).getMaxChecked();
                        ((ItemCollyActivity) mContext).setCountItem();
                        ((ItemCollyActivity) mContext).showChanged();
                    }
                } else {
                    helper.updateFlag2("N", dataRow.C_INVENTORY_ITEM_ID, dataRow.C_LINE_NUMBER, dataRow.C_NOMOR_COLLY);
                    mDataRow.get(position).setFlag("N");
                    if (mContext instanceof ItemCollyActivity) {
//                        ((ItemCollyActivity) mContext).getMaxChecked();
                        ((ItemCollyActivity) mContext).setCountItem();
                        ((ItemCollyActivity) mContext).showChanged();
                    }

                }
            }
        });

        holder.btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posisi = position;
                int count = Integer.parseInt(String.valueOf(holder.tv_qtyedit.getText()));
                if (count > 0) {
                    count--;
                    holder.tv_qtyedit.setText(String.valueOf(count));
                    helper.update_qty_detail2(holder.tv_qtyedit.getText().toString(), dataRow.C_LINE_NUMBER, dataRow.C_NOMOR_COLLY);
                    ((ItemCollyActivity) mContext).showChanged();
                }
            }
        });

        holder.btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posisi = position;
                int count = Integer.parseInt(String.valueOf(holder.tv_qtyedit.getText()));
                int request = Integer.parseInt(dataRow.C_QTY_READY);
                if (count >= request) {
                    Toast.makeText(v.getContext(), "qty tidak boleh lebih dari Allocated", Toast.LENGTH_SHORT).show();
                } else {
                    count++;
                    holder.tv_qtyedit.setText(String.valueOf(count));
                    helper.update_qty_detail2(holder.tv_qtyedit.getText().toString(), dataRow.C_LINE_NUMBER, dataRow.C_NOMOR_COLLY);
                    ((ItemCollyActivity) mContext).showChanged();
                }
            }
        });

        holder.btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posisi = position;
//                helper.update_flag_colly("N", dataRow.C_LINE_NUMBER, dataRow.C_NOMOR_COLLY);
//                ((ItemCollyActivity) mContext).showChanged();
            }
        });
    }

    public boolean isAuto(String colly, String item_id) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM KHS_COLLY_TAMPUNG kct \n" +
                    "            WHERE kct.COLLY_NUMBER = '"+colly+"'\n" +
                    "            AND kct.AUTO = 'Y'\n" +
                    "            --AND kct.ITEM_ID = "+item_id+"";
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


    public boolean isAnyPackInThisColly(String colly, String item_id) {
        Integer ver = 0;
        ResultSet theResultSet;
        try {
            Statement statement = mConn.createStatement();
            String mQuery = "SELECT count(*) FROM KHS_COLLY_TAMPUNG kct \n" +
                    "            WHERE kct.COLLY_NUMBER = '"+colly+"'\n" +
                    "            AND kct.ITEM_ID = "+item_id+"";
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

    interface ItemClickListener {
        void OnItemClick(int position, View itemView);
    }

    class ViewHplderItem extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {
        CardView cv_data;
        CheckBox cb_pilih;
        TextView tv_urut;
        TextView tv_Item;
        TextView tv_Desc;
        TextView tv_Loc;
        TextView tv_ReqQty;
        TextView tv_Done;
        TextView tv_Allocate;
        TextView tv_PackQty;

        TextView tv_InQty;

        ImageButton btn_plus;
        ImageButton btn_minus;
        ImageButton btn_remove;
        TextView tv_qtyedit;
//        EditText et_qtyedit;

        LinearLayout ll_qtyinput, ll_qtypacking;

        public ViewHplderItem(View view) {
            super(view);
            cv_data = (CardView) view.findViewById(R.id.cv_data);
            cb_pilih = (CheckBox) view.findViewById(R.id.cb_pilih);
            tv_urut = (TextView) view.findViewById(R.id.tv_urut);
            tv_Item = (TextView) view.findViewById(R.id.tv_Item);
            tv_Desc = (TextView) view.findViewById(R.id.tv_Desc);
            tv_Loc = (TextView) view.findViewById(R.id.tv_Loc);
            tv_ReqQty = (TextView) view.findViewById(R.id.tv_ReqQty);
            tv_Allocate = (TextView) view.findViewById(R.id.tv_Allocate);
            tv_Done = (TextView) view.findViewById(R.id.tv_qtysudahallocate);
            tv_PackQty = (TextView) view.findViewById(R.id.tv_PackQty);

            tv_InQty = (TextView) view.findViewById(R.id.tv_InQty);

            btn_plus = (ImageButton) view.findViewById(R.id.btn_plus);
            btn_minus = (ImageButton) view.findViewById(R.id.btn_minus);
            btn_remove = (ImageButton) view.findViewById(R.id.btn_remove);
            tv_qtyedit = (TextView) view.findViewById(R.id.tv_qtyedit);
//            et_qtyedit = (EditText) view.findViewById(R.id.et_qtyedit);

            ll_qtyinput = (LinearLayout) view.findViewById(R.id.ll_qtyinput);
            ll_qtypacking = (LinearLayout) view.findViewById(R.id.ll_qtypacking);

            cv_data.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListen.OnItemClick(getAdapterPosition(), v);
        }
    }
}
