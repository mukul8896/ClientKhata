package dao;

public class DBParameters {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "ClientBilling_DB";

    //Tables
    public static final String DB_CLIENT_TABLE = "Client";


    //Keys of our table in db
    public static final String KEY_CLIENT_ID = "ClientID";
    public static final String KEY_CLIENT_NAME = "ClientName";
    public static final String KEY_CLIENT_ADDRESS = "Address";
    public static final String KEY_CLIENT_FEE = "ClientFee";
    public static final String KEY_CLIENT_BALANCE = "Balance";
    public static final String KEY_CLIENT_CONTACT = "ContactNo";
}
