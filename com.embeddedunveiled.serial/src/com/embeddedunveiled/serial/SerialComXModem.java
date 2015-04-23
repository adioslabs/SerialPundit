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

package com.embeddedunveiled.serial;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 */
public final class SerialComXModem {
	
	private static final byte SOH = 0x01; // Start of header byteacter
	private static final byte EOT = 0x04; // End-of-transmission byteacter
	private static final byte ACK = 0x06; // Acknowledge byteacter
	private static final byte NAK = 0x15; // Negative-acknowledge byteacter
	private static final byte SUB = 0x1A; // Substitute/CTRL-Z
		
	private SerialComManager scm = null;
	private long handle = 0;
	private File fileToSend = null;
	
	private int blockNumber = -1;
	private byte[] block = new byte[132];       // 132 bytes xmodem block/packet
	private BufferedInputStream inStream = null;
	private boolean noMoreData = false;
	
	
	/**
	 * @param scm SerialComManager instance associated with this handle
	 * @param handle of the port on which file is to be sent
	 * @param fileToSend File instance representing file to be sent
	 */
	public SerialComXModem(SerialComManager scm, long handle, File fileToSend) {
		this.scm = scm;
		this.handle = handle;
		this.fileToSend = fileToSend;
	}

	/**
	 * <p>For internal use only.</p>
	 * <p>Represents actions to execute in state machine to implement xmodem protocol.</p>
	 */
	public boolean sendFileX() throws SecurityException, IOException, SerialComException {
		
		// Finite state machine
		final int WAITNAK = 0;
		final int BEGINSEND = 1;
		final int WAITACK = 2;
		final int RESEND = 3;
		final int SENDNEXT = 4;
		final int ENDTX = 5;
		final int ABORT = 6;
		
		boolean nakReceived = false;
		boolean eotAckReceptionTimerInitialized = false;
		String errMsg = null;
		int retryCount = 0;
		int state = -1;
		byte[] data = new byte[512];
		long responseWaitTimeOut = 0;
		long eotAckWaitTimeOutValue = 0;
		
		inStream = new BufferedInputStream(new FileInputStream(fileToSend));
		state = WAITNAK;
		
		while(true) {
			switch(state) {
				case WAITNAK:
					responseWaitTimeOut = System.currentTimeMillis() + 60000;
					while(nakReceived != true) {
						try {
							data = scm.readBytes(handle);
						} catch (SerialComException exp) {
							inStream.close();
							throw exp;
						}
						
						if(data.length > 0) {
							for(int x=0; x < data.length; x++) {
								if(NAK == data[x]) {
									nakReceived = true;
									state = BEGINSEND;
									break;
								}
							}
						}else {
							try {
								Thread.sleep(800);  // delay before next attempt to check NAK arrival
							} catch (InterruptedException e) {
							}
							// abort if timedout while waiting for NAK character
							if((nakReceived != true) && (System.currentTimeMillis() >= responseWaitTimeOut)) {
								errMsg = SerialComErrorMapper.ERR_TIMEOUT_RECEIVER_CONNECT;
								state = ABORT;
								break;
							}
						}
					}
					break;
				case BEGINSEND:
					blockNumber = 1; // Block numbering starts with 1 for the first block sent, not 0.
					assembleBlock();
					try {
						scm.writeBytes(handle, block);
					} catch (SerialComException exp) {
						inStream.close();
						throw exp;
					}
					state = WAITACK;
					break;
				case RESEND:
					if(retryCount > 10) {
						errMsg = SerialComErrorMapper.ERR_MAX_RETRY_REACHED;
						state = ABORT;
						break;
					}
					try {
						scm.writeBytes(handle, block);
					} catch (SerialComException exp) {
						inStream.close();
						throw exp;
					}
					state = WAITACK;
					break;
				case WAITACK:
					responseWaitTimeOut = System.currentTimeMillis() + 60000; // 1 minute
					while(true) {
						// delay before next attempt to read from serial port
						try {
							if(noMoreData != true) {
								Thread.sleep(150);
							}else {
								Thread.sleep(1500);
							}
						} catch (InterruptedException e) {
						}
						
						// try to read data from serial port
						try {
							data = scm.readBytes(handle);
						} catch (SerialComException exp) {
							inStream.close();
							throw exp;
						}
						
						/* if data received process it. if long timeout occurred abort otherwise retry reading from serial port.
						 * if nothing
						 */
						if(data.length > 0) {
							break;
						}else {
							if(noMoreData == true) {
								state = ENDTX;
								break;
							}
							if(System.currentTimeMillis() >= responseWaitTimeOut) {
								if(noMoreData == true) {
									errMsg = SerialComErrorMapper.ERR_TIMEOUT_ACKNOWLEDGE_EOT;
								}else {
									errMsg = SerialComErrorMapper.ERR_TIMEOUT_ACKNOWLEDGE_BLOCK;
								}
								state = ABORT;
								break;
							}
						}
					}
					
					if((state != ABORT) && (state != ENDTX)) {
						if(noMoreData != true) {
							if(data[0] == ACK) {
								state = SENDNEXT;
							}else if(data[0] == NAK) {
								retryCount++;
								state = RESEND;
							}else{
								errMsg = SerialComErrorMapper.ERR_KNOWN_ERROR_OCCURED;
								state = ABORT;
							}
						}else {
							if(data[0] == ACK) {
								inStream.close();
								return true; // successfully sent file, let's go back home happily
							}else{
								if(System.currentTimeMillis() >= eotAckWaitTimeOutValue) {
									errMsg = SerialComErrorMapper.ERR_TIMEOUT_ACKNOWLEDGE_EOT;
									state = ABORT;
								}else {
									state = ENDTX;
								}
							}
						}
					}
					break;
				case SENDNEXT:
					retryCount = 0;
					blockNumber++;
					assembleBlock();
					if(noMoreData == true) {
						state = ENDTX;
						break;
					}
					try {
						scm.writeBytes(handle, block);
					} catch (SerialComException exp) {
						inStream.close();
						throw exp;
					}
					state = WAITACK;
					break;
				case ENDTX:
					if(eotAckReceptionTimerInitialized != true) {
						eotAckWaitTimeOutValue = System.currentTimeMillis() + 60000; // 1 minute
						eotAckReceptionTimerInitialized = true;
					}
					try {
						scm.writeSingleByte(handle, EOT);
					} catch (SerialComException exp) {
						inStream.close();
						throw exp;
					}
					state = WAITACK;
					break;
				case ABORT:
					/* if ioexception occurs, control will not reach here instead exception would have been
					 * thrown already. */
					inStream.close();
					throw new SerialComTimeOutException("sendFile()", errMsg);
			}
		}
	}

	// prepares xmodem block <SOH><blk #><255-blk #><--128 data bytes--><cksum>
	private void assembleBlock() throws IOException {
		int data = 0;
		int x = 0;
		int blockChecksum = 0;
		
		if(blockNumber > 0xFF) {
			blockNumber = 0x00;
		}
		
		block[0] = SOH;
		block[1] = (byte) blockNumber;
		block[2] = (byte) ~blockNumber;
		
		for(x=x+3; x<128+3; x++) {
			data = inStream.read();
			if(data < 0) {
				if(x != 3) {
					// assembling last block with padding
					for(x=x+0; x<128+4; x++) {
						block[x] = SUB;
					}
				}else {
					noMoreData = true;
					return;
				}
			}else {
				block[x] = (byte) data;
			}
		}
		
		for(x=3; x<131; x++) {
			blockChecksum = (byte)blockChecksum + block[x];
		}
		block[131] = (byte) (blockChecksum % 256);
	}
}
