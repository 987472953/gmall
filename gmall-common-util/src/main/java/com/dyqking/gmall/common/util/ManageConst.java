package com.dyqking.gmall.common.util;

public class ManageConst {

    //redis : skuInfo
    public static final String SKUKEY_PREFIX="sku:";

    public static final String SKUKEY_SUFFIX=":info";

    public static final int SKUKEY_TIMEOUT=24*60*60;

    //redis 分布式锁
    public static final int SKULOCK_EXPIRE_PX=10000;
    public static final String SKULOCK_SUFFIX=":lock";


}
