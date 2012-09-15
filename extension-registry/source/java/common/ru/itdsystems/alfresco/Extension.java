/**
 * Copyright © 2012 ITD Systems
 *
 * This file is part of Alvex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.itdsystems.alfresco;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for all extensions
 * 
 * @author Alexey Ermakov
 * 
 */

public class Extension implements InitializingBean {
	protected String id = null;
	protected String version = null;
	protected ExtensionRegistry registry = null;
	protected MessageDigest md5;
	protected String fileListPath = null;

	// constructor
	public Extension() throws Exception {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			throw new Exception(
					"There is no MD5 algorithm support, but we really need it.",
					e);
		}
	}

	// dependency injection
	public void setExtensionRegistry(ExtensionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// register extension
		if (registry != null)
			registry.registerExtension(this);
		else
			throw new Exception("ExtensionRegistry is not set, it's fatal.");

	}

	// returns extension id
	public String getId() {
		return id;
	}

	// returns extension version
	public String getVersion() {
		return version;
	}

	// returns md5 hash for a specified file
	protected String getMD5Hash(String file) throws Exception {
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream(file);

		} catch (Exception e) {
			throw new Exception("Error occured while opening a file.", e);
		}
		byte[] bytesOfMessage = IOUtils.toByteArray(is);
		byte[] digest = md5.digest(bytesOfMessage);
		String result = "";

		for (int i = 0; i < digest.length; i++) {
			result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
					.substring(1);
		}
		is.close();
		return result;
	}

	// returns md5 hashes for all extension files
	public Map<String, String> getMD5Hashes() throws Exception {
		// read list of files
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader()
					.getResourceAsStream(fileListPath);
		} catch (Exception e) {
			throw new Exception("Error occured while opening file list.", e);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String filePath;
		String md5hash;
		Map<String,String> md5hashes = new HashMap<String, String>();
		while ((filePath = br.readLine()) != null) {
			try {
				md5hash = getMD5Hash(filePath);
			} catch (Exception e) {
				md5hash = "";
			}
			md5hashes.put(filePath, md5hash);

		}
		is.close();

		return md5hashes;
	}
}