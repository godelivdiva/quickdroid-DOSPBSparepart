package com.quick.dospbsparepart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class dbHelp extends SQLiteOpenHelper {

    static final String TABLE_USER = "TABLE_USER";
    final String user_id = "user_id";
    final String user = "user";
    final String employee = "employee";
    final String kode_sie = "kode_sie";
    final String lokasi = "lokasi";

    static final String TABLE_DO = "tb_do";
    public final String _id	= "_id";
    public final String DO_HEADER_ID  = "HEADER_ID";
    public final String DO_REQUEST_NUMBER  = "REQUEST_NUMBER";
    public final String DO_ASSIGNEE_ID  = "ASSIGNEE_ID";
    public final String DO_NOT_VERIFIKASI  = "NOT_VERIFIKASI";


    static final String TABLE_ITEM = "tb_item";
    public final String _id3 = "_id";
    public final String REQUEST_NUMBER = "REQUEST_NUMBER";
    public final String HEADER_ID = "HEADER_ID";
    public final String LINE_NUMBER = "LINE_NUMBER";
    public final String LINE_ID = "LINE_ID";
    public final String SEGMENT1 = "SEGMENT1";
    public final String INVENTORY_ITEM_ID = "INVENTORY_ITEM_ID";
    public final String DESCRIPTION = "DESCRIPTION";
    public final String LOKASI_SIMPAN = "LOKASI_SIMPAN";
    public final String REQUIRED_QUANTITY = "REQUIRED_QUANTITY";
    public final String ALLOCATED_QUANTITY = "ALLOCATED_QUANTITY";
    public final String QUANTITY_DETAILED = "QUANTITY_DETAILED";
    public final String STATUS = "STATUS";
    public final String STD_PACK = "STD_PACK";
    public final String JUMLAH_BAGI = "JUMLAH_BAGI";
    public final String FLAG = "FLAG";

    static final String TABLE_HEADER_COLLY = "tb_header_colly";
    public final String _id4 = "_id";
    public final String NOMOR_COLLY = "NOMOR_COLLY";
    public final String AUTO = "AUTO";

    static final String TABLE_ITEM_COLLY = "tb_item_colly";
    public final String _id5 = "_id";
    public final String COLLY_HEADER_ID = "HEADER_ID";
    public final String COLLY_LINE_ID = "LINE_ID";
    public final String COLLY_REQUEST_NUMBER = "REQUEST_NUMBER";
    public final String COLLY_ORGANIZATION_ID = "ORGANIZATION_ID";
    public final String COLLY_SEGMENT1 = "SEGMENT1";
    public final String COLLY_INVENTORY_ITEM_ID = "INVENTORY_ITEM_ID";
    public final String COLLY_DESCRIPTION = "DESCRIPTION";
    public final String COLLY_LOKASI_SIMPAN = "LOKASI_SIMPAN";
    public final String COLLY_LINE_NUMBER = "LINE_NUMBER";
    public final String COLLY_REQUIRED_QUANTITY = "REQUIRED_QUANTITY";
    public final String COLLY_ALLOCATED_QUANTITY = "ALLOCATED_QUANTITY";
    public final String COLLY_STATUS = "STATUS";
    public final String COLLY_COLLY_NUMBER = "COLLY_NUMBER";
    public final String COLLY_QTY_PACKING = "QTY_PACKING";
    public final String COLLY_QTY_INPUT = "QTY_INPUT";
    public final String COLLY_QTY_READY = "QTY_READY";
    public final String COLLY_QUANTITY_DETAILED = "QUANTITY_DETAILED";
    public final String COLLY_COLLY_FLAG = "COLLY_FLAG";
    public final String COLLY_ITEM_FLAG = "ITEM_FLAG";
    public final String COLLY_NOMOR_COLLY = "NOMOR_COLLY";
    public final String COLLY_FLAG = "FLAG";

    static final String TABLE_DO_TRANSACT = "tb_do_transact";
    public final String _id6 = "_id";
    public final String DOTR_REQUEST_NUMBER = "REQUEST_NUMBER";
    public final String DOTR_HEADER_ID = "HEADER_ID";
    public final String DOTR_PIC_PACKING = "PIC_PACKING";
    public final String DOTR_JUMLAH_ITEM = "JUMLAH_ITEM";
    public final String DOTR_JUMLAH_PCS = "JUMLAH_PCS";
    public final String DOTR_JUMLAH_ALLOCATE = "JUMLAH_ALLOCATE";
    public final String DOTR_JUMLAH_COLLY = "JUMLAH_COLLY";

    static final String TABLE_COLLY_TRANSACT = "tb_colly_transact";
    public final String _id7 = "_id";
    public final String CT_COLLY_NUMBER = "COLLY_NUMBER";

    static final String TABLE_ITEM_TRANSACT = "tb_item_transact";
    public final String _id8 = "_id";
    public final String TR_REQUEST_NUMBER = "REQUEST_NUMBER";
    public final String TR_LINE_ID = "LINE_ID";
    public final String TR_LINE_NUMBER = "LINE_NUMBER";
    public final String TR_COLLY_NUMBER = "COLLY_NUMBER";
    public final String TR_ORGANIZATION_ID = "ORGANIZATION_ID";
    public final String TR_INVENTORY_ITEM_ID = "INVENTORY_ITEM_ID";
    public final String TR_SEGMENT1 = "SEGMENT1";
    public final String TR_DESCRIPTION = "DESCRIPTION";
    public final String TR_QUANTITY = "QUANTITY";
    public final String TR_CREATION_DATE = "CREATION_DATE";
    public final String TR_CREATED_BY = "CREATED_BY";
    public final String TR_VERIF_FLAG = "VERIF_FLAG";
    public final String TR_FLAG = "FLAG";

   String mQuery,mQuery2,mQuery3,mQuery4,mQuery5,mQuery6, mQuery7, mQuery8;

    public dbHelp(Context context) {
        super(context, "db_data", null, 5);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //create table

        mQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                "_id" + " INTEGER PRIMARY KEY," +
                user_id + " TEXT," +
                user + " TEXT," +
                employee + " TEXT," +
                kode_sie + " TEXT," +
                lokasi + " TEXT" +
                ")";
        db.execSQL(mQuery);

        mQuery2 = "CREATE TABLE " + TABLE_DO + " (" +
                _id + " INTEGER PRIMARY KEY," +
                DO_HEADER_ID + " TEXT," +
                DO_REQUEST_NUMBER + " TEXT," +
                DO_ASSIGNEE_ID + " TEXT," +
                DO_NOT_VERIFIKASI + " TEXT" +
                ")";
        db.execSQL(mQuery2);

        mQuery3 = "CREATE TABLE "+TABLE_ITEM+" (" +
                _id +" INTEGER PRIMARY KEY," +
                REQUEST_NUMBER + " TEXT,"+
                HEADER_ID + " TEXT,"+
                LINE_NUMBER + " TEXT,"+
                LINE_ID + " TEXT,"+
                SEGMENT1 + " TEXT,"+
                INVENTORY_ITEM_ID + " TEXT,"+
                DESCRIPTION + " TEXT,"+
                LOKASI_SIMPAN + " TEXT,"+
                REQUIRED_QUANTITY + " TEXT,"+
                ALLOCATED_QUANTITY + " TEXT,"+
                QUANTITY_DETAILED + " TEXT,"+
                STATUS + " TEXT,"+
                STD_PACK + " TEXT,"+
                JUMLAH_BAGI + " TEXT,"+
                FLAG + " TEXT"+
                ")";
        db.execSQL(mQuery3);

        mQuery4 = "CREATE TABLE " + TABLE_HEADER_COLLY + " (" +
                _id + " INTEGER PRIMARY KEY," +
                NOMOR_COLLY + " TEXT," +
                AUTO + " TEXT" +
                ")";
        db.execSQL(mQuery4);

        mQuery5 = "CREATE TABLE "+TABLE_ITEM_COLLY+" (" +
//                _id +" INTEGER PRIMARY KEY," +
                COLLY_HEADER_ID + " TEXT,"+
                COLLY_LINE_ID + " TEXT,"+
                COLLY_REQUEST_NUMBER + " TEXT,"+
                COLLY_SEGMENT1 + " TEXT,"+
                COLLY_INVENTORY_ITEM_ID	+ " TEXT,"+
                COLLY_DESCRIPTION + " TEXT,"+
                COLLY_LOKASI_SIMPAN + " TEXT,"+
                COLLY_LINE_NUMBER + " TEXT,"+
                COLLY_REQUIRED_QUANTITY	+ " TEXT,"+
                COLLY_ALLOCATED_QUANTITY  + " TEXT,"+
                COLLY_QTY_PACKING + " TEXT,"+
                COLLY_QTY_INPUT + " TEXT,"+
                COLLY_QTY_READY + " TEXT,"+
                COLLY_QUANTITY_DETAILED + " TEXT,"+
                COLLY_COLLY_FLAG + " TEXT,"+
                COLLY_ITEM_FLAG + " TEXT,"+
                COLLY_NOMOR_COLLY + " TEXT,"+
                COLLY_FLAG + " TEXT"+
                ")";
        db.execSQL(mQuery5);

        mQuery6 = "CREATE TABLE IF NOT EXISTS " + TABLE_DO_TRANSACT + " (" +
                "_id" + " INTEGER PRIMARY KEY," +
                DOTR_REQUEST_NUMBER + " TEXT," +
                DOTR_HEADER_ID + " TEXT," +
                DOTR_PIC_PACKING + " TEXT," +
                DOTR_JUMLAH_ITEM + " TEXT," +
                DOTR_JUMLAH_PCS + " TEXT," +
                DOTR_JUMLAH_ALLOCATE + " TEXT," +
                DOTR_JUMLAH_COLLY + " TEXT" +
                ")";
        db.execSQL(mQuery6);

        mQuery7 = "CREATE TABLE " + TABLE_COLLY_TRANSACT + " (" +
                _id + " INTEGER PRIMARY KEY," +
                CT_COLLY_NUMBER + " TEXT" +
                ")";
        db.execSQL(mQuery7);

        mQuery8 = "CREATE TABLE "+TABLE_ITEM_TRANSACT+" (" +
                _id +" INTEGER PRIMARY KEY," +
                TR_REQUEST_NUMBER + " TEXT,"+
                TR_LINE_ID + " TEXT,"+
                TR_LINE_NUMBER + " TEXT,"+
                TR_COLLY_NUMBER + " TEXT,"+
                TR_ORGANIZATION_ID + " TEXT,"+
                TR_INVENTORY_ITEM_ID + " TEXT,"+
                TR_SEGMENT1 + " TEXT,"+
                TR_DESCRIPTION + " TEXT,"+
                TR_QUANTITY + " TEXT,"+
                TR_CREATION_DATE + " TEXT,"+
                TR_CREATED_BY + " TEXT,"+
                TR_VERIF_FLAG + " TEXT,"+
                TR_FLAG + " TEXT"+
                ")";
        db.execSQL(mQuery8);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("NEW", "" + newVersion);
        Log.d("OLD", "" + oldVersion);
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE " + TABLE_USER);
            onCreate(db);
            db.execSQL("DROP TABLE " + TABLE_DO);
            onCreate(db);
            db.execSQL("DROP TABLE " + TABLE_ITEM);
            onCreate(db);
            db.execSQL("DROP TABLE " + TABLE_HEADER_COLLY);
            onCreate(db);
            db.execSQL("DROP TABLE " + TABLE_ITEM_COLLY);
            onCreate(db);
            db.execSQL("DROP TABLE " + TABLE_DO_TRANSACT);
            onCreate(db);
            db.execSQL("DROP TABLE " + TABLE_COLLY_TRANSACT);
            onCreate(db);
            db.execSQL("DROP TABLE " + TABLE_ITEM_TRANSACT);
            onCreate(db);
        }
    }

    //========================================TABLE USER ===========================================
    public void inputUser(String user_id, String user, String employee, String kode_sie,String lokasi){
        SQLiteDatabase db = getWritableDatabase();
        mQuery = "INSERT INTO "+TABLE_USER+" (user_id, user, employee, kode_sie,lokasi)" +
                "VALUES ('"+user_id+"','"+user+"','"+employee+"','"+kode_sie+"','"+lokasi+"')";
        Log.d("INSERT", mQuery);
        db.execSQL(mQuery);
    }

    Cursor selectUser(){
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT * FROM "+TABLE_USER;
        Log.d("Query", mQuery);
        return db.rawQuery(mQuery,null);
    }

    void deleteUser(){
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM "+TABLE_USER;
        Log.d("Query", mQuery);
        db.execSQL(mQuery);
        db.close();
    }

    //======================================TABLE DO ALLOCATE=======================================
    public void insertDO(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("insert", "" + values.toString());
        db.insert("tb_do", null, values);
    }

    public Cursor selectDO() {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT * FROM tb_do";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public void deleteDO() {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_do";
        db.execSQL(mQuery);
    }

    //====================================TABLE ITEM ALLOCATE=======================================
    public void insertItem(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("insert", "" + values.toString());
        db.insert("tb_item", null, values);
    }

    public Cursor selectItem() {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT * FROM tb_item";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public void deleteItem() {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_item";
        db.execSQL(mQuery);
    }

    public int jumlahitemY() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT * FROM tb_item WHERE FLAG = 'Y'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void update_qty_detail(String qty_detailed,int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "UPDATE tb_item\n"
                + "SET QUANTITY_DETAILED='"
                + qty_detailed
                + "' "
                + "WHERE _id="
                + id
                + "";
        Log.d("UPDATE",mQuery);
        db.execSQL(mQuery);
    }


    public void update_jumlah_bagi(String bagi,int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "UPDATE tb_item\n"
                + "SET JUMLAH_BAGI='"
                + bagi
                + "' "
                + "WHERE _id="
                + id
                + "";
        Log.d("UPDATE",mQuery);
        db.execSQL(mQuery);
    }

    void updateFlag(String flag, String itemId, String line_num){
        SQLiteDatabase db = getWritableDatabase();
        mQuery = "UPDATE "+TABLE_ITEM+" " +
                "SET "+ FLAG +" = '"+flag+"' " +
                "WHERE "+INVENTORY_ITEM_ID+" = "+itemId +" " +
                "AND LINE_NUMBER = '"+line_num+"'";
        Log.d("UPDATE", mQuery);
        db.execSQL(mQuery);
        db.close();
    }

    void updateFlagAll(String flag){
        SQLiteDatabase db = getWritableDatabase();
        mQuery = "UPDATE "+TABLE_ITEM+" " +
                "SET "+ FLAG +" = '"+flag+"' ";
        Log.d("UPDATE", mQuery);
        db.execSQL(mQuery);
        db.close();
    }

    public Cursor selectUpdateStatus() {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("Data Serial : " + mQuery);
        mQuery = "SELECT REQUEST_NUMBER, INVENTORY_ITEM_ID, QUANTITY_DETAILED, LINE_NUMBER " +
                "FROM tb_item " +
                "WHERE FLAG = 'Y' "; //tambah line number
        return db.rawQuery(mQuery, null);
    }

    public Cursor selectUpdateStatusV() {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("Data Serial : " + mQuery);
        mQuery = "SELECT REQUEST_NUMBER, INVENTORY_ITEM_ID, QUANTITY_DETAILED, LINE_NUMBER " +
                "FROM tb_item " +
                "WHERE STATUS = 'AV' "; //tambah line number
        return db.rawQuery(mQuery, null);
    }

    public Cursor selectUpdateQty() {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("Data Serial : " + mQuery);
        mQuery = "SELECT REQUEST_NUMBER, INVENTORY_ITEM_ID, QUANTITY_DETAILED, LINE_NUMBER " +
                "FROM tb_item " +
                "WHERE ALLOCATED_QUANTITY <> QUANTITY_DETAILED"; //tambah line number
        return db.rawQuery(mQuery, null);
    }

    //=====================================TABLE HEADER COLLY=======================================
    public void insertHead(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("insert", "" + values.toString());
        db.insert("tb_header_colly", null, values);
    }

    public void inputHead(String no_colly){
        SQLiteDatabase db = getWritableDatabase();
        mQuery = "INSERT INTO "+TABLE_HEADER_COLLY+" (NOMOR_COLLY, AUTO)" +
                "VALUES ('"+no_colly+"', 'N')";
        Log.d("INSERT", mQuery);
        db.execSQL(mQuery);
    }

    public Cursor selectHeaderColly() {
        SQLiteDatabase db = this.getWritableDatabase();
        //grf
//        mQuery = "SELECT hc.* , CAST(trim (substr (hc.NOMOR_COLLY,\n" +
//                "                                            instr (hc.NOMOR_COLLY, '-', 1,\n" +
//                "                                                   1)\n" +
//                "                                          + 1,\n" +
//                "                                            LENGTH (hc.NOMOR_COLLY)\n" +
//                "                                          - instr (hc.NOMOR_COLLY, '-', 1,\n" +
//                "                                                   1)\n" +
//                "                                         )\n" +
//                "                                )\n" +
//                "                            AS INTEGER) FROM tb_header_colly hc\n" +
//                "ORDER BY 2 ASC";
        mQuery = "SELECT * FROM tb_header_colly";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public Cursor selectHeaderCollyByColly(String colly) {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT * FROM tb_header_colly WHERE NOMOR_COLLY = '"+colly+"'";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public String selectHeadLast(int id) {
        String headLast = "";
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT NOMOR_COLLY FROM tb_header_colly";
        Cursor c = db.rawQuery(mQuery, null);
        if(c.moveToLast()){
            headLast = c.getString(0);
        }
        c.close();
        return headLast;
    }

    public void deleteHeaderColly() {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_header_colly";
        db.execSQL(mQuery);
    }

    public void deleteHeadByColly(String coly) {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_header_colly WHERE NOMOR_COLLY = '"+coly+"'";
        db.execSQL(mQuery);
    }

    public int jumlahHead() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT * FROM tb_header_colly";
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void removeHead(int id) {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_header_colly where _id = "+id+"";
        db.execSQL(mQuery);
    }

    //=====================================TABLE ITEM COLLY=========================================
    public void insertItemColly(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("insert", "" + values.toString());
        db.insert("tb_item_colly", null, values);
    }

    public Cursor selectItemColly(String no_colly) {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT DISTINCT * FROM tb_item_colly WHERE NOMOR_COLLY = '"+no_colly+"'";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public void deleteItemColly() {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_item_colly";
        db.execSQL(mQuery);
    }

    public int jumlahItemCollyY() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT * FROM tb_item_colly WHERE FLAG = 'Y'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void update_qty_detail2(String qty_detailed,String line_number, String nomor_colly) {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "UPDATE tb_item_colly\n"
                + "SET QUANTITY_DETAILED='"
                + qty_detailed
                + "' "
                + "WHERE LINE_NUMBER = '"+line_number+"' AND NOMOR_COLLY = '"+nomor_colly+"'";
        Log.d("UPDATE",mQuery);
        db.execSQL(mQuery);
    }

    void updateFlag2(String flag, String itemId, String line_num, String nomor_colly){
        SQLiteDatabase db = getWritableDatabase();
        mQuery = "UPDATE "+TABLE_ITEM_COLLY+" " +
                "SET "+ COLLY_FLAG +" = '"+flag+"' " +
                "WHERE "+COLLY_INVENTORY_ITEM_ID+" = "+itemId +" " +
                "AND "+ COLLY_LINE_NUMBER +" = '"+line_num+"' AND "+ COLLY_NOMOR_COLLY +" = '"+nomor_colly+"'";
        Log.d("UPDATE", mQuery);
        db.execSQL(mQuery);
        db.close();
    }

    void updateFlagAll2(String flag, String nomor_colly){
        SQLiteDatabase db = getWritableDatabase();
        mQuery = "UPDATE "+TABLE_ITEM_COLLY+" " +
                "SET "+ COLLY_FLAG +" = '"+flag+"' AND "+ COLLY_NOMOR_COLLY +" = '"+nomor_colly+"'";
        Log.d("UPDATE", mQuery);
        db.execSQL(mQuery);
        db.close();
    }

    public Cursor selectUpdateFlagCheck() {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("Data Serial : " + mQuery);
        mQuery = "SELECT REQUEST_NUMBER, INVENTORY_ITEM_ID, QUANTITY_DETAILED, LINE_NUMBER, NOMOR_COLLY, HEADER_ID, LINE_ID " +
                "FROM tb_item_colly " +
                "WHERE FLAG = 'Y'"; //tambah line number
        return db.rawQuery(mQuery, null);
    }

    public Cursor selectInsertKctNF() {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("Data Serial : " + mQuery);
        mQuery = "SELECT REQUEST_NUMBER, INVENTORY_ITEM_ID, QUANTITY_DETAILED, LINE_NUMBER, NOMOR_COLLY " +
                "FROM tb_item_colly " +
                "WHERE FLAG = 'Y' AND COLLY_FLAG = 'NF' AND ITEM_FLAG = 'Y'"; //tambah line number
        return db.rawQuery(mQuery, null);
    }

    public Cursor selectInsertProcedure() {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("Data Serial : " + mQuery);
        mQuery = "SELECT REQUEST_NUMBER, INVENTORY_ITEM_ID, QUANTITY_DETAILED, LINE_NUMBER, NOMOR_COLLY, HEADER_ID, LINE_ID " +
                "FROM tb_item_colly " +
                "WHERE FLAG = 'Y'"; //tambah line number
        return db.rawQuery(mQuery, null);
    }

    //table DO Transact
    public void insertDOTr(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("insert", "" + values.toString());
        db.insert("tb_do_transact", null, values);
    }

    public Cursor selectDOTr() {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT * FROM tb_do_transact";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public void deleteDOTr() {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_do_transact";
        db.execSQL(mQuery);
    }

    //=====================================TABLE HEADER COLLY=======================================
    public void insertCollyTr(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("insert", "" + values.toString());
        db.insert("tb_colly_transact", null, values);
    }

    public Cursor selectCollyTr() {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT * FROM tb_colly_transact";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public void deleteCollyTr() {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_colly_transact";
        db.execSQL(mQuery);
    }

    //table item
    public void insertItemTr(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("insert", "" + values.toString());
        db.insert("tb_item_transact", null, values);
    }

    public Cursor selectItemTr() {
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "SELECT * FROM tb_item_transact";
        Cursor c = db.rawQuery(mQuery, null);
        return c;
    }

    public void deleteItemTr() {
        //SQLite Delete ndes
        SQLiteDatabase db = this.getWritableDatabase();
        mQuery = "DELETE FROM tb_item_transact";
        db.execSQL(mQuery);
    }
}
