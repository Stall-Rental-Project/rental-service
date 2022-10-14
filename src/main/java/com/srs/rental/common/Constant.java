package com.srs.rental.common;

import java.util.List;

/**
 * @author duynt on 3/1/22
 */
public class Constant {

    public static final List<String> RATE_SORTS = List.of("code", "type", "detail");
    public static final List<String> WORKFLOW_SORTS = List.of("code", "type");
    public static final List<String> APPLICATION_SORTS = List.of("code");
    public static final List<String> LEASE_SORTS = List.of("firstName", "lastName");
    public static final List<String> HELPER_SORTS = List.of("firstName", "lastName");
    public static final List<String> RENEWAL_SORTS = List.of("firstName");
    public static final List<String> TRANSFER_SORTS = List.of("firstName");
    public static final List<String> EXTENSION_SORTS = List.of("firstName");
    public static final List<String> HA_SORTS = List.of("associationName", "createdAt");
    public static final List<String> RENEWAL_HAWKER_SORTS = List.of("code");
    public static final List<String> NEW_HAWKER_PERMIT_SORTS = List.of("code", "firstName",
            "lastName");

    public static final List<String> PRIVATE_STALLHOLDER_SORTS = List.of("code", "firstName",
            "lastName", "marketName");

    public static final List<String> MOP_SORTS = List.of("issuedAt");

    public static final int TEMPORARY_STALL_LEASE_DURATION = 1; // year
    public static final int PERMANENT_STALL_LEASE_DURATION = 5; // year

    public static final String RSA_PREFIX = "RSA"; // Renewal stall application
    public static final String NSA_PREFIX = "NSA"; // New stall application
    public static final String TSA_PREFIX = "TSA"; // Transfer stall application
    public static final String SEA_PREFIX = "SEA"; // Stall extension application
    public static final String RPA_PREFIX = "RPA"; // Repair permit application

    public static final List<String> HAWKER_HELPER_SORTS = List.of("firstName", "lastName");
    public static final List<String> HAWKER_ACCOUNT_SORTS = List.of("firstName", "lastName",
            "siteType");
    public static final List<String> RENEWAL_MARKET_OPERATOR_SORTS = List.of("code", "firstName",
            "lastName", "marketName");

    public static final List<String> MARKET_OPERATOR_APP_SORTS = List.of("code", "firstName",
        "lastName", "marketName");
    public static final List<String> RENEWAL_MARKET_OPERATOR_APP_SORTS = List.of("createAt");

    public static final List<String> MARKET_OPERATOR_ACCOUNT_SORTS = List.of("operatorId",
        "firstName",
        "lastName", "marketName");

    public static final int MARKET_PRIVATE_FRANCHISE_DURATION = 1; // year
    public static final int MARKET_TALIBABA_FRANCHISE_DURATION = 5; // year

    public static final String PRIVATE_MARKET_MANAGE_ACCOUNT = "PRIVATE_MARKET_MANAGE_ACCOUNT";
    public static final String PRIVATE_MARKET_VIEW_INFORMATION = "PRIVATE_MARKET_VIEW_INFORMATION";

    public static final String PRIVATE_MARKET_ACCESS_DENIED = "USER DON'T HAVE PRIVILEGE TO ACCESS";


}
