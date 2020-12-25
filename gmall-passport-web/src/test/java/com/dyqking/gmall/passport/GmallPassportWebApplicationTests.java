package com.dyqking.gmall.passport;

import com.dyqking.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {


    @Test
    public void jwtTest() {

        String key = "dyqking";
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", "666");
        map.put("name", "很厉害");
        String sort = "192.168.183.1";

        String encode = JwtUtil.encode(key, map, sort);
        System.out.println("token:" + encode);

        Map<String, Object> deMap = JwtUtil.decode(encode, key, sort);
        System.out.println("map:" + deMap);
        //eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoi5b6I5Y6J5a6zIiwiaWQiOiI2NjYifQ.WtXtDK9pGG7Hq8XD-X2Tmsk0XZ1ccB0dg8-tANt7nmQ
        //{name=很厉害, id=666}
    }

}
