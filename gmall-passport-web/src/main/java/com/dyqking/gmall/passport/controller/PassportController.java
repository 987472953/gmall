package com.dyqking.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.UserInfo;
import com.dyqking.gmall.passport.config.JwtUtil;
import com.dyqking.gmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.awt.geom.AreaOp;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassportController {


    @Reference
    private UserService userService;

    @RequestMapping("index")
    public String toIndex(HttpServletRequest request) {

        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    @Value("${token.key}")
    String signKey;

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request) {

        String remoteAddr = request.getHeader("X-forwarded-for");
        UserInfo result = userService.login(userInfo);


        if (result != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", result.getId());
            map.put("nickName", result.getNickName());

            String token = JwtUtil.encode(signKey, map, remoteAddr);
            return token;
        } else {
            return "fail";
        }
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request) {
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");

         if (!StringUtils.isEmpty(token)) {
            //进行验证
            Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
            if (map != null && map.size() > 0) {
                String userId = (String) map.get("userId");
                UserInfo userInfo = userService.verify(userId);
                if(userInfo != null){
                    return "success";
                }
            }
        }
        return "fail";
    }
}
