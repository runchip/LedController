package org.runchip.utils;

import org.junit.Test;

import static org.junit.Assert.*;

import org.runchip.utils.NetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NetUtilsTest {
    @Test
    public void test_isValidIp() {
        assertTrue(NetUtils.isValidIP("0.0.0.0"));
        assertTrue(NetUtils.isValidIP("0.0.0.1"));
        assertTrue(NetUtils.isValidIP("255.255.255.255"));
        assertTrue(NetUtils.isValidIP("255.255.255.0"));
        assertFalse(NetUtils.isValidIP("255.255.255.256"));
        assertFalse(NetUtils.isValidIP("255.255.255.255.1"));
        assertFalse(NetUtils.isValidIP("255.255.1"));
        assertFalse(NetUtils.isValidIP("255.255..1"));
        assertFalse(NetUtils.isValidIP("255.255.abc.1"));
    }

    @Test
    public void test_isValidPort() {
        assertTrue(NetUtils.isValidPort("0"));
        assertTrue(NetUtils.isValidPort("1"));
        assertTrue(NetUtils.isValidPort("2"));
        assertTrue(NetUtils.isValidPort("65535"));
        assertFalse(NetUtils.isValidPort("65536"));
        assertFalse(NetUtils.isValidPort("abc"));
    }

    @Test
    public void test_intToInetAddress() {
        byte[] ipbytes = { (byte) 0xC0, (byte) 0xA8, (byte) 0x01, (byte) 0x02};
        int hostAddress = 0x0201A8C0;
        try {
            InetAddress address = InetAddress.getByAddress(ipbytes);
            assertEquals(address.getHostAddress(), NetUtils.intToInetAddress(hostAddress).getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
