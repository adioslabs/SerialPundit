/**
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;

import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUnexpectedException;

/**
 * <p>This class is an interface between java and native shared library.</p>
 */
public final class SerialComFTDID2XXJNIBridge {

	/**
	 * <p>Allocates a new SerialComFTDID2XXJNIBridge object.</p>
	 */
	public SerialComFTDID2XXJNIBridge() {
	}

	/**
	 * <p>Extract native library from jar in a working directory and load it. Also load vendor's library.</p>
	 *  
	 * @param libDirectory directory in which native library will be extracted and vendor library will be found
	 * @param vlibName name of vendor library to load and link
	 * @param cpuArch architecture of CPU this library is running on
	 * @param osType operating system this library is running on
	 * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties
	 * @throws SerialComUnexpectedException if a critical java system property is null in system
	 * @throws SecurityException if any java system property can not be accessed
	 * @throws FileNotFoundException if the vendor library file is not found
	 * @throws SerialComLoadException if any file system related issue occurs
	 * @throws UnsatisfiedLinkError if loading/linking shared library fails
	 */
	public static boolean loadNativeLibrary(File libDirectory, String vlibName, int cpuArch, int osType, 
			SerialComSystemProperty serialComSystemProperty) throws UnsatisfiedLinkError, 
			SerialComLoadException, SerialComUnexpectedException, SecurityException, FileNotFoundException {
		String libToExtractFromJar = null;
		File libFile = null;
		File vlibFile = null;
		InputStream input = null;
		FileOutputStream output = null;
		String fileSeparator = null;

		fileSeparator = serialComSystemProperty.getfileSeparator();
		if(fileSeparator == null) {
			throw new SerialComUnexpectedException("The file.separator java system property is null in the system !");
		}

		/* Find the native library that will be extracted based on arch and os type */
		if(cpuArch == SerialComManager.ARCH_AMD64) {
			switch(osType) {
			case SerialComManager.OS_WINDOWS:
				libToExtractFromJar = "win_d2xx_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dll";
				break;
			case SerialComManager.OS_LINUX:
				libToExtractFromJar = "linux_d2xx_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.so";
				break;
			case SerialComManager.OS_MAC_OS_X:
				libToExtractFromJar = "mac_d2xx_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dylib";
			default:
			}
		}else if(cpuArch == SerialComManager.ARCH_X86) {
			switch(osType) {
			case SerialComManager.OS_WINDOWS:
				libToExtractFromJar = "win_d2xx_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dll";
				break;
			case SerialComManager.OS_LINUX:
				libToExtractFromJar = "linux_d2xx_" + SerialComManager.JAVA_LIB_VERSION + "_x86.so";
				break;
			case SerialComManager.OS_MAC_OS_X:
				libToExtractFromJar = "mac_d2xx_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dylib";
			default:
			}
		}else {
		}

		/* Extract shared library from jar into working directory */
		try {
			libFile = new File(libDirectory.getAbsolutePath() + fileSeparator + libToExtractFromJar);
			input = SerialComPortJNIBridge.class.getResourceAsStream("/vendor-libs/" + libToExtractFromJar);
			output = new FileOutputStream(libFile);
			if(input != null) {
				int read;
				byte[] buffer = new byte[4096];
				while((read = input.read(buffer)) != -1){
					output.write(buffer, 0, read);
				}
				output.flush();
				output.close();
				output = null;
				if(!((libFile != null) && libFile.exists() && libFile.isFile())) {
					throw new SerialComLoadException("Can not extract native shared library from scm-x.x.x.jar file !");
				}
			}else {
				throw new SerialComLoadException("Can not get shared library resource as stream from scm-x.x.x.jar file using class loader !");
			}
		} catch (Exception e) {
			throw (SerialComLoadException) new SerialComLoadException(libFile.toString()).initCause(e);
		} finally {
			try {
				if(output != null) {
					output.close();
				}
			} catch (Exception e) {
				// ignore
			}
			try {
				if(input != null) {
					input.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}

		// load libraries in reverse order of dependencies
		try {
			// vendor supplied shared library
			vlibFile = new File(libDirectory.getAbsolutePath() + fileSeparator + vlibName);
			System.load(vlibFile.toString());
		} catch (Exception e) {
			throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + vlibFile.toString() + " native library !").initCause(e);
		}
		try {
			// scm JNI glue shared library
			System.load(libFile.toString());
		} catch (Exception e) {
			throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + libFile.toString() + " native library !").initCause(e);
		}

		return true;
	}

	// D2XX Classic Functions
	public native int setVidPid(int vid, int pid);
	public native int[] getVidPid();
	public native int createDeviceInfoList();
	public native String[] getDeviceInfoList(int numOfDevices);
	public native String[] getDeviceInfoDetail(int index);
	public native String[] listDevices(int pvArg1, int dwFlags);
	public native long open(int index);
	public native long openEx(String serialOrDescription, long locationId, int dwFlags);
	public native int close(long handle);
	public native int read(long handle, byte[] buffer, int numOfBytesToRead);
	public native int write(long handle, byte[] buffer, int numOfBytesToWrite);
	public native int setBaudRate(long handle, int baudRate);
	public native int setDivisor(long handle, int divisor);
	public native int setDataCharacteristics(long handle, int dataBits, int stopBits, int parity);
	public native int setTimeouts(long handle, long readTimeOut, long writeTimeOut);
	public native int setFlowControl(long handle, int flctrl, char xon, char xoff);
	public native int setDTR(long handle);
	public native int clearDTR(long handle);
	public native int setRTS(long handle);
	public native int clearRTS(long handle);
	public native int getModemStatus(long handle);
	public native int getQueueStatus(long handle);
	public native String[] getDeviceInfo(long handle);
	public native long getDriverVersion(long handle);
	public native long getLibraryVersion();
	public native long getComPortNumber(long handle);
	public native long[] getStatus(long handle);
	// TODO FT_SetEventNotification
	public native int setChars(long handle, char eventChar, char eventEnable, char errorChar, char errorEnable);
	public native int setBreakOn(long handle);
	public native int setBreakOff(long handle);
	public native int purge(long handle, boolean purgeTxBuffer, boolean purgeRxBuffer);
	public native int resetDevice(long handle);
	public native int resetPort(long handle);
	public native int cyclePort(long handle);
	public native int rescan();
	public native int reload(int vid, int pid);
	public native int setResetPipeRetryCount(long handle, int count);
	public native int stopInTask(long handle);
	public native int restartInTask(long handle);
	public native int setDeadmanTimeout(long handle, int count);
	
	// EEPROM Programming Interface Functions

	// Extended API Functions

	// FT-Win32 API Functions
}





















