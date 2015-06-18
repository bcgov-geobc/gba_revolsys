package com.revolsys.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.Resource;

public class ZipUtil {
  /**
   * Add the all the sub directories and files below the directory to the zip
   * output stream. The names of the files in the ZIP file will be relative to
   * the directory.
   *
   * @param zipOut The zip output stream to add the files to.
   * @param directory The directory containing the files.
   * @throws IOException
   * @throws IOException If an I/O error occurs.
   */
  public static void addDirectoryToZipFile(final ZipOutputStream zipOut, final File directory)
    throws IOException {
    addDirectoryToZipFile(zipOut, directory, directory);
  }

  /**
   * Add the all the sub directories and files below the directory to the zip
   * output stream. The names of the files in the ZIP file will be relative to
   * the baseDirectory.
   *
   * @param zipOut The zip output stream to add the files to.
   * @param baseDirectory The base directory files are relative to.
   * @param directory The directory containing the files.
   * @throws IOException If an I/O error occurs.
   */
  public static void addDirectoryToZipFile(final ZipOutputStream zipOut, final File baseDirectory,
    final File directory) throws IOException {
    final File[] files = directory.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (file.isDirectory()) {
          addDirectoryToZipFile(zipOut, baseDirectory, file);
        } else {
          final String zipEntryName = FileUtil.getRelativePath(baseDirectory, file);
          zipOut.putNextEntry(new ZipEntry(zipEntryName));
          final InputStream in = new FileInputStream(file);
          FileUtil.copy(in, zipOut);
          in.close();
        }
      }
    }
  }

  /**
   * Add the list of file names to the zip output stream. The names of the files
   * in the ZIP file will be relative to the baseDirectory.
   *
   * @param zipOut The zip output stream to add the files to.
   * @param baseDirectory The base directory files are relative to.
   * @param fileNames The list of file names to add.
   * @throws IOException If an I/O error occurs.
   */
  public static void addFilesToZipFile(final ZipOutputStream zipOut, final File baseDirectory,
    final String[] fileNames) throws IOException {
    for (final String fileName : fileNames) {
      final File file = new File(baseDirectory, fileName);
      if (file.isDirectory()) {
        addDirectoryToZipFile(zipOut, baseDirectory, file);
      } else {
        zipOut.putNextEntry(new ZipEntry(fileName));
        final InputStream in = new FileInputStream(file);
        FileUtil.copy(in, zipOut);
        in.close();
      }
    }
  }

  public static List<String> unzipFile(final File file, final File outputDirectory)
    throws IOException {
    final List<String> entryNames = new ArrayList<String>();
    final ZipFile zipFile = new ZipFile(file);
    for (final Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
      final ZipEntry entry = entries.nextElement();
      final String entryName = entry.getName();
      final File outputFile = new File(outputDirectory, entryName);
      outputFile.getParentFile().mkdirs();
      FileUtil.copy(zipFile.getInputStream(entry), outputFile, entry.getSize());
      entryNames.add(entryName);
    }
    zipFile.close();
    return entryNames;
  }

  public static File unzipFile(final Resource resource) throws IOException {
    String filename = resource.getFilename();
    while (filename.length() < 3) {
      filename += "x";
    }
    final File directory = FileUtil.createTempDirectory(filename, ".zip");
    final InputStream in = resource.getInputStream();
    final ZipInputStream zipIn = new ZipInputStream(in);
    try {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        final String entryName = entry.getName();
        final File outputFile = new File(directory, entryName);
        outputFile.getParentFile().mkdirs();
        if (entry.isDirectory()) {
          outputFile.mkdir();
        } else {
          FileUtil.copy(zipIn, outputFile);
        }
        zipIn.closeEntry();
      }
      FileUtil.closeSilent(zipIn);
      return directory;

    } catch (final IOException e) {
      FileUtil.closeSilent(zipIn);
      FileUtil.deleteDirectory(directory);
      throw e;
    }
  }

  public static void zipDirectory(final File zipFile, final File directory) {
    try {
      final OutputStream outputStream = new FileOutputStream(zipFile);
      zipDirectory(directory, outputStream);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create zip file:" + zipFile, e);
    }
  }

  public static void zipDirectory(final File directory, final OutputStream outputStream)
    throws IOException {
    final ZipOutputStream zipOut = new ZipOutputStream(outputStream);
    addDirectoryToZipFile(zipOut, directory, directory);
    zipOut.close();
  }

}
