package fr.bouyguestelecom.tv.openapi.secondscreen.bbox;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Send a Wake-On-Lan packet to the given host
 * Sources are from: https://code.google.com/p/mythmote/source/browse/branches/Wake_On_LAN/src/tkj/android/homecontrol/mythmote/WOLPowerManager.java
 * @author rob elsner
 */
public class WOLPowerManager {
    /**
     * Send a WOL broadcast packet to the specified MAC
     *
     * @param macAddress 00:00:00:00:00 format
     * @return true on success
     * @throws IOException
     */
    public static boolean sendWOL(final String macAddress, final Context mContext)
            throws IOException {
        try {
            byte[] wolPacket = buildWolPacket(macAddress);
            InetAddress broadcast = getBroadcastAddress(mContext);
            DatagramSocket socket = new DatagramSocket(7000);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(wolPacket, wolPacket.length, broadcast, 7000);
            socket.send(packet);
            socket.close();
            return true;
        } catch (Exception e) {
            Log.e("WakeOnLan", "failure", e);
            return false;
        }
    }

    private static InetAddress getBroadcastAddress(final Context mContext) throws IOException {

        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    /**
     * @param macAddress should be formatted as 00:00:00:00:00:00 in a string
     * @return the raw packet bytes to send along
     */
    private static byte[] buildWolPacket(final String macAddress) {
        final byte[] preamble =
                {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF};
        byte[] macBytes = new byte[6];
        String[] octets = macAddress.split(":");
        for (int i = 0; i < 6; i++) {

            macBytes[i] = (byte) (Integer.parseInt(octets[i], 16) & 0xFF);
        }
        final byte[] body = new byte[36]; // 6 bytes for mac, repeated 6 times
        for (int destPos = 0; destPos < 36; destPos += 6) {
            System.arraycopy(macBytes, 0, body, destPos, 6);
        }
        final byte[] packetBody = new byte[42];
        System.arraycopy(preamble, 0, packetBody, 0, 6);
        System.arraycopy(body, 0, packetBody, 6, 36);
        return packetBody;
    }

    /**
     * Sources from: http://www.flattermann.net/2011/02/android-howto-find-the-hardware-mac-address-of-a-remote-host/
     * @param ip ip of the device which we want the mac address
     * @return the mac address formatted as 00:00:00:00:00:00 in a string
     */
    public static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}