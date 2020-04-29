package com.rami.common.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.rami.driverspecifics.Asset;

public interface IPersist {
	public Asset getPersistedObj(String fileName) throws FileNotFoundException;
	void setPersistedObj(Asset obj) throws FileNotFoundException, IOException;
}
