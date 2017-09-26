package org.runchip.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NetUtils {

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean isValidIP(CharSequence devIP) {
        if (devIP == null || devIP.length() == 0) return false;
        String[] nums = devIP.toString().split("\\.");
        if (nums.length != 4) return false;
        for (int i = 0; i < nums.length; i++) {
            int n = 0;
            try {
                n = Integer.parseInt(nums[i]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (n < 0 || n > 255) return false;
        }
        return true;
    }

    public static boolean isValidPort(CharSequence devPort) {
        if (devPort == null || devPort.length() == 0) return false;
        int port = 0;
        try {
            port = Integer.parseInt(devPort.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return 0 <= port && port <= 65535;
    }

}
