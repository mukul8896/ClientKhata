package dao;

public class DBParameters {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "ClientBilling_DB";

    //Tables
    public static final String DB_CLIENT_TABLE = "Client";
    public static final String DB_BILL_TABLE = "Bill";
    public static final String DB_TRANSECTION_TABLE = "ClientTransection";


    //Keys for Client table in db
    public static final String KEY_CLIENT_ID = "ClientID";
    public static final String KEY_CLIENT_NAME = "ClientName";
    public static final String KEY_CLIENT_ADDRESS = "Address";
    public static final String KEY_CLIENT_FEE = "ClientFee";
    public static final String KEY_CLIENT_BALANCE = "Balance";
    public static final String KEY_CLIENT_CONTACT = "ContactNo";

    //Keys for Bill table in db
    public static final String KEY_BILL_ID = "BillID";
    public static final String KEY_BILL_NO = "BillNo";
    public static final String KEY_BILL_YEAR = "FinancialYear";
    public static final String KEY_BILL_CLIENTID = "ClientId";
    public static final String KEY_BILL_TODATE = "ToDate";
    public static final String KEY_BILL_FROMDATE = "FromDate";
    public static final String KEY_BILL_ISSHARED = "IsShared";
    public static final String KEY_BILL_GENERATIONDATE = "GenerationDate";

    //Keys for ClientTransection table in db
    public static final String KEY_TRANSECTION_ID = "TransectionID";
    public static final String KEY_TRANSECTION_BILLID = "BillID";
    public static final String KEY_TRANSECTION_CLIENTID = "ClientID";
    public static final String KEY_TRANSECTION_TYPE = "TransectionType";
    public static final String KEY_TRANSECTION_DATE = "TransectionDate";
    public static final String KEY_TRANSECTION_DESC = "Description";
    public static final String KEY_TRANSECTION_AMOUNT = "Amount";
    public static final String KEY_TRANSECTION_BILLDETAILS = "BillDetails";
}
