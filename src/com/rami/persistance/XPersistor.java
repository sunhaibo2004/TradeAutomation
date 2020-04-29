package com.rami.persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rami.common.interfaces.IPersist;
import com.rami.driverspecifics.Asset;
import com.thoughtworks.xstream.XStream;

public class XPersistor implements IPersist {
    
    @Override
    public synchronized Asset getPersistedObj(String fileName) throws FileNotFoundException {
        XStream xs = new XStream();
        File input = new File(fileName);
        FileInputStream fis = new FileInputStream(input);
        Asset readAsset = (Asset) xs.fromXML((InputStream) fis);
        return readAsset;
    }
    
    @Override
    public synchronized void setPersistedObj(Asset asset) throws IOException {
        XStream xs = new XStream();
        String fileName = asset.getName();
        File tgtFile = new File(fileName);
        if (tgtFile.exists()) {
            String bakFileName = fileName + "_" + System.currentTimeMillis() + ".OLD";
            File bakFile = new File(bakFileName);
            tgtFile.renameTo(bakFile);
        }
        FileOutputStream fop = new FileOutputStream(tgtFile);
        xs.toXML(asset, (OutputStream) fop);
        fop.close();
    }
}
