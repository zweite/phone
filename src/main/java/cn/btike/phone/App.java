package cn.btike.phone;

import com.alibaba.fastjson.JSON;

import java.util.Date;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        PhoneDb.LoadAddressDB("/Users/zweite/Documents/project/java_env/src/phone/src/main/java/cn/btike/phone/phone.dat");
        Phone phone = PhoneDb.LookupPhone("13924480075");
        System.out.println(JSON.toJSONString(phone));
        batch();
    }

    public static void batch() {
        int total = 1000000;
        String phoneNumber = "13924480075";
        Date start = new Date();
        for (int i = 0; i < total; i++) {
            PhoneDb.LookupPhone(phoneNumber);
        }
        Date end = new Date();
        System.out.println((end.getTime()-start.getTime())*1.0/total + " ms") ;
    }
}
