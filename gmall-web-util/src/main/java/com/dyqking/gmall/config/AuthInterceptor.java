package com.dyqking.gmall.config;

import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.common.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 权限过滤
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //originUrl=http%3a%2f%2fitem.gmall.com%2f46.html
        //eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IjEyMyIsInVzZXJJZCI6IjQifQ.Ad3BO-BORnlvx5CjUUNM1QtIcm8BTA8BYtWESY2QxPQ
        String token = request.getParameter("newToken");

        if (token != null) {
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        } else {
            //尝试从cookie中拿 token
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        if (token != null) {
            //从token中获取nikeName
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);
        }

        //-----------------------/以下对需要登录的页面进行拦截/-------------------
        HandlerMethod handlerMethod = null;
        try {
            handlerMethod = (HandlerMethod) handler;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        LoginRequire loginRequireAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //方法上有LoginRequie注解的才进行拦截
        if (loginRequireAnnotation != null) {
            // 获得盐， 请注意nginx中的配置
            String remoteAddr = request.getHeader("x-forwarded-for");
            //http://passport.atguigu.com/verify?token=...&currentIp=192.168.183.1
            //进行是否已登录的验证
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            // 认证成功， 将userId放入作用域
            if ("success".equals(result)) {
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);
                return true;
            } else { // 认证失败，看是否是必须登录的控制器
                if (loginRequireAnnotation.autoRedirect()) {
                    // 必须登录， 跳转到登录页面
                    String requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "utf-8");
                    System.out.println(encodeURL);
                    //http://passport.atguigu.com/index?originUrl=http%3a%2f%2fitem.dyq.com%2f46.html
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeURL);
                    return false;
                }
            }
        }
        return true;
    }

    private Map getUserMapByToken(String token) {

        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenByte = base64UrlCodec.decode(tokenUserInfo);
        String userJSON = null;
        try {
            userJSON = new String(tokenByte, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(userJSON, Map.class);
        return map;
    }

    /**
     * 进入控制器之后，视图渲染之前
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 视图渲染之后
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }
}
