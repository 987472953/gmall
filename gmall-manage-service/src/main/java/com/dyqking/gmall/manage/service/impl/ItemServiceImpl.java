package com.dyqking.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.common.util.ManageConst;
import com.dyqking.gmall.config.RedisUtil;
import com.dyqking.gmall.bean.*;
import com.dyqking.gmall.manage.mapper.*;
import com.dyqking.gmall.service.ItemService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        Jedis jedis = null;
        RLock myLock = null;
        try {
//        return getSkuInfoJedis(skuId);
            Config config = new Config();
            config.useSingleServer().setAddress("redis://139.224.30.125:8989").setPassword("dyq*1010A");
            RedissonClient redissonClient = Redisson.create(config);
            myLock = redissonClient.getLock("myLock");
            myLock.lock(10, TimeUnit.SECONDS);


            jedis = redisUtil.getJedis();
            SkuInfo skuInfo = null;
            String skuInfoKey = ManageConst.SKU_KEY_PREFIX + skuId + ManageConst.SKU_KEY_SUFFIX;

            if (jedis.exists(skuInfoKey)) {
                String skuJson = jedis.get(skuInfoKey);
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
            } else {
                skuInfo = getSkuInfoDB(skuId);
                jedis.setex(skuInfoKey, ManageConst.SKU_KEY_TIMEOUT, JSON.toJSONString(skuInfo));
            }
            return skuInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return getSkuInfoDB(skuId);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
            if (myLock != null) {
                myLock.unlock();
            }
        }
    }

    private SkuInfo getSkuInfoJedis(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();
            String skuInfoKey = ManageConst.SKU_KEY_PREFIX + skuId + ManageConst.SKU_KEY_SUFFIX;

            String skuJson = jedis.get(skuInfoKey);
            if (skuJson == null || skuJson.length() == 0) {
                // redis 无数据 分布式锁
                String lockKey = ManageConst.SKU_KEY_PREFIX + skuId + ManageConst.SKU_LOCK_SUFFIX;

                // value 的值不影响返回的值 若设置锁成功则返回OK  否则返回null
                //jedis.setex(lockKey, ManageConst.SKULOCK_EXPIRE_PX, "doing");
                String lock = jedis.set(lockKey, "OK", "NX", "PX", ManageConst.SKU_LOCK_EXPIRE_PX);

                if ("OK".equals(lock)) {
                    //成功获得锁
                    skuInfo = getSkuInfoDB(skuId);
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    //不管skuInfo的数据是否为空都放入redis
                    jedis.setex(skuInfoKey, ManageConst.SKU_KEY_TIMEOUT, skuRedisStr);
                    jedis.del(lockKey);

                    return skuInfo;
                } else {
                    System.out.println("等待！");
                    // 等待
                    Thread.sleep(1000);
                    // 自旋
                    return getSkuInfo(skuId);
                }
            } else {
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
            //解决redis宕机
            return getSkuInfoDB(skuId);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        if(skuInfo!=null){
            SkuImage skuImage = new SkuImage();
            skuImage.setSkuId(skuId);
            List<SkuImage> imageList = skuImageMapper.select(skuImage);
            skuInfo.setSkuImageList(imageList);

            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
            skuInfo.setSkuAttrValueList(skuAttrValueList);

            SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
            skuSaleAttrValue.setSkuId(skuId);
            List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
            skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        }
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        if(skuInfo==null){
            return null;
        }
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValue(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValue(spuId);
    }
}
