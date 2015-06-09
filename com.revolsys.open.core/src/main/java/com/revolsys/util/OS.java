package com.revolsys.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.format.json.JsonMapIoFactory;
import com.revolsys.io.FileUtil;

public class OS {

  public static final String OS_ARCH = System.getProperty("os.arch");

  public final static String OS_NAME = System.getProperty("os.name");

  public final static boolean IS_WINDOWS = OS_NAME.startsWith("Windows");

  public final static boolean IS_SOLARIS = OS_NAME.equals("SunOS");

  public final static boolean IS_LINUX = OS_NAME.equals("Linux");

  public final static boolean IS_MAC = OS_NAME.contains("OS X") || OS_NAME.equals("Darwin");

  public static File getApplicationDataDirectory(final String applicationName) {
    String path;
    if (OS.isWindows()) {
      path = System.getenv("APPDATA");
    } else if (OS.isMac()) {
      path = System.getProperty("user.home") + "/Library/Application Support";
    } else {
      path = System.getProperty("user.home") + "/.config";
    }
    final File directory = FileUtil.getFile(path + "/" + applicationName);
    directory.mkdirs();
    return directory;
  }

  public static String getArch() {
    final String osArch = OS_ARCH.toLowerCase();
    if (osArch.equals("i386")) {
      return "x86";
    } else if (osArch.startsWith("amd64") || osArch.startsWith("x86_64")) {
      return "x86_64";
    } else if (osArch.equals("ppc")) {
      return "ppc";
    } else if (osArch.startsWith("ppc")) {
      return "ppc_64";
    } else if (osArch.startsWith("sparc")) {
      return "sparc";
    } else {
      return OS_ARCH;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getPreference(final String applicationName, final String path,
    final String propertyName) {
    final Map<String, Object> preferences = getPreferences(applicationName, path);
    return (T)Property.get(preferences, propertyName);
  }

  public static <T> T getPreference(final String applicationName, final String path,
    final String propertyName, final T defaultValue) {
    final T value = getPreference(applicationName, path, propertyName);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static File getPreferenceFile(final String applicationName, final String path) {
    if (path.contains("..")) {
      throw new IllegalArgumentException("Path cannot contain the '..' character sequernce: "
        + path);
    }
    final File preferencesDirectory = getPreferencesDirectory(applicationName);
    final File file = FileUtil.getFile(preferencesDirectory, path + ".rgobject");
    file.getParentFile().mkdirs();
    return file;
  }

  public static Map<String, Object> getPreferences(final String applicationName, final String path) {
    final File file = getPreferenceFile(applicationName, path);
    if (file.exists()) {
      return JsonMapIoFactory.toMap(file);
    } else {
      return new LinkedHashMap<String, Object>();
    }
  }

  public static File getPreferencesDirectory(final String applicationName) {
    String path;
    if (OS.isWindows()) {
      path = System.getenv("APPDATA") + "/" + applicationName + "/Preferences";
    } else if (OS.isMac()) {
      path = System.getProperty("user.home") + "/Library/Preferences/" + applicationName;
    } else {
      path = System.getProperty("user.home") + "/.config/" + applicationName + "/Preferences";
    }
    final File directory = FileUtil.getFile(path);
    directory.mkdirs();
    return directory;
  }

  public static File getUserDirectory() {
    final String home = System.getProperty("user.home");
    return FileUtil.getFile(home);
  }

  public static File getUserDirectory(final String path) {
    final File userDirectory = getUserDirectory();
    return FileUtil.getFile(userDirectory, path);
  }

  public static boolean isMac() {
    return IS_MAC;
  }

  public static boolean isUnix() {
    return IS_SOLARIS || IS_LINUX;
  }

  public static boolean isWindows() {
    return IS_WINDOWS;
  }

  public static void setPreference(final String applicationName, final String path,
    final String propertyName, final Object value) {
    final Map<String, Object> preferences = getPreferences(applicationName, path);
    Property.set(preferences, propertyName, value);
    final File file = getPreferenceFile(applicationName, path);
    JsonMapIoFactory.write(preferences, file, true);
  }

}
