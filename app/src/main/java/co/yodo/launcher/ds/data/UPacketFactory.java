package co.yodo.launcher.ds.data;

import com.google.gson.Gson;

import sunmi.ds.SF;
import sunmi.ds.callback.ISendCallback;
import sunmi.ds.data.DSData.DataType;
import sunmi.ds.data.DataPacket;

/**
 * 用户数据包工厂类
 *
 * @author longtao.li
 */
public class UPacketFactory {

    private static Gson gson = new Gson();

    //------------DATA----------

    /**
     * build一个数据包
     *
     * @param callback
     * @return
     */
    public static DataPacket buildPack(String receiverPackageName, DataType dataType, DataModel dataModel, String dataJson, ISendCallback callback) {
        String dataJsonStr = createJson(dataModel, dataJson);
        return new DataPacket.Builder(dataType).recPackName(receiverPackageName).data(dataJsonStr).addCallback(callback).isReport(true).build();
    }

    /**
     * build一个指定taskId的数据包
     *
     * @param callback
     * @return
     */
    public static DataPacket buildPack(String receiverPackageName, long taskId, DataType dataType, DataModel dataModel, String dataJson, ISendCallback callback) {
        String dataJsonStr = createJson(dataModel, dataJson);
        return new DataPacket.Builder(dataType).recPackName(receiverPackageName).taskId(taskId).data(dataJsonStr).addCallback(callback).isReport(true).build();
    }


    //---------------FILE-------------

    /**
     * build一个文件传输的数据包
     *
     * @param callback
     * @return
     */
    public static DataPacket buildFilePacket(String recePackName, String filePath, ISendCallback callback) {
        return new DataPacket.Builder(DataType.FILE).recPackName(recePackName).data(filePath).
                addCallback(callback).isReport(true).build();
    }


    //------------CMD--------------

    /**
     * build一个CMD数据包，可以指定要使用的缓存文件Id
     *
     * @param receiverPackageName
     * @param dataModel
     * @param dataJson
     * @param fileId              如果没有则传0
     * @param callback
     * @return
     */
    public static DataPacket buildCMDPack(String receiverPackageName, DataModel dataModel, String dataJson, long fileId, ISendCallback callback) {
        String dataJsonStr = createJson(dataModel, dataJson);
        return new DataPacket.Builder(DataType.CMD).recPackName(receiverPackageName).data(dataJsonStr).fileId(fileId).addCallback(callback).isReport(true).build();
    }

    /**
     * 副屏关机
     */
    public static DataPacket buildShutDown(String recePacakgeName, ISendCallback callback) {
        return buildPack(recePacakgeName, DataType.CMD, DataModel.SHUTDOWN, "", callback);
    }

    /**
     * 副屏重启
     */
    public static DataPacket buildReboot(String recePacakgeName, ISendCallback callback) {
        return buildPack(recePacakgeName, DataType.CMD, DataModel.REBOOT, "", callback);
    }

    /**
     * build一个获取副屏设置的数据的数据包
     *
     * @param callback
     * @return
     */
    public static DataPacket buildSecondScreenData(String receiverPackageName, ISendCallback callback) {
        return buildPack(receiverPackageName, DataType.CMD, DataModel.GET_SECOND_SCREEN_DATA, "", callback);
    }

    /**
     * build一个解锁副屏的数据包
     *
     * @param callback
     * @return
     */
    public static DataPacket buildScreenUnlock(String receiverPackageName, ISendCallback callback) {
        return buildPack(receiverPackageName, DataType.CMD, DataModel.SCREEN_UNLOCK, "", callback);
    }

    /**
     * build一个显示文字的数据包
     *
     * @param text
     * @return
     */
    public static DataPacket buildShowText(String receiverPackageName, String text, ISendCallback callback) {
        return buildPack(receiverPackageName, DataType.DATA, DataModel.TEXT, text, callback);
    }

    /**
     * build一个显示当行居中文字的数据包
     *
     * @param text
     * @return
     */
    public static DataPacket buildShowSingleText(String receiverPackageName, String text, ISendCallback callback) {
        return buildPack(receiverPackageName, DataType.DATA, DataModel.TEXT_SINGLE, text, callback);
    }

    public static DataPacket deleteFolders(String receiverPackageName, String text, ISendCallback callback){
        return buildPack(receiverPackageName, DataType.CMD, DataModel.CLEAN_FILES, text, callback);
    }

    public static String createJson(DataModel dataModel, String dataStr) {
        Data data = new Data();
        data.dataModel = dataModel;
        data.data = dataStr;
        return gson.toJson(data);
    }


}
