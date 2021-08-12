package com.quick.dospbsparepart;

//activity peritem (menu allocate)
public class DataRow {
    String M_REQUEST_NUMBER;
    String M_HEADER_ID;
    String M_LINE_NUMBER;
    String M_SEGMENT1;
    String M_INVENTORY_ITEM_ID;
    String M_DESCRIPTION;
    String M_LOKASI_SIMPAN;
    String M_REQUIRED_QUANTITY;
    String M_ALLOCATED_QUANTITY;
    String M_QUANTITY_DETAILED;
    String M_STATUS;
    String M_FLAG;
    String _id;

    void setData(String REQUEST_NUMBER,
                 String HEADER_ID,
                 String LINE_NUMBER,
                 String SEGMENT1,
                 String INVENTORY_ITEM_ID,
                 String DESCRIPTION,
                 String LOKASI_SIMPAN,
                 String REQUIRED_QUANTITY,
                 String ALLOCATED_QUANTITY,
                 String QUANTITY_DETAILED,
                 String STATUS,
                 String FLAG,
                 String id) {
        M_REQUEST_NUMBER = REQUEST_NUMBER;
        M_HEADER_ID = HEADER_ID;
        M_LINE_NUMBER = LINE_NUMBER;
        M_SEGMENT1 = SEGMENT1;
        M_INVENTORY_ITEM_ID = INVENTORY_ITEM_ID;
        M_DESCRIPTION = DESCRIPTION;
        M_LOKASI_SIMPAN = LOKASI_SIMPAN;
        M_REQUIRED_QUANTITY = REQUIRED_QUANTITY;
        M_ALLOCATED_QUANTITY = ALLOCATED_QUANTITY;
        M_QUANTITY_DETAILED = QUANTITY_DETAILED;
        M_STATUS = STATUS;
        M_FLAG = FLAG;
        _id = id;
    }

    public String getFlag() {
        return M_FLAG;
    }

    void setFlag(String flag) {
        M_FLAG = flag;
    }
}
