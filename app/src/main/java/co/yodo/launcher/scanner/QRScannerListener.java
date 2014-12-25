package co.yodo.launcher.scanner;

public interface QRScannerListener {
    /**
     * Listener for the data of the scanner
     * @param data String data received
     */
	public void onNewData(String data);
}
