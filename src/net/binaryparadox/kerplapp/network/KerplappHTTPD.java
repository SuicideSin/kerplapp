
package net.binaryparadox.kerplapp.network;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

import net.binaryparadox.kerplapp.KerplappKeyStore;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLServerSocketFactory;

public class KerplappHTTPD extends SimpleWebServer {
    private static final String TAG = "KerplappHTTPD";

    public KerplappHTTPD(String ipAddressString, int port, File wwwroot, boolean quiet) {
        super(ipAddressString, port, wwwroot, quiet);
    }

    public void enableHTTPS(KerplappKeyStore keystore) {
        try {
            SSLServerSocketFactory factory = NanoHTTPD.makeSSLSocketFactory(
                    keystore.getKeyStore(),
                    keystore.getKeyManagers());
            makeSecure(factory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
