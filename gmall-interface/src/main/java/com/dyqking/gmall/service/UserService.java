package com.dyqking.gmall.service;

import com.dyqking.gmall.bean.UserAddress;
import com.dyqking.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 查询所有用户
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户ID查询用户
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);

    /**
     * 根据用户名和密码进行登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 根据用户id进行redis查询并更新
     * @param userId
     * @return
     */
    UserInfo verify(String userId);

}
