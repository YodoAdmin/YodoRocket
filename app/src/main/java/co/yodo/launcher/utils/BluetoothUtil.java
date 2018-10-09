package co.yodo.launcher.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by hei on 17/01/17.
 * Utils for Bluetooth
 */

public class BluetoothUtil {
    /** Address of the printer */
    private static final String mInnerPrinterAddress = "00:11:22:33:44:55";
    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /** Gets the BT adapter */
    private static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Enables Bluetooth if it is not already enabled
     */
    private static void enableBT() {
        BluetoothAdapter adapter = getBTAdapter();
        if( adapter != null && !adapter.isEnabled() )
            adapter.enable();
    }

    /**
     * Gets the printer device
     * @return The Bluetooth device that represents the printer
     */
    public static BluetoothDevice getDevice() {
        // Enables bluetooth if it is off
        enableBT();

        BluetoothDevice innerprinter_device = null;
        BluetoothAdapter adapter = getBTAdapter();
        if( adapter != null ) {
            Set<BluetoothDevice> devices = adapter.getBondedDevices();

            for( BluetoothDevice device : devices ) {
                if( device.getAddress().equals( mInnerPrinterAddress ) ) {
                    innerprinter_device = device;
                    break;
                }
            }
        }
        return innerprinter_device;
    }

    /**
     * Connects the socket for the printer
     * @param device The Bluetooth device
     * @return The opened socket
     */
    private static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
        socket.connect();
        return socket;
    }

    /**
     * Sends the data to the printer
     * @param bytes The bytes to be written in the socket
     * @param socket The opened socket
     */
    private static void sendData(byte[] bytes, BluetoothSocket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write(bytes, 0, bytes.length);
        out.close();
    }

    /**
     * Prints the data using the bluetooth printer
     * @param data The data that shouldn't be null
     */
    public static void printData(byte[] data) {
        if (data != null) {
            // 4: Using InnerPrinter print data
            BluetoothSocket socket = null;
            try {
                socket = BluetoothUtil.getSocket(getDevice());
                BluetoothUtil.sendData(data, socket);
            } catch (IOException e) {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
