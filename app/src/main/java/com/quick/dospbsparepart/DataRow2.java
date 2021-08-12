package com.quick.dospbsparepart;

//activity peritem (menu allocate)
public class DataRow2 {
    String C_REQUEST_NUMBER;
    String C_HEADER_ID;
    String C_LINE_ID;
    String C_LINE_NUMBER;
    String C_SEGMENT1;
    String C_INVENTORY_ITEM_ID;
    String C_DESCRIPTION;
    String C_LOKASI_SIMPAN;
    String C_REQUIRED_QUANTITY;
    String C_ALLOCATED_QUANTITY;
    String C_QUANTITY_DETAILED;
    String C_QTY_PACKING;
    String C_QTY_INPUT;
    String C_QTY_READY;
    String C_COLLY_FLAG;
    String C_ITEM_FLAG;
    String C_NOMOR_COLLY;
    String C_FLAG;
//    String _id;

    void setData(String REQUEST_NUMBER,
                 String HEADER_ID,
                 String LINE_ID,
                 String LINE_NUMBER,
                 String SEGMENT1,
                 String INVENTORY_ITEM_ID,
                 String DESCRIPTION,
                 String LOKASI_SIMPAN,
                 String REQUIRED_QUANTITY,
                 String ALLOCATED_QUANTITY,
                 String QUANTITY_DETAILED,
                 String QTY_PACKING,
                 String QTY_INPUT,
                 String QTY_READY,
                 String COLLY_FLAG,
                 String ITEM_FLAG,
                 String NOMOR_COLLY,
                 String FLAG
//                 String id
    ) {
        C_REQUEST_NUMBER = REQUEST_NUMBER;
        C_HEADER_ID = HEADER_ID;
        C_LINE_ID = LINE_ID;
        C_LINE_NUMBER = LINE_NUMBER;
        C_SEGMENT1 = SEGMENT1;
        C_INVENTORY_ITEM_ID = INVENTORY_ITEM_ID;
        C_DESCRIPTION = DESCRIPTION;
        C_LOKASI_SIMPAN = LOKASI_SIMPAN;
        C_REQUIRED_QUANTITY = REQUIRED_QUANTITY;
        C_ALLOCATED_QUANTITY = ALLOCATED_QUANTITY;
        C_QUANTITY_DETAILED = QUANTITY_DETAILED;
        C_QTY_PACKING = QTY_PACKING;
        C_QTY_INPUT = QTY_INPUT;
        C_QTY_READY = QTY_READY;
        C_COLLY_FLAG = COLLY_FLAG;
        C_ITEM_FLAG = ITEM_FLAG;
        C_NOMOR_COLLY = NOMOR_COLLY;
        C_FLAG = FLAG;
//        _id = id;
    }

    public String getFlag() {
        return C_FLAG;
    }

    void setFlag(String flag) {
        C_FLAG = flag;
    }
}
