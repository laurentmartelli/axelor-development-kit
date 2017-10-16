/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.axelor.app.internal.AppFilter;
import com.axelor.common.ClassUtils;
import com.google.common.base.Throwables;

public final class AppSettings {

	private static final String DEFAULT_CONFIG_LOCATION = "application.properties";
	private static final String CUSTOM_CONFIG_LOCATION = "axelor.config";

	private Properties properties = new Properties();

	private static AppSettings instance;

	private AppSettings() {
		loadFromResource(DEFAULT_CONFIG_LOCATION);
		
		String config = System.getProperty(CUSTOM_CONFIG_LOCATION);
		if (!StringUtils.isBlank(config))
			load(new File(config));
	}

	private void load(InputStream stream) {
		try {
			properties.load(stream);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
	}

	private void load(File file) {
		try {
			load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			Throwables.propagate(e);
		}
	}

	private void loadFromResource(String resourcePath) {
		try (InputStream stream =  ClassUtils.getResourceStream(resourcePath))
		{
			load(stream);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
	}

	public static AppSettings get() {
		if (instance == null) {
			instance = new AppSettings();
		}
		return instance;
	}

	public String get(String key) {
		return sub(properties.getProperty(key));
	}

	public String get(String key, String defaultValue) {
		String value = properties.getProperty(key, defaultValue);
		if (value == null || "".equals(value.trim())) {
			value = defaultValue;
		}
		return sub(value);
	}

	public int getInt(String key, int defaultValue) {
		try {
			return Integer.parseInt(get(key).toString());
		} catch (Exception e){}
		return defaultValue;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		try {
			return Boolean.parseBoolean(get(key).toString());
		} catch (Exception e){}
		return defaultValue;
	}

	public String getPath(String key, String defaultValue) {
		String path = get(key, defaultValue);
		if (path == null) {
			return null;
		}
		return sub(path);
	}

	private String sub(String value) {
		if (value == null) {
			return null;
		}
		final Calendar cal = Calendar.getInstance();
		return value.replace("{year}", "" + cal.get(Calendar.YEAR))
					.replace("{month}", "" + cal.get(Calendar.MONTH))
					.replace("{day}", "" + cal.get(Calendar.DAY_OF_MONTH))
					.replace("{java.io.tmpdir}", System.getProperty("java.io.tmpdir"))
					.replace("{user.home}", System.getProperty("user.home"));
	}

	/**
	 * Get the application base URL.
	 * <p>
	 * This method tries to calculate the base url from current http request. If
	 * the method is called outside of http request scope, it returns the value
	 * of <code>application.baseUrl</code> configuration setting.
	 * </p>
	 * 
	 * @return application base url
	 */
	public String getBaseURL() {
		String url = AppFilter.getBaseURL();
		if (url == null) {
			url = get("application.baseUrl");
		}
		return url;
	}

	public boolean isProduction() {
		return !"dev".equals(get("application.mode", "dev"));
	}

	/**
	 * For internal use only.
	 */
	public Properties getProperties() {
		return properties;
	}
}
