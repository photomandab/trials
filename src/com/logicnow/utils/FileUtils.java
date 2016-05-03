package com.logicnow.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;

/**
 * Contains utility methods for reading/writing files while handling encodings and other issues.
 *
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
  /** Magic number for gzip files */
  private static final byte[] GZIP_MAGIC = new byte[] { (byte)0x1f, (byte)0x8b };

  /** The line separator equal to system property "line.separator". */
  public static final String LINE_SEP = System.getProperty("line.separator");

  /**
   * The UTF-8 encoding constant (equal to "UTF-8").
   */
  public static final String ENCODING_UTF_8 = "UTF-8";

  /**
   * Reads a file in the default encoding ENCODING_UTF_8.
   *
   * @param f file to read
   * @return the contents of the file
   * @throws IOException if an error occurs while reading a file
   */
  public static String readFileAsText(File f) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), ENCODING_UTF_8));
    try {
      return IOUtils.readAsText(reader);
    } finally {
      reader.close();
    }
  }

  /**
   * Renames a file, deleting destination and retrying if rename fails.
   * @param src the source file
   * @param dest the destination file
   * @throws IOException
   */
  public static void renameFile(File src, File dest) throws IOException {
    // Make sure parent directory of destination exists
    forceMkdir(dest.getParentFile());

    // If rename succeeds, we're done
    if (src.renameTo(dest))
      return;

    // handle if destination existing is blocking rename (Windows issue)
    if (dest.exists() && dest.isFile()) {
      if (dest.delete()) {
        moveFile(src, dest);
        return;
      } else {
        String message = String.format("Failed to rename '%s' -> '%s': Failed to remove destination.", src.getPath(), dest.getPath());
        throw new IOException(message);
      }
    } else if (dest.exists()) {
      String message = String.format("Failed to rename '%s' -> '%s': Destination exists and is not a file.", src.getPath(), dest.getPath());
      throw new IOException(message);
    } else {
      moveFile(src, dest);
    }

    // Unknown error (renameTo returned false)
    throw new IOException(String.format("Failed to rename '%s' -> '%s'", src.getPath(), dest.getPath()));
  }

  /**
   * Opens an InputStream for the possibly compressed specified file.
   *
   * <p>If the file is gzipped, it will be wrapped in a GZIPInputStream.</p>
   *
   * @param file the file to open.
   * @return an InputStream for reading <code>file</code>
   * @throws IOException if opening file fails.
   */
  public static InputStream openCompressedInputStream(File file) throws IOException {
    @SuppressWarnings("resource")
	final RandomAccessFile raf = new RandomAccessFile(file, "r");

    // Read magic number anyone, anyone
    final byte[] magic = new byte[2];
    int bytesRead = raf.read(magic);

    // Open the file input stream
    raf.seek(0);
    final InputStream is = new FileInputStream(raf.getFD());

    // Handle compressed file
    if (bytesRead == 2 && Arrays.equals(GZIP_MAGIC, magic)) {
      return new GZIPInputStream(is);
    } else {
      return is;
    }
  }

  /**
   * Opens an InputStream for the possibly compressed specified uri.
   *
   * <p>If the uri is gzipped, it will be wrapped in a GZIPInputStream.</p>
   *
   * @param uri
   * @return an InputStream for reading the <code>uri</code>
   * @throws IOException if opening file fails.
   */
  public static InputStream openCompressedInputStream(URI uri) throws IOException {
    InputStream is = uri.toURL().openStream();
    if (is.markSupported())
      is.mark(10);
    
    // Read magic number anyone, anyone
    final byte[] magic = new byte[2];
    int bytesRead = is.read(magic);

    if (is.markSupported())
      is.reset();
    else {
      is.close();
      is = uri.toURL().openStream();
    }
    
    // Handle compressed file
    if (bytesRead == 2 && Arrays.equals(GZIP_MAGIC, magic)) {
      return new GZIPInputStream(is);
    } else {
      return is;
    }
  }


  /**
   * Writes out the contents of a file in the default encoding ENCODING_UTF_8.
   *
   * @param file the file name to write to, parent directories are created if necessary
   * @param contents the content to write to the file
   * @throws IOException if an error occurs while writing the file
   */
  public static void writeFile(String file, String contents) throws IOException {
    File f = new File(file);
    writeFile(f, contents);
  }

  /**
   * Writes out the contents of a file in the default encoding ENCODING_UTF_8.
   *
   * @param f the file to write to, parent directories are created if necessary
   * @param contents the content to write to the file
   * @throws IOException if an error occurs while writing the file
   */
  public static void writeFile(File f, String contents) throws IOException {
    FileUtils.createDirectory(f.getParentFile());
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), ENCODING_UTF_8));
    try {
      writer.write(contents);
      writer.flush();
    } finally {
      writer.close();
    }
  }

  /**
   * Tries to delete a file or directory immediately. If that fails, zero's out any files and flag them for delete when the jvm exits.
   *
   * @param f file to delete
   * @throws IOException
   */
  public static void deleteDirectoryOrFile(File f) throws IOException {
    org.apache.commons.io.FileUtils.deleteDirectory(f);
  }

  /**
   * Tries to delete a file immediately. If that fails, flag it for delete when the JVM exits.
   *
   * @param f file to delete
   * @throws IOException
   * @return true if file was deleted or did not exist, false if scheduled for deletion
   */
  public static boolean deleteFile(File f) throws IOException {
    // first try nicely
    boolean b = f.delete();
    if (!b) {
      try {
        forceDelete(f);
        b = true;
      } catch (FileNotFoundException e) {
        b = true;
      } catch (IOException e) {
        f.deleteOnExit();
        b = false;
      }
    }
    return b;
  }

  /**
   * If the parent of <code>f</code> will be empty after this file is deleted, then delete both.
   * Returns parent directory if it was deleted
   * @param f
   * @throws IOException
   * @return the parent file if it was deleted, null otherwise
   */
  public static File deleteFileAndParentIfEmpty(File f) throws IOException {
    deleteFile(f);  // don't delete parent if there was an io exception deleting f.
    File parent = f.getParentFile();
    if (parent == null || !parent.exists()) return null;
    if (parent.list().length == 0) {
      deleteDirectory(parent);
      return parent;
    }
    return null;
  }
  /**
   * Creates the directory and any necessary parent directories.
   *
   * @param directory directory to create
   * @return handle to newly created directory
   * @throws IOException
   */
  public static File createDirectory(File directory) throws IOException {
    if (directory == null)
      return null;
    try {
      org.apache.commons.io.FileUtils.forceMkdir(directory);
    } catch( IOException e ){
      if( !directory.isDirectory() || !directory.exists() ){
        throw e;
      }
    }
    return directory;
  }

  /**
   * Creates the directory and any necessary parent directories.
   *
   * @param directory directory to create
   * @return handle to newly created directory
   */
  public static File createDirectory(String directory) throws IOException {
    File f = new File(directory);
    return createDirectory(f);
  }

  /** The default constructor. (Hidden). */
  protected FileUtils() {
    // do nothing for checkstyle
  }

  /**
   * Sorts files in alphabetical order.
   * @param files the array of file objects to sort
   * @return the sorted array
   */
  public static File[] sortFileByName(File[] files) {
    Arrays.sort(files, new FileNameComparator());
    return files;
  }

  /**
   * Compares file objects.
   */
  protected static class FileNameComparator implements Comparator<File>, Serializable {
    private static final long serialVersionUID = -8111728458013780791L;
    @Override
    public int compare(File f1, File f2) {
      return f1.getName().compareTo(f2.getName());
    }
  }

  /**
   * Copies a directory
   * @param src the source
   * @param dest the destination
   * @throws IOException
   */
  public static void copyDirectory(File src, File dest) throws IOException {
    org.apache.commons.io.FileUtils.copyDirectory(src, dest);
  }

  /**
   * Counts files in a directory
   * @param directory the directory
   * @param extensions any extensions to filter files by
   * @return the count
   * @throws IOException
   */
  public static int countFiles(File directory, String... extensions) throws IOException {
    return listFiles(directory, extensions, true).size();
  }

  private static final long KB = 1024;

  private static final long MB = KB * 1024;

  private static final long GB = MB * 1024;

  private static final long TB = GB * 1024;

  /**
   * Gets a "pretty" (user-friendly) size string for the size of a given file.
   * @param f the file
   * @return the size as a user-friendly string
   */
  public static String getPrettySize(File f) {
    long size = f.length();
    if (size > TB) {
      return format(size, TB) + "tb";
    } else if (size > GB) {
      return format(size, GB) + "gb";
    } else if (size > MB) {
      return format(size, MB) + "mb";
    } else if (size > KB) {
      return format(size, KB) + "kb";
    } else
      return size + "bytes";
  }

  private static String format(float f) {
    DecimalFormat df = new DecimalFormat("0.00");
    return df.format(f);
  }

  private static String format(long num, long den) {
    float f = (float) num / (float) den;
    return format(f);
  }

  /**
   * Gets the file extension, given the file path. The returned extension is normalized to lower case.
   *
   * @param path the file path
   * @param includeDot if true, the dot is included in the returned extension
   * @return the extension, or null if the path has no extension
   */
  public static String getFileExtension(final String path, final boolean includeDot) {
    String ext = null;
    if (!StringUtils.isEmpty(path)) {
      File file = new File(path);
      String fname = file.getName();
      int index = fname.lastIndexOf('.');
      if (index > 0) {
        ext = fname.substring((includeDot) ? index : index + 1, fname.length()).toLowerCase();
      }
    }
    return ext;
  }

  /**
   * Splits a filename into two constituent parts, the short name and the extension, if any.
   *
   * @param fileName
   * @return a string array whose 0-th element is the short name (with no extension) and the first element is the extension; if no extension is present, the first element is set to an empty string
   */
  public static String[] getFileNameParts(String fileName) {
    String[] parts = new String[2];

    int idx = fileName.lastIndexOf('.');
    if (idx == -1) {
      parts[0] = fileName;
      parts[1] = StringUtils.EMPTY;
    } else {
      parts[0] = fileName.substring(0, idx);
      parts[1] = fileName.substring(idx + 1);
    }

    return parts;
  }

  /**
   * Joins multiple sub-paths into a single path.
   * @param args the sub-paths
   * @return the path
   */
  public static String pathJoin(String... args) {
    if (args.length == 0) {
      return "";
    }

    String bp = args[0];
    for (int i = 1; i < args.length; i++) {
      bp = FilenameUtils.concat(bp, args[i]);
    }

    return bp;
  }

  /** Gets the size of a file or directory recursively walking all sub-directories. */
  public static long size(File file) {
    if (file.isFile())
      return file.length();
    File[] files = file.listFiles();
    long size = 0;
    if (files != null) {
      for (int i = 0; i < files.length; i++)
        size += size(files[i]);
    }
    return size;
  }

  /** Gets a Reader from a file in UTF-8. */
  public static Reader newFileReader(String filePath) throws FileNotFoundException {
    try {
      return new InputStreamReader(new FileInputStream(filePath), ENCODING_UTF_8);
    } catch (UnsupportedEncodingException e) {
      // we always have UTF-8
      return null;
    }
  }

  /**
   * Do everything possible to sync <code>file</code> to stable storage.
   */
  public static void sync(File file) throws IOException {
    sync(file, 5);
  }

  /**
   * Do everything possible to sync <code>file</code> to stable storage.
   */
  public static void sync(File file, int retries) throws IOException {
    IOException error = null;

    do {
      try {
        final RandomAccessFile fp = new RandomAccessFile(file, "rw");
        try {
          fp.getFD().sync();
        } finally {
          fp.close();
        }
        return; // All good
      } catch (IOException e) {
        // Remember the first exception
        if (error == null) {
          error = e;
        }
      }

      // Pause for 10 msec prior to trying again
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      // Decrement retry count
      --retries;
    } while (retries > -1);

    // Throw the exception
    throw error;
  }

  /**
   * Deletes the contents of a given directory if there are no files in the directory itself or in any of its descendant subdirectories.
   * @param dir the directory to clean
   * @throws IOException
   */
  public static boolean cleanDirectoryIfNoFiles(File dir) throws IOException {
    boolean cleaned = false;
    if (dir.isDirectory() && !hasFiles(dir)) {
      FileUtils.cleanDirectory(dir);
      cleaned = true;
    }
    return cleaned;
  }

  /**
   * Recursively walks the directory, deleting it if it contains no files or just empty sub-directories.
   * @param dir
   * @throws IOException
   */
  public static final void cleanEmptyDirectories(File dir) throws IOException {
    for (File f : dir.listFiles()) {
      if (f.isFile()) {
        // not an empty directory
        return;
      } else {
        cleanEmptyDirectories(f);
      }
      if (f.listFiles().length==0) {
        FileUtils.forceDelete(f);
      }
    }
  }

  // Recursively checks to see whether a given directory has any files (omitting any sub-directories from the count).
  private static boolean hasFiles(File dir) {
    boolean has = false;

    if (dir.isDirectory()) {
      File[] children = dir.listFiles();

      if (children != null) {
        for (int i = 0; i < children.length && !has; i++) {
          File f = children[i];
          if (f.isFile()) {
            has = true;
          } else if (f.isDirectory()) {
            has = hasFiles(f);
          }
        }
      }
    }

    return has;
  }

  /**
   * @param dir
   * @param file
   * @return true if file is contained within dir.  this is done by examining paths only
   */
  public static boolean directoryContainsFile(File dir, File file) {
    if (dir == null) return false;
    while (file != null) {
      if (dir.equals(file)) return true;
      file = file.getParentFile();
    }
    return false;
  }
  
}
