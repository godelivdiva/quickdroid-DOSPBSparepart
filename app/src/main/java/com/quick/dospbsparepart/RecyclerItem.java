package com.quick.dospbsparepart;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class RecyclerItem extends RecyclerView.Adapter<RecyclerItem.ViewHplderItem> {
    ArrayList<DataRow> mDataRow;
    Context mContext;
    ItemClickListener mItemClickListen;
    dbHelp helper;

    public RecyclerItem(Context context, ArrayList<DataRow> dataRow, ItemClickListener listener) {
        mContext = context;
        mItemClickListen = listener;
        mDataRow = dataRow;
        helper = new dbHelp(mContext);
    }

    @Override
    public int getItemCount() {
        return mDataRow.size();
    }

    @Override
    public ViewHplderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_list_item, parent, false);
        return new ViewHplderItem(view);
    }

    @Override
    public void onBindViewHolder(final ViewHplderItem holder, final int position) {
        final DataRow dataRow = mDataRow.get(position);
//        holder.tv_Urut.setText(dataRow.M_LINE_NUMBER);
        holder.tv_Urut.setText("" + (position + 1));
        holder.tv_Item.setText(dataRow.M_SEGMENT1);
        holder.tv_Desc.setText(dataRow.M_DESCRIPTION);
        holder.tv_Loc.setText(dataRow.M_LOKASI_SIMPAN);
        holder.tv_ReqQty.setText(dataRow.M_REQUIRED_QUANTITY);
        holder.tv_Allocate.setText(dataRow.M_ALLOCATED_QUANTITY);

        holder.tv_qtyedit.setText(dataRow.M_QUANTITY_DETAILED);

        holder.cb_pilih.setOnCheckedChangeListener(null);

        if (mDataRow.get(position).M_FLAG.equals("Y")) {
            holder.cb_pilih.setChecked(true);
            mDataRow.get(position).setFlag("Y");
        } else {
            holder.cb_pilih.setChecked(false);
            mDataRow.get(position).setFlag("N");
        }

        if (mDataRow.get(position).M_STATUS.equals("V")){
            holder.cv_data.setBackgroundColor(Color.parseColor("#a5d6a7"));
//            holder.cv_data.setEnabled(false);
            holder.tv_VerifQty.setText(dataRow.M_ALLOCATED_QUANTITY);
//            holder.cv_data.setEnabled(false);
//            Toast.makeText(mContext, "Sudah Verifikasi",
//                    Toast.LENGTH_LONG).show();
        } else if (mDataRow.get(position).M_STATUS.equals("AV")){
            holder.cv_data.setBackgroundColor(Color.parseColor("#b3e5fc"));
//            holder.cv_data.setEnabled(false);
            holder.tv_VerifQty.setText(dataRow.M_ALLOCATED_QUANTITY);
//            holder.cv_data.setEnabled(true);
        } else {
            holder.cv_data.setBackgroundColor(Color.parseColor("#ffffff"));
//            holder.cv_data.setEnabled(true);
            holder.tv_VerifQty.setText("0");
//            holder.cv_data.setEnabled(true);
        }

        holder.cb_pilih.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    helper.updateFlag("Y", dataRow.M_INVENTORY_ITEM_ID, dataRow.M_LINE_NUMBER);
                    mDataRow.get(position).setFlag("Y");
                    if (mContext instanceof ItemAllocateActivity) {
                        ((ItemAllocateActivity) mContext).getMaxChecked();
                        ((ItemAllocateActivity) mContext).setCountItem();
                        ((ItemAllocateActivity) mContext).showChanged();
                    }
                } else {
                    helper.updateFlag("N", dataRow.M_INVENTORY_ITEM_ID, dataRow.M_LINE_NUMBER);
                    mDataRow.get(position).setFlag("N");
                    if (mContext instanceof ItemAllocateActivity) {
                        ((ItemAllocateActivity) mContext).getMaxChecked();
                        ((ItemAllocateActivity) mContext).setCountItem();
                        ((ItemAllocateActivity) mContext).showChanged();
                    }

                }
            }
        });

//        holder.btn_minus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int posisi = position;
//                int count = Integer.parseInt(String.valueOf(holder.tv_qtyedit.getText()));
//                if (count > 0) {
//                    count--;
//                    holder.tv_qtyedit.setText(String.valueOf(count));
//                    helper.update_qty_detail(holder.tv_qtyedit.getText().toString(), posisi + 1);
//                    ((ItemAllocateActivity) mContext).showChanged();
//                }
//            }
//        });
//
//        holder.btn_plus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int posisi = position;
//                int count = Integer.parseInt(String.valueOf(holder.tv_qtyedit.getText()));
//                int request = Integer.parseInt(dataRow.M_REQUIRED_QUANTITY);
//                if (count >= request) {
//                    Toast.makeText(v.getContext(), "qty allocate tidak boleh lebih dari request", Toast.LENGTH_SHORT).show();
//                } else {
//                    count++;
//                    holder.tv_qtyedit.setText(String.valueOf(count));
//                    helper.update_qty_detail(holder.tv_qtyedit.getText().toString(), posisi + 1);
//                    ((ItemAllocateActivity) mContext).showChanged();
//                }
//            }
//        });

    }

    interface ItemClickListener {
        void OnItemClick(int position, View itemView);
    }

    class ViewHplderItem extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {
        CardView cv_data;
        CheckBox cb_pilih;
        TextView tv_Urut;
        TextView tv_Item;
        TextView tv_Desc;
        TextView tv_Loc;
        TextView tv_ReqQty;
        TextView tv_Allocate;
        TextView tv_VerifQty;

        ImageButton btn_plus;
        ImageButton btn_minus;
        TextView tv_qtyedit;


        public ViewHplderItem(View view) {
            super(view);
            cv_data = (CardView) view.findViewById(R.id.cv_data);
            cb_pilih = (CheckBox) view.findViewById(R.id.cb_pilih);
            tv_Urut = (TextView) view.findViewById(R.id.tv_urut);
            tv_Item = (TextView) view.findViewById(R.id.tv_Item);
            tv_Desc = (TextView) view.findViewById(R.id.tv_Desc);
            tv_Loc = (TextView) view.findViewById(R.id.tv_Loc);
            tv_ReqQty = (TextView) view.findViewById(R.id.tv_ReqQty);
            tv_Allocate = (TextView) view.findViewById(R.id.tv_Allocate);
            tv_VerifQty = (TextView) view.findViewById(R.id.tv_VerifQty);

            btn_plus = (ImageButton) view.findViewById(R.id.btn_plus);
            btn_minus = (ImageButton) view.findViewById(R.id.btn_minus);
            tv_qtyedit = (TextView) view.findViewById(R.id.tv_qtyedit);

            cv_data.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListen.OnItemClick(getAdapterPosition(), v);
        }
    }
}
