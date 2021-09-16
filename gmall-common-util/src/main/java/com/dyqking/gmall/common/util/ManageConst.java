package com.dyqking.gmall.common.util;

public class ManageConst {

    //redis : skuInfo
    public static final String SKU_KEY_PREFIX = "sku:";

    public static final String SKU_KEY_SUFFIX = ":info";

    public static final int SKU_KEY_TIMEOUT = 24 * 60 * 60;

    //redis 分布式锁
    public static final int SKU_LOCK_EXPIRE_PX = 10000;
    public static final String SKU_LOCK_SUFFIX = ":lock";


}
