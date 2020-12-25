package com.dyqking.gmall.cart.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.bean.CartInfo;
import com.dyqking.gmall.bean.SkuInfo;
import com.dyqking.gmall.cart.constant.CartConst;
import com.dyqking.gmall.cart.mapper.CartInfoMapper;
import com.dyqking.gmall.config.RedisUtil;
import com.dyqking.gmall.service.CartService;
import com.dyqking.gmall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ItemService itemService;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);

        if (cartInfoExist != null) { // 数据库有数据

            Integer skuNumOld = cartInfoExist.getSkuNum();
            cartInfoExist.setSkuNum(skuNumOld + skuNum);
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        } else { // 数据库无数据
            cartInfoExist = new CartInfo();

            SkuInfo skuInfo = itemService.getSkuInfo(skuId);
            cartInfoExist.setSkuId(skuId);
            cartInfoExist.setUserId(userId);
            cartInfoExist.setSkuNum(skuNum);
            cartInfoExist.setCartPrice(new BigDecimal(skuInfo.getPrice()));
            cartInfoExist.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoExist.setSkuName(skuInfo.getSkuName());
            cartInfoMapper.insertSelective(cartInfoExist);
        }
        // 更新缓存 cookie 或者 redis
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            //user:userId:cart
            String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoExist));

            // 更新购物车过期时间
            String userInfoKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
            Long ttl = jedis.ttl(userInfoKey);

            jedis.expire(cartKey, ttl.intValue());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        Jedis jedis = null;
        List<String> cartStr = null;
        List<CartInfo> cartInfoList = new ArrayList<>();

        try {
            jedis = redisUtil.getJedis();
            cartStr = jedis.hvals(userCartKey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) jedis.close();
        }

        if (cartStr != null && cartStr.size() > 0) {
            for (String s : cartStr) {
                CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
        } else {
            //从数据库拿数据
            cartInfoList = loadCartCache(userId);
        }

        if (cartInfoList != null && cartInfoList.size() > 1) {
            //根据修改时间进行比较  (暂且使用id比较)
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
        }

        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {

        // 根据userId获得购物车信息
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        for (CartInfo cookieInfo : cartListFromCookie) {
            boolean isMatch = false;
            for (CartInfo dbinfo : cartInfoList) {
                if (cookieInfo.getSkuId().equals(dbinfo.getSkuId())) {
                    //合并
                    dbinfo.setSkuNum(cookieInfo.getSkuNum() + dbinfo.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(dbinfo);
                    isMatch = true;
                }
            }
            if(!isMatch){ // 数据库中没有该缓存信息
                cookieInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cookieInfo);
            }
        }

        // 更新完毕后再一次从数据库中获得数据 加入缓存
        List<CartInfo> newCartInfoList = loadCartCache(userId);

        for (CartInfo cartInfo : newCartInfoList) {
            for (CartInfo info : cartListFromCookie) {
                if (cartInfo.getSkuId().equals(info.getSkuId())){
                    // 只有被勾选的才会进行更改
                    if (!info.getIsChecked().equals(cartInfo.getIsChecked())){
                        cartInfo.setIsChecked(info.getIsChecked());
                        // 更新redis中的isChecked
                        checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                    }
                }
            }
        }

        return newCartInfoList;

    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        Jedis jedis = null;
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        try {
            jedis = redisUtil.getJedis();
            String hget = jedis.hget(cartKey, skuId);
            CartInfo cartInfo = JSON.parseObject(hget, CartInfo.class);
            cartInfo.setIsChecked(isChecked);
            String cartJSON = JSON.toJSONString(cartInfo);
            jedis.hset(cartKey, skuId, cartJSON);

            // 新增到已选中购物车
            String checkedCart = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
            if(isChecked.equals("1")){
                jedis.hset(checkedCart, skuId, cartJSON);
            }else{
                jedis.hdel(checkedCart, skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) jedis.close();
        }
//        CartInfo cartInfo = new CartInfo();
//        cartInfo.setUserId(userId);
//        cartInfo.setIsChecked(isChecked);
//        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userCheckedKey  = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        List<String> hvals = jedis.hvals(userCheckedKey);

        List<CartInfo> cartInfoList = new ArrayList<>();
        if(hvals!=null&&hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
        }
        return cartInfoList;
    }

    //从数据库中拿购物车信息
    public List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            if(cartInfo.getCartPrice()!=cartInfo.getSkuPrice()){
                cartInfo.setCartPrice(cartInfo.getSkuPrice());
                cartInfoMapper.updateByPrimaryKeySelective(cartInfo);
            }
            String cartJson = JSON.toJSONString(cartInfo);
            // key 都是同一个，值会产生重复覆盖！
            map.put(cartInfo.getSkuId(),cartJson);
        }
        // 将java list - redis hash
        jedis.hmset(userCartKey,map);
        jedis.close();
        return  cartInfoList;
    }
}
