package cn.btike.phone;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PhoneDb {
    private static byte[] buf;
    private static String version = "";
    private static int recordTotal = 0;
    private static int firstOffset = 0;

    private static int CHAR_LEN = 1;
    private static int INT_LEN = 4;
    private static int PHONE_INDEX_LENGTH = 9;


    private static byte[] getBytes(byte[] data, int start, int len) {
        if (data.length < start + len) {
            return new byte[]{};
        }

        byte[] cutData = new byte[len];
        for (int i = start; i < start+len; i++) {
            cutData[i-start] = data[i];
        }

        return cutData;
    }


    // 记录总数
    private static int getRecordTotal() {
        if (recordTotal != 0) {
            return recordTotal;
        }

        ByteBuffer bb = ByteBuffer.wrap(buf, INT_LEN, INT_LEN);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        firstOffset = bb.getInt();
        recordTotal = (buf.length-firstOffset)/PHONE_INDEX_LENGTH;
        return recordTotal;
    }

    // 获取版本号
    private static String getVersion() {
        if (!"".equals(version)) {
            return version;
        }

        ByteBuffer bb = ByteBuffer.wrap(buf, 0, INT_LEN);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        version = new String(bb.array());
        return version;
    }

    public static Phone LookupPhone(String phoneNumber) {
        if (phoneNumber.length() < 7 || phoneNumber.length() > 11) {
            // 此处应该抛错
            return null;
        }

        int phoneSevenInt = new Integer(phoneNumber.substring(0, 7)).intValue();
        int left = 0;
        int right = (buf.length-firstOffset)/PHONE_INDEX_LENGTH;
        while (true) {
            if (left > right) {
                break;
            }

            int mid = (left+right)/2;
            int offset = firstOffset+mid*PHONE_INDEX_LENGTH;
            if (offset >= buf.length) {
                break;
            }

            int curPhone = ByteBuffer.wrap(buf, offset, INT_LEN).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int recordOffset = ByteBuffer.wrap(buf, offset+INT_LEN, INT_LEN).order(ByteOrder.LITTLE_ENDIAN).getInt();


            int cartType = new Integer(buf[offset+INT_LEN*2]).intValue();

            if (curPhone > phoneSevenInt) {
                right = mid - 1;
            } else if (curPhone < phoneSevenInt) {
                left = mid + 1;
            } else {
                int endPoint = recordOffset;
                for (int i = recordOffset; i < buf.length; i++) {
                    if (buf[i] == '\0') {
                        break;
                    }
                    endPoint++;
                }
                return formatPhoneContent(phoneNumber, new String(getBytes(buf, recordOffset, endPoint-recordOffset)), cartType);
            }

        }
        return null;
    }

    private static String getPhoneType(int type) {
        switch (type) {
            case 1:
                return "移动";
            case 2:
                return "联通";
            case 3:
                return "电信";
            case 4:
                return "电信虚拟运营商";
            case 5:
                return "联通虚拟运营商";
            case 6:
                return "移动虚拟运营商";
            default:
                return "未知运营商";
        }
    }

    private static Phone formatPhoneContent(String phoneNumb, String content, int type) {
        String[] bits = content.split("\\|");
        Phone phone = new Phone();
        phone.phoneNumb = phoneNumb;
        phone.province = bits[0];
        phone.city = bits[1];
        phone.zipCode = bits[2];
        phone.areaCode = bits[3];
        phone.phoneType = getPhoneType(type);
        return phone;
    }

    private static void init(byte[] buf) {
        PhoneDb.buf = buf;
        getVersion();
        getRecordTotal();
    }

    // 通过配置文件加载地区配置
    public static void LoadAddressDB(String filePath) {
        try {
            byte[] buf = FileUtils.readFileToByteArray(new File(filePath));
            init(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
