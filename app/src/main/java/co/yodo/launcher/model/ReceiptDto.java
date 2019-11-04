package co.yodo.launcher.model;

import co.yodo.restapi.network.model.ServerResponse;

public class ReceiptDto {
    public ServerResponse response;
    public String total;
    public String cashTender;
    public String cashBack;
    public String currency;
    public String staticBalance;
}
