package com.dyqking.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.bean.UserAddress;
import com.dyqking.gmall.bean.UserInfo;
import com.dyqking.gmall.config.RedisUtil;
import com.dyqking.gmall.service.UserService;
import com.dyqking.gmall.usermanage.mapper.UserAddressMapper;
import com.dyqking.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;
    //redis
    public String userKey_prefix = "user:";
    public String userinfoKey_suffix = ":info";
    public int userKey_timeOut = 60 * 60 * 24;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        String newPassword = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(newPassword);
        UserInfo user = userInfoMapper.selectOne(userInfo);
        if (user != null) {//登录成功
            Jedis jedis = null;
            try {
                jedis = redisUtil.getJedis();
                String key = userKey_prefix + user.getId() + userinfoKey_suffix;
                //jedis.set(key,JSON.toJSONString(user),"NX","EX", userKey_timeOut);
                jedis.setex(key, userKey_timeOut, JSON.toJSONString(user));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }

        return user;
    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String key = userKey_prefix + userId + userinfoKey_suffix;

            String userJson = jedis.get(key);
            if (!StringUtils.isEmpty(userJson)) {
                //延长实效
                jedis.expire(key, userKey_timeOut);
                return JSON.parseObject(userJson, UserInfo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }
}
