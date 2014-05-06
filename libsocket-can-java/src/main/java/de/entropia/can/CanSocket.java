package de.entropia.can;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardOpenOption;
//import java.nio.file.attribute.FileAttribute;
//import java.nio.file.attribute.PosixFilePermission;
//import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
//import java.util.Objects;
import java.util.Set;

public final class CanSocket implements Closeable {

	static {
		final String LIB_JNI_SOCKETCAN = "jni_socketcan";
		try {
			System.loadLibrary(LIB_JNI_SOCKETCAN);
		} catch (final UnsatisfiedLinkError e) {
			try {
				loadLibFromJar(LIB_JNI_SOCKETCAN);
			} catch (final IOException _e) {
				throw new UnsatisfiedLinkError(LIB_JNI_SOCKETCAN);
			}
		}
	}

	private static void copyStream(final InputStream in,
			final OutputStream out) throws IOException {
		final int BYTE_BUFFER_SIZE = 0x1000;
		final byte[] buffer = new byte[BYTE_BUFFER_SIZE];
		for (int len; (len = in.read(buffer)) != -1;) {
			out.write(buffer, 0, len);
		}
	}

	//    private static void loadLibFromJar(final String libName)
	//            throws IOException {
	//        Objects.requireNonNull(libName);
	//        final String fileName = "/lib/lib" + libName + ".so";
	//        final FileAttribute<Set<PosixFilePermission>> permissions =
	//                PosixFilePermissions.asFileAttributei(
	//                        PosixFilePermissions.fromString("rw-------"));
	//        final Path tempSo = Files.createTempFile(CanSocket.class.getName(),
	//                ".so", permissions);
	//        try {
	//        	final InputStream libstream = CanSocket.class.getResourceAsStream(fileName);
	//        	if (libstream == null) {
	//        		throw new FileNotFoundException("jar:*!" + fileName);
	//        	}
	//        	final OutputStream fout = Files.newOutputStream(tempSo,
	//        			StandardOpenOption.WRITE,
	//        			StandardOpenOption.TRUNCATE_EXISTING);        		
	//        	copyStream(libstream, fout);
	//            System.load(tempSo.toString());
	//    	} catch (Exception e) {
	//    		// TODO Auto-generated catch block
	//    		e.printStackTrace();
	//    	} finally {
	//            Files.delete(tempSo);
	//        }
	//    }

	/**
	 * Loads library from current JAR archive
	 * 
	 * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after exiting.
	 * Method uses String as filename because the pathname is "abstract", not system-dependent.
	 * 
	 * @param filename The filename inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
	 * @throws IOException If temporary file creation or read/write operation fails
	 * @throws IllegalArgumentException If source file (param path) does not exist
	 * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters (restriction of {@see File#createTempFile(java.lang.String, java.lang.String)}).
	 */
	public static void loadLibFromJar(String fileName) throws IOException {

		String osType = System.getProperty("os.arch");
		final String path;
		if(osType.matches("arm")){
			path = "/de/entropia/can/lib/linux-arm/lib" + fileName + ".so";     
		}
		else{
			path = "/de/entropia/can/lib/linux-32/lib" + fileName + ".so";     
		}

		// Obtain filename from path
		String[] parts = path.split("/");
		String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

		// Split filename to prexif and suffix (extension)
		String prefix = "";
		String suffix = null;
		if (filename != null) {
			parts = filename.split("\\.", 2);
			prefix = parts[0];
			suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null; // Thanks, davs! :-)
		}

		// Check if the filename is okay
		if (filename == null || prefix.length() < 3) {
			throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
		}

		// Prepare temporary file
		File temp = File.createTempFile(prefix, suffix);
		temp.deleteOnExit();

		if (!temp.exists()) {
			throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
		}

		// Prepare buffer for data copying
		byte[] buffer = new byte[1024];
		int readBytes;

		// Open and check input stream
		InputStream is = CanSocket.class.getResourceAsStream(path);
		if (is == null) {
			throw new FileNotFoundException("File " + path + " was not found inside JAR.");
		}

		// Open output stream and copy data between source file in JAR and the temporary file
		OutputStream os = new FileOutputStream(temp);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} finally {
			// If read/write fails, close streams safely before throwing an exception
			os.close();
			is.close();
		}

		// Finally, load the library
		System.load(temp.getAbsolutePath());
	}

	public static final CanInterface CAN_ALL_INTERFACES = new CanInterface(0);

	private static native int _getCANID_SFF(final int canid);
	private static native int _getCANID_EFF(final int canid);
	private static native int _getCANID_ERR(final int canid);

	private static native boolean _isSetEFFSFF(final int canid);
	private static native boolean _isSetRTR(final int canid);
	private static native boolean _isSetERR(final int canid);

	private static native int _setEFFSFF(final int canid);
	private static native int _setRTR(final int canid);
	private static native int _setERR(final int canid);

	private static native int _clearEFFSFF(final int canid);
	private static native int _clearRTR(final int canid);
	private static native int _clearERR(final int canid);

	private static native int _openSocketRAW() throws IOException;
	private static native int _openSocketBCM() throws IOException;
	private static native void _close(final int fd) throws IOException;

	private static native int _fetchInterfaceMtu(final int fd,
			final String ifName) throws IOException;
	private static native int _fetch_CAN_MTU();
	private static native int _fetch_CAN_FD_MTU();

	private static native int _discoverInterfaceIndex(final int fd,
			final String ifName) throws IOException;
	private static native String _discoverInterfaceName(final int fd,
			final int ifIndex) throws IOException;

	private static native void _bindToSocket(final int fd,
			final int ifId) throws IOException;

	private static native CanFrame _recvFrame(final int fd) throws IOException;
	private static native void _sendFrame(final int fd, final int canif,
			final int canid, final byte[] data) throws IOException;

	public static final int CAN_MTU = _fetch_CAN_MTU();
	public static final int CAN_FD_MTU = _fetch_CAN_FD_MTU();

	private static native int _fetch_CAN_RAW_FILTER();
	private static native int _fetch_CAN_RAW_ERR_FILTER();
	private static native int _fetch_CAN_RAW_LOOPBACK();
	private static native int _fetch_CAN_RAW_RECV_OWN_MSGS();
	private static native int _fetch_CAN_RAW_FD_FRAMES();

	private static final int CAN_RAW_FILTER = _fetch_CAN_RAW_FILTER();
	private static final int CAN_RAW_ERR_FILTER = _fetch_CAN_RAW_ERR_FILTER();
	private static final int CAN_RAW_LOOPBACK = _fetch_CAN_RAW_LOOPBACK();
	private static final int CAN_RAW_RECV_OWN_MSGS = _fetch_CAN_RAW_RECV_OWN_MSGS();
	private static final int CAN_RAW_FD_FRAMES = _fetch_CAN_RAW_FD_FRAMES();

	private static native void _setsockopt(final int fd, final int op,
			final int stat) throws IOException;
	private static native int _getsockopt(final int fd, final int op)
			throws IOException;
	private static native void _setCanFilter(final int fd, final int id, final int mask)
			throws IOException;

	public final static class CanId implements Cloneable {
		private int _canId = 0;

		public static enum StatusBits {
			ERR, EFFSFF, RTR
		}

		public CanId(final int address) {
			_canId = address;
		}

		public boolean isSetEFFSFF() {
			return _isSetEFFSFF(_canId);
		}

		public boolean isSetRTR() {
			return _isSetRTR(_canId);
		}

		public boolean isSetERR() {
			return _isSetERR(_canId);
		}

		public CanId setEFFSFF() {
			_canId = _setEFFSFF(_canId);
			return this;
		}

		public CanId setRTR() {
			_canId = _setRTR(_canId);
			return this;
		}

		public CanId setERR() {
			_canId = _setERR(_canId);
			return this;
		}

		public CanId clearEFFSFF() {
			_canId = _clearEFFSFF(_canId);
			return this;
		}

		public CanId clearRTR() {
			_canId = _clearRTR(_canId);
			return this;
		}

		public CanId clearERR() {
			_canId = _clearERR(_canId);
			return this;
		}

		public int getCanId_SFF() {
			return _getCANID_SFF(_canId);
		}

		public int getCanId_EFF() {
			return _getCANID_EFF(_canId);
		}

		public int getCanId_ERR() {
			return _getCANID_ERR(_canId);
		}

		@Override
		protected Object clone() {
			return new CanId(_canId);
		}

		private Set<StatusBits> _inferStatusBits() {
			final EnumSet<StatusBits> bits = EnumSet.noneOf(StatusBits.class);
			if (isSetERR()) {
				bits.add(StatusBits.ERR);
			}
			if (isSetEFFSFF()) {
				bits.add(StatusBits.EFFSFF);
			}
			if (isSetRTR()) {
				bits.add(StatusBits.RTR);
			}
			return Collections.unmodifiableSet(bits);
		}

		@Override
		public String toString() {
			return "CanId [canId=" + (isSetEFFSFF()
					? getCanId_EFF() : getCanId_SFF())
					+ "flags=" + _inferStatusBits() + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + _canId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CanId other = (CanId) obj;
			if (_canId != other._canId)
				return false;
			return true;
		}
	}

	public final static class CanInterface implements Cloneable {
		private final int _ifIndex;
		private String _ifName;

		public CanInterface(final CanSocket socket, final String ifName)
				throws IOException {
			this._ifIndex = _discoverInterfaceIndex(socket._fd, ifName);
			this._ifName = ifName;
		}

		private CanInterface(int ifIndex, String ifName) {
			this._ifIndex = ifIndex;
			this._ifName = ifName;
		}

		private CanInterface(int ifIndex) {
			this(ifIndex, null);
		}

		public int getInterfaceIndex() {
			return _ifIndex;
		}

		@Override
		public String toString() {
			return "CanInterface [_ifIndex=" + _ifIndex + ", _ifName="
					+ _ifName + "]";
		}

		public String getIfName() {
			return _ifName;
		}

		public String resolveIfName(final CanSocket socket) {
			if (_ifName == null) {
				try {
					_ifName = _discoverInterfaceName(socket._fd, _ifIndex);
				} catch (IOException e) { /* EMPTY */ }
			}
			return _ifName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + _ifIndex;
			result = prime * result
					+ ((_ifName == null) ? 0 : _ifName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CanInterface other = (CanInterface) obj;
			if (_ifIndex != other._ifIndex)
				return false;
			if (_ifName == null) {
				if (other._ifName != null)
					return false;
			} else if (!_ifName.equals(other._ifName))
				return false;
			return true;
		}

		@Override
		protected Object clone() {
			return new CanInterface(_ifIndex, _ifName);
		}
	}

	public final static class CanFrame implements Cloneable {
		private final CanInterface canIf;
		private final CanId canId;
		private final byte[] data;

		public CanFrame(final CanInterface canIf, final CanId canId,
				byte[] data) {
			this.canIf = canIf;
			this.canId = canId;
			this.data = data;
		}

		/* this constructor is used in native code */
		@SuppressWarnings("unused")
		private CanFrame(int canIf, int canid, byte[] data) {
			if (data.length > 8) {
				throw new IllegalArgumentException();
			}
			this.canIf = new CanInterface(canIf);
			this.canId = new CanId(canid);
			this.data = data;
		}

		public CanId getCanId() {
			return canId;
		}

		public byte[] getData() {
			return data;
		}

		public CanInterface getCanInterfacae() {
			return canIf;
		}

		@Override
		public String toString() {
			return "CanFrame [canIf=" + canIf + ", canId=" + canId + ", data="
					+ Arrays.toString(data) + "]";
		}

		@Override
		protected Object clone() {
			return new CanFrame(canIf, (CanId)canId.clone(),
					Arrays.copyOf(data, data.length));
		}
	}

    public final static class CanFilter implements Cloneable {
        private int _can_id;
        private int _can_mask;
        
        private CanFilter(int can_id, int can_mask) {
            this.set_can_id(can_id);
            this.set_can_mask(can_mask);
        }

		public int get_can_id() {
			return _can_id;
		}

		public void set_can_id(int _can_id) {
			this._can_id = _can_id;
		}

		public int get_can_mask() {
			return _can_mask;
		}

		public void set_can_mask(int _can_mask) {
			this._can_mask = _can_mask;
		}
    }
    
	public static enum Mode {
		RAW, BCM
	}

	private final int _fd;
	private final Mode _mode;
	private CanInterface _boundTo;

	public CanSocket(Mode mode) throws IOException {
		switch (mode) {
		case BCM:
			_fd = _openSocketBCM();
			break;
		case RAW:
			_fd = _openSocketRAW();
			break;
		default:
			throw new IllegalStateException("unkown mode " + mode);
		}
		this._mode = mode;
	}

	public void bind(CanInterface canInterface) throws IOException {
		_bindToSocket(_fd, canInterface._ifIndex);
		this._boundTo = canInterface;
	}

	public void send(CanFrame frame) throws IOException {
		_sendFrame(_fd, frame.canIf._ifIndex, frame.canId._canId, frame.data);
	}

	public CanFrame recv() throws IOException {
		return _recvFrame(_fd);
	}

	@Override
	public void close() throws IOException {
		_close(_fd);
	}

	public int getMtu(final String canif) throws IOException {
		return _fetchInterfaceMtu(_fd, canif);
	}

	public void setLoopbackMode(final boolean on) throws IOException {
		_setsockopt(_fd, CAN_RAW_LOOPBACK, on ? 1 : 0);
	}

	public boolean getLoopbackMode() throws IOException {
		return _getsockopt(_fd, CAN_RAW_LOOPBACK) == 1;
	}

	public void setRecvOwnMsgsMode(final boolean on) throws IOException {
		_setsockopt(_fd, CAN_RAW_RECV_OWN_MSGS, on ? 1 : 0);
	}

	public boolean getRecvOwnMsgsMode() throws IOException {
		return _getsockopt(_fd, CAN_RAW_RECV_OWN_MSGS) == 1;
	}
	
	public void setCanFilter(int can_id, int can_mask) throws IOException {
		_setCanFilter(_fd, can_id, can_mask);
	}

}
