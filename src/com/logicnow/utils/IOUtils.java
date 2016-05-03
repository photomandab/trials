package com.logicnow.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Enumeration;

import org.apache.commons.io.FilenameUtils;

/**
 * Contains utilities for reading/writing streams.
 */
public class IOUtils {

  /** The 'Mac' OS family identifier. */
  public static final String MAC = "mac";

  /** The 'Linux' OS family identifier. */
  public static final String LINUX = "linux";

  /** The 'Windows' OS family identifier. */
  public static final String WINDOWS = "windows";

  /** The 'Solaris' OS family identifier. */
  public static final String SOLARIS = "sunos";

  /** 64 bit OS architecture identifier.*/
  public static final String X64 = "amd64";

  /** x86/64 bit OS architecture identifier (mac).*/
  public static final String X86_64 = "x86_64";

  /** 32 bit OS architecture identifier.*/
  public static final String X32 = "x86";

  /** 32 bit OS architecture identifier.*/
  public static final String I386 = "i386";

  // CheckStyle:Start:SuppressWarning ConstantName
  /** 64 bit OS architecture identifier.
   * @deprecated Use {@link IOUtils#X64} instead.
   */
  @Deprecated
  public static final String x64 = X64;

  /** 32 bit OS architecture identifier.
   * @deprecated Use {@link IOUtils#X32} instead.
   */
  @Deprecated
  public static final String x32 = X32;

  /** 32 bit OS architecture identifier.
   * @deprecated Use {@link IOUtils#I386} instead.
   */
  @Deprecated
  public static final String i386 = I386;
  // CheckStyle:End:SuppressWarning ConstantName


  /** The default encoding value (UTF-8). */
  public static final String DEFAULT_ENCODING = "UTF-8";
  public static final String ISO_ENCODING = "ISO-8859-1";

  private static String ipAddress = null;

  static {
    // need to do this once instead of for every call
    initIpAddress();
  }

  /**
   * Gets the contents of a reader as text.
   * WARNING: Does not close stream
   *
   * @param r the reader to read
   * @return String from Reader
   * @throws IOException if an error occurs reading
   */
  public static String readAsText(Reader r) throws IOException {
    return org.apache.commons.io.IOUtils.toString(r);
  }

  /**
   * Reads InputStream using the default encoding.
   * WARNING: Does not close stream
   *
   * @param in stream to read
   * @param encoding encoding to use
   * @return String from InputStream
   * @throws IOException if an error occurs
   */
  public static String readAsText(InputStream in, String encoding) throws IOException {
    Reader r = new InputStreamReader(in, encoding);
    return readAsText(r);
  }

  /**
   * Reads InputStream using the default encoding.
   * WARNING: Does not close stream
   *
   * @param in stream to read
   * @param charset encoding to use
   * @return String from InputStream
   * @throws IOException if an error occurs
   */
  public static String readAsText(InputStream in, Charset charset) throws IOException {
    Reader r = new InputStreamReader(in, charset);
    return readAsText(r);
  }

  /**
   * Reads InputStream using the default encoding.
   * WARNING: Does not close stream
   * @param in stream to read in default encoding
   * @return String from InputStream
   * @throws IOException if an error occurs
   */
  public static String readAsText(InputStream in) throws IOException {
    return readAsText(in, IOUtils.DEFAULT_ENCODING);
  }

  /**
   * Reads InputStream using the default encoding, trapping IOException and returning null.
   * WARNING: Does not close stream
   * @param in
   * @return the stream as a string or null if error was encountered reading the stream.
   */
  public static String readAsTextSafely(InputStream in) {
    try {
      return readAsText(in, IOUtils.DEFAULT_ENCODING);
    } catch (IOException io) {
      return null;
    }
  }
  /**
   * Reads the contents of an InputStream.
   * WARNING: Does not close stream
   *
   * @param is stream to read
   * @return byte array from InputStream
   * @throws IOException if an error occurs
   */
  public static byte[] readAsBytes(InputStream is) throws IOException {
    return org.apache.commons.io.IOUtils.toByteArray(is);
  }

  /**
   * Copies the contents from an OutputStream into an InputStream.  Both streams will be closed after the copy operation completes.
   * @param in input stream
   * @param out output stream
   * @throws IOException if error
   * @see <a href=http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html>Java I/O Performance</a>
   */
  public static void copyStream(InputStream in, OutputStream out) throws IOException {
    copyStream(in, out, true);
  }

  /**
   * Copies the contents from an OutputStream into an InputStream.
   * @param in input stream
   * @param out output stream
   * @param closeStreams if true, both streams will be closed after the copy operation completes successfully
   * @throws IOException if error
   * @see <a href=http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html>Java I/O Performance</a>
   */
  public static long copyStream(InputStream in, OutputStream out, boolean closeStreams) throws IOException {
    try {
      long size = 0;
      byte[] buffer = new byte[4096];
      int n = 0;
      while (true) {
        try {
          n = in.read(buffer);
        } catch (IOException ioe) {
          throw new IOException(String.format("Error reading from stream %s", in), ioe);
        }
        if (n == -1) {
          break;
        }
        out.write(buffer, 0, n);
        size = size + n;
      }
      return size;
    } finally {
      if (closeStreams) {
        in.close();
        out.close();
      }
    }
  }

  /**
   * Gets bytes from a String in the proper encoding.
   *
   * @param s string to get bytes of
   * @return byte array encoded in UTF-8, if string is null return an empty byte array
   */
  public static byte[] getBytes(String s) {
    if (s == null) return new byte[0];
    try {
      return s.getBytes(IOUtils.DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 not supported", e);
    }
  }

  /**
   * Gets String from a byte array in proper encoding.
   *
   * @param bytes byte array to get String from.  if bytes is null, returns null
   */
  public static String getString(byte[] bytes) {
    if (bytes == null || bytes.length == 0) return null;
    try {
      return new String(bytes, IOUtils.DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 not supported", e);
    }
  }

  /**
   * Writes out a String.
   *
   * @param s String to write
   * @param out Stream to write to in default encoding
   * @throws IOException if an error occurs while writing
   */
  public static void write(String s, OutputStream out) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(out, IOUtils.DEFAULT_ENCODING);
    writer.write(s);
    writer.flush();
    writer.close();
  }

  /** The default constructor (hidden). */
  protected IOUtils() {
    // do nothing
  }

  /**
   * Gets the ProcessID of the JVM from java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
   * @return the process ID of the JVM
   */
  public static int getPID() {
    String tmp = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    tmp = tmp.split("@")[0];
    return Integer.valueOf(tmp);
  }

  /**
   * Get the IP address of this node as a String.
   *
   * <p>NOTE: in the event that the default local host address is a loopback address,
   *    all non-local network interfaces will be searched for the first ipv4 address.
   *    This may still result in an address that is not externally available on the
   *    network. To avoid this, you must configure your host to use a static ip address.</p>
   */
  public static void initIpAddress() {
    try {
      final InetAddress local = InetAddress.getLocalHost();
       
      
      // If local is a loopback address, try to be smarter
      if (local.isLoopbackAddress()) {
        final Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
          final NetworkInterface iface = ifaces.nextElement();
          if (iface.getInetAddresses().hasMoreElements()) {
            final Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
              final InetAddress address = addresses.nextElement();
              if (address instanceof java.net.Inet4Address) {
                ipAddress = address.getHostAddress();
                return;
              }
            }
          }
        }
      }

      // Use local anyway if no non-local address exists
      ipAddress = local.getHostAddress();
    } catch (IOException e) {
      System.err.println("failed to get host address of localhost! " + e.getMessage());
      ipAddress = "localhost";
    }
  }

  public static String getIpAddress() {
    return ipAddress;
  }

  /**
   * Returns the 1st IP address that starts with the given string.
   * @param startsWith
   * @return
   * @throws UnknownHostException 
   * @throws SocketException 
   */
  public static String findIpAddress(String startsWith) throws UnknownHostException, SocketException {
    
    // find a better address
    Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
    while ( ni.hasMoreElements() ) {
      NetworkInterface iface = ni.nextElement();
      if (iface.getInetAddresses().hasMoreElements()) {
        final Enumeration<InetAddress> addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          final InetAddress address = addresses.nextElement();
          if (address instanceof java.net.Inet4Address && address.getHostAddress().startsWith(startsWith)) {
            return address.getHostAddress();
          }
        }
      }
    }
    
    return getIpAddress();
  }
  
  
  /** Determines is the local host is the same as the otherHost specified.
   * Checks to see if any of the IP address of the otherHost match any of the IPs of any of hte local machine's interfaces.
   */
  public static boolean isSameHost(String otherHost) throws Exception {
    try {
      InetAddress[] otherIps = InetAddress.getAllByName(otherHost);
      final Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
      while (ifaces.hasMoreElements()) {
        final NetworkInterface iface = ifaces.nextElement();
        if (iface.getInetAddresses().hasMoreElements()) {
          final Enumeration<InetAddress> addresses = iface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            final InetAddress address = addresses.nextElement();
            if (address instanceof java.net.Inet4Address) {
              for (InetAddress tmp : otherIps) {
                if (tmp.equals(address)) return true;
              }
            }
          }
        }
      }
      return false;
    } catch (IOException e) {
      throw new Exception("Failed to get host address of localhost!");
    }
  }

  /**
   * @param host
   * @return the IP address of <code>host</code> as a String or null if it could not be found.
   */
  public static String getIpAddress(String host) {
    try {
      return InetAddress.getByName(host).getHostAddress();
    } catch (UnknownHostException e) {
      System.err.println(String.format("Failed to find ip address of %s", host));
      return null;
    }
  }

  /**
   * Open a reader for a file with a specified encoding.
   */
  public static Reader openReader(File file, String encoding) throws IOException {
    return new InputStreamReader(new FileInputStream(file), encoding);
  }

  /**
   * Open a reader for a file with a specified encoding.
   */
  public static Reader openReader(URI uri, String encoding) throws IOException {
    URL url = uri.toURL();
    return new InputStreamReader(url.openStream(), encoding);
  }

  /**
   * Gets a nice clean OS name using system properties.
   * @return the OS name
   * @see #MAC
   * @see #LINUX
   * @see #WINDOWS
   * @see #SOLARIS
   */
  //CheckStyle:Start:SuppressWarning illegal pattern - kernel should get properties this way.
  public static String getOSFamily() {
    String tmp = System.getProperty("os.name").toLowerCase();
    if (tmp.indexOf(WINDOWS) >= 0) {
      tmp = WINDOWS;
    } else if (tmp.indexOf(LINUX) >= 0) {
      tmp = LINUX;
    } else if (tmp.indexOf(SOLARIS) >= 0) {
      tmp = SOLARIS;
    } else if (tmp.indexOf(MAC) >= 0) {
      tmp = MAC;
    } else {
      throw new RuntimeException("Patform could not be detected for setting 'os' variable");
    }
    return tmp;
  }

  /**
   * Gets a nice clean OS cpu architecture name using system properties
   * @return the CPU Architecture name, either 32 or 64
   */
  public static String getOSArchitecture() {
    String tmp = System.getProperty("os.arch").toLowerCase();
    if ((tmp.indexOf(X64) >= 0) || (tmp.indexOf(IOUtils.X86_64) >= 0)) {
      tmp = "64";
    } else if (tmp.indexOf(X32) >= 0) {
      tmp = "32";
    } else if (tmp.equals(I386)) {
      tmp = "32";
    } else {
      throw new RuntimeException("CPU Architecture could not be detected for setting 'os' variable");
    }
    return tmp;
  }
  //CheckStyle:End:SuppressWarning illegal pattern

  /**
   * Get the binary filename extension for the current platform
   * @return the binary extension (ie .exe for windows)
   */
  public static String getBinaryExtension(){
    if( getOSFamily().equals(WINDOWS) ){
      return ".exe";
    }
    return "";
  }

  /**
   * Calls {@link #isPortInUse(int, int, int)} with a retry wait time of 0
   * @param port socket port number
   * @param numRetries number of attempts to use that socket
   * @return true if port is in use, false otherwise
   */
  public static boolean isPortInUse(int port, int numRetries) {
    return isPortInUse(port, numRetries, 0);
  }

  /**
   * Checks to see if a port is in use with a given number of loops (numRetries)
   * waiting retryWaitTimeInMilliseconds for each loop.
   * @param port socket port number
   * @param numRetries number of attempts to use that socket
   * @param retryWaitTimeInMilliseconds time in milliseconds to sleep between retries
   * @return true if port is in use, false otherwise
   */
  public static boolean isPortInUse(int port, int numRetries, int retryWaitTimeInMilliseconds) {
    numRetries++;  // +1 for the first attempt
    if (numRetries < 1) throw new IllegalArgumentException("number of retries must be > 0");
    while (numRetries-- > 0) {
      try {
        ServerSocket tmp = new ServerSocket(port);
        tmp.close();
        tmp = null;
        return false;
      } catch (IOException ioe) {  // we get an IOException if the port is in use.
        if (retryWaitTimeInMilliseconds > 0) {
          try {
            Thread.sleep(retryWaitTimeInMilliseconds);
          } catch (InterruptedException e) {
            // ignore
          }
        }
        continue;
      }
    }
    return true;
  }

  /** Make path separators consistent and correct for the local platform. */
  public static String fixPathSeparators(String path) {
    if (IOUtils.WINDOWS.equals(IOUtils.getOSFamily())) {
      path = path.replaceAll("/", "\\\\");
    } else {
      path = path.replaceAll("\\\\", "/");
    }
    return path;
  }

  /**
   * <p>Creates an InputStream which reads data written by the {@link OutputStreamConsumer}.  An OutputStreamConsumer is an interface
   * which provides a single write method that takes an OutputStream.</p>
   *
   * <h4>Sample use</h4>
   * <pre class="code-java">
   * {@literal
   * class1.read(IOUtils.toInputStream(new OutputStreamConsumer() {
   *     public void write(OutputStream os) throws IOException {
   *     class2.write(os);
   *   }
   * }));
   * }</pre>
   *
   * @param ss the output stream consumer to write to.
   * @return an InputStream which reads data written by the OutputStreamConsumer.
   */
  public static InputStream toInputStream(final OutputStreamConsumer ss) throws IOException {
    final ExceptionPassingPipedInputStream in = new ExceptionPassingPipedInputStream();
    final PipedOutputStream out = new PipedOutputStream(in);
    Thread t = new Thread(
        new Runnable(){
          @Override
          public void run(){
            try {
              ss.write(out);
            } catch (IOException e) {
              in.setException(e);
            }
          }
        }
    );
    t.setName(String.format("toInputStream-%d",t.getId()));
    t.start();
    return in;
  }

  /**
   * A class used to pass IOExceptions generated while writing to an associated output stream
   * to the reader.
   */
  private static class ExceptionPassingPipedInputStream extends PipedInputStream {
    private IOException ex = null;

    @Override
    public synchronized int available() throws IOException {
      if (ex!=null) throwException();
      return super.available();
    }
    @Override
    public synchronized int read() throws IOException {
      if (ex!=null) throwException();
      return super.read();
    }
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
      if (ex!=null) throwException();
      return super.read(b, off, len);
    }
    @Override
    public void close() throws IOException {
      super.close();
      throwException();
    }

    public synchronized void setException(IOException e) {
      ex = e;
    }

    public synchronized void throwException() throws IOException {
      if (ex == null) return;
      try { throw newIOException(ex); }
      finally { ex = null; }
    }

  }

  /** Used for copying an OutputStream into an InputStream.
   * @see IOUtils#toInputStream(OutputStreamConsumer)
   * */
  public static interface OutputStreamConsumer {
    /** Write contents to OutputStream. */
    void write(OutputStream s) throws IOException;
  }

  /** Creates a new IOException and sets the cause to be passed in exception.
   * This is used as jre1.5 does not have a utility constructor to handle this.
   */
  public static final IOException newIOException(Exception ex) {
    return newIOException(null, ex);
  }

  /** Creates a new IOException and sets the cause to be passed in exception.
   * This is used as jre1.5 does not have a utility constructor to handle this.
   */
  public static final IOException newIOException(String msg, Exception ex) {
    IOException ioe = new IOException(msg);
    ioe.initCause(ex);
    return ioe;
  }


  public static final Exception close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException ioe) {
        return ioe;
      }
    }
    return null;
  }

  /** Checks to see if a specified port is in use on the local host.
   *
   * @return true if the port is in use or there was some other exception testing the port, false if port is not in use.
   * */
  public static boolean checkIfPortInUse(int port) {
    ServerSocket tmp = null;
    try {
      tmp = new ServerSocket(port);
      return false;
    } catch (IOException ioe) {
      return true;
    } finally {
      //argh! ServerSocket does not implement Closeable
      if (tmp!=null) {
        try {
          tmp.close();
        } catch (IOException ioe) {
          // no op
          tmp = null;
        }
      }
    }
  }

  /**
   * Returns a URI representing the file. If <code>file</code> is already a URI, it is returned as a URI
   * @param file
   * @return a corresponding URI
   */
  public static URI toUri(String file) {
    URI uri = null;
    try {
      if (file.matches("^[a-zA-Z]*:/.*")) {
        uri = new URI(file);  // if already a uri
        if (uri.getScheme() == null) uri = null;  // something about the path confused the uri parser.  but scheme should never be null if it is a uri.
        if (uri != null && uri.getScheme().length() == 1) uri = null; //probably a windows drive letter.  TODO: we could check to see if a url resolver is registered and otherwise reject.  
      }
    } catch (URISyntaxException e) {
      uri = null;
    }
    if (uri == null) {
      file = FilenameUtils.normalize(file);
      uri = new File(file).toURI();
    }
    return uri;
  }

}
