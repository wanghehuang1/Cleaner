package com.evideo.kmbox.cleaner.util;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @brief : 文件类工具
 */
@SuppressLint("SdCardPath")
@SuppressWarnings("deprecation")
public class FileUtil {
    /**
     * 缓存的大小
     */
    public static final int BUFFER_SIZE = 8192;

    /**
     * 计算视频文件MD5值大小
     */
    public static final int FIXED_MD5_LENGTH = 3 * 1024 * 1024;

    /**
     * 通过CMD命令计算硬盘空间大小
     *
     * @param path 路径
     * @return double
     */
    public static double countDiskSpaceByCMD(String path) {
        double countSize = 0;
        long begin = System.currentTimeMillis();
        try {
            String CMD = "busybox du -m " + path;
            Process process = Runtime.getRuntime().exec(CMD);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line;
            // 执行命令cmd，只取结果中含有mnt的这一行
            line = reader.readLine();
            while (line != null && line.contains("mnt") == false) {
            }
            String arr[] = line.split("\\/mnt");
            if (arr.length > 0) {
                countSize = Integer.parseInt(arr[0].toString().trim());
            }
        } catch (Exception e) {
//            LogAnalyzeManagerUtil.reportError(e);
            EvLog.e("count FilesSize failure");
        }
        long end = System.currentTimeMillis();
        EvLog.d("count FilesSize use time:" + (end - begin) + "ms");
        EvLog.d("count FilesSize is:" + countSize);
        return countSize;
    }

    /**
     * [功能说明]
     *
     * @param fileName 文件路径
     * @return 文件是否存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * @param path 文件路径
     * @return 是否是文件夹
     */
    public static boolean isDirectory(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * [功能说明] 删除文件
     *
     * @param fileName 文件名
     */
    public static boolean deleteFile(String fileName) {
        if (fileName == null)
            return false;

        File file = new File(fileName);
        if (file.exists()) {
            EvLog.d("delete file " + fileName);
            return file.delete() ? true : false;
        }
        return true;
    }

    /**
     * [功能说明] 删除文件夹下的文件
     *
     * @param dir 文件夹路径
     */
    public static void deleteDir(String dir) {
        File file = new File(dir);

        if (file.exists()) {
            if (file.isDirectory()) { // 如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    files[i].delete(); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }
    }



    /**
     * [功能说明]
     *
     * @return 获取tft的路径
     */
    public static String getTtftpPath() {
        String tftppath = "/mnt/sdcard/kmbox/tftproot/";
        try {
            if (!(new File(tftppath).isDirectory())) {
                new File(tftppath).mkdir();
            }
        } catch (SecurityException e) {
//            LogAnalyzeManagerUtil.reportError(e);
            e.printStackTrace();
        }
        return tftppath;
    }

    /**
     * @param path 文件夹路径
     * @brief : [删除指定文件夹下的所有文件]
     */
    public static void deleteAllFiles(String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) { // 文件不存在或不是目录
            return;
        }

        String[] tempList = file.list();
        if (tempList == null) {
            EvLog.d("tempList null, no file in " + file.getPath());
            return;
        }
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {

            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            } else if (temp.isDirectory()) {
                deleteAllFiles(temp.getAbsolutePath());
                temp.delete();
            }
        }

    }

    /**
     * @param path 文件路径
     * @return 文件大小
     * @brief : [计算该路径对应文件或目录的长度，文件或目录不存在返回-1] 使用单线程递归方式计算
     */
    public static long countLength(String path) {
        if (path == null) {
            return -1;
        }

        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                for (File f : children) {
                    size += countLength(f.getAbsolutePath());
                }
                return size;
            } else if (file.isFile()) {
                return file.length();
            }
        }

        return -1;
    }

    /**
     * @param path 路径
     * @return 可用空间大小
     * @brief : [获取可用空间大小]
     */
    public static long getAvailableSize(String path) {
        long time = System.currentTimeMillis();
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return -1;
        }
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();
        long size = blockSize * availableBlocks;
        long countTime = (System.currentTimeMillis() - time);
        if (countTime > 10) {
            EvLog.w("getAvailableSize count time:" + countTime +"  size:" + size + "  path:" + path);
        }
        return size;
    }

    /**
     * 在限定时间内获取硬盘剩余空间大小
     * @param path
     * @return
     */
    public static long getAvailableSizeInTime(final String path){
        long avialableSize = -1;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<Long> future =
                new FutureTask<Long>(new Callable<Long>() {
                    public Long call() {
                        return getAvailableSize(path);
                    }});
        executor.execute(future);
        try {
            //限定100ms执行完，正常情况下 10ms内能执行完
            avialableSize = future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            avialableSize = -1;
            future.cancel(true);
            EvLog.e("getAvailableSizeInTime InterruptedException");
        } catch (ExecutionException e) {
            avialableSize = -1;
            future.cancel(true);
            EvLog.e("getAvailableSizeInTime ExecutionException");
        } catch (TimeoutException e) {
            avialableSize = -1;
            future.cancel(true);
            EvLog.e("getAvailableSizeInTime TimeoutException");
        } finally {
            executor.shutdown();
        }
        return avialableSize;
    }

//    /**
//     * @param context 上下文
//     * @param path    路径
//     * @return 总空间大小
//     * @brief : [获取格式化后的可用空间大小]
//     */
//    public static String getAvailableFormatSize(Context context, String path) {
//        long size = getAvailableSize(path);
//        if (size < 0) {
//            return null;
//        }
//        return FileSizeFormatter.formatFileSize(context, size);
//    }

    /**
     * @param path 路径
     * @return 总大小
     * @brief : [获取总大小]
     */
    public static long getTotalSize(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return -1;
        }
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long blockCount = statFs.getBlockCount();
        return blockSize * blockCount;
    }

    /**
     * @param path   路径
     * @param append 路径
     * @return 拼接后的路径
     * @brief : [组合路径]
     */
    public static String concatPath(String path, String append) {
        if (path == null) {
            return null;
        }
        String temp = null;
        if (path.endsWith(File.separator)) {
            temp = path + append;
        } else {
            temp = path + File.separator + append;
        }
        return temp;
    }

    /**
     * [获取文件行数]
     *
     * @param file 文件路径
     * @return
     * @throws IOException
     */
    public static int getTotalLines(File file) throws IOException {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        in.close();
        return lines;
    }

    /**
     * [根据行读取文件]
     *
     * @param sourceFile
     * @param lineNumber 行号从1开始
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public static String readFileLineNumber(String sourceFile, int lineNumber)
            throws IOException {
        File file = new File(sourceFile);
        if (file == null) {
            EvLog.e("the file is null!");
            return "";
        }
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = "";
        String out = "";
        if (lineNumber <= 0 || lineNumber > getTotalLines(file)) {
            EvLog.e("the lineNumber is out of range!");
        }
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
            if ((lines - lineNumber) == 0) {
                out = s;
                break;
            }
        }
        reader.close();
        in.close();
        return out;
    }

    /**
     * @return SD卡剩余空间大小
     */
    public static Long getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    /**
     * [获取文件的MD5值]
     *
     * @param file 文件路径
     * @return MD5值
     * @throws FileNotFoundException 文件未找到异常
     */
    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(
                    FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            // e.printStackTrace();
//            LogAnalyzeManagerUtil.reportError(e);
            EvLog.e("---getMd5ByFile exception1--");
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    // e.printStackTrace();
//                    LogAnalyzeManagerUtil.reportError(e);
                    EvLog.e("---getMd5ByFile exception2--");
                }
            }
        }
        return value;
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file 文件
     * @return md5值
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * [获取文件扩展名]
     *
     * @param filePath 文件名
     * @return String 扩展名
     */
    public static String getFileExt(String filePath) {
        if (filePath == null) {
            return null;
        }

        int pos = filePath.indexOf(".");
        if (pos < 0 || pos >= filePath.length()) {
            return null;
        }

        return filePath.substring(filePath.indexOf(".") + 1);
    }

    /**
     * [获取某文件的byte数组]
     *
     * @param filePath 文件的路径
     * @return byte[]
     */
    public static byte[] getFileByteArray(String filePath) {
        FileInputStream is;
        try {
            is = new FileInputStream(new File(filePath));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[8 * 1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            is.close();
            return buffer.toByteArray();
        } catch (FileNotFoundException e2) {
//            LogAnalyzeManagerUtil.reportError(e2);
            // TODO Auto-generated catch block
        } catch (IOException e1) {
//            LogAnalyzeManagerUtil.reportError(e1);
            // TODO Auto-generated catch block
        }
        return null;
    }

    /**
     * [获取文件的MD5值]
     *
     * @param file 文件路径
     * @return MD5值
     * @throws FileNotFoundException 文件未找到异常
     */
    public static String getMd5ByFilePartial(File file) throws Exception {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        int size = (int) Math.min(file.length(), FIXED_MD5_LENGTH);
        byte buffer[] = new byte[size];
        int len;
        try {
            int readLenght = size;
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, readLenght)) != -1) {
                digest.update(buffer, 0, len);
                readLenght -= len;
                if (readLenght <= 0) {
                    break;
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        String md5 = bigInt.toString(16);

        if (md5.length() != 32) {
            for (int i = md5.length(); i < 32; i++) {
                md5 += "0";
            }
        }
        return md5;
    }

    public static boolean createFile(String destFileName) {
        // 保证sdcard至少有100M空间
//        cleanSdCard(100);

        File file = new File(destFileName);
        if (file.exists()) {
            EvLog.i("创建单个文件" + destFileName + "失败，目标文件已存在！");
            return false;
        }
        if (destFileName.endsWith(File.separator)) {
            EvLog.e("创建单个文件" + destFileName + "失败，目标文件不能为目录！");
            return false;
        }
        // 判断目标文件所在的目录是否存在
        if (!file.getParentFile().exists()) {
            // 如果目标文件所在的目录不存在，则创建父目录
            EvLog.i("目标文件所在目录不存在，准备创建它！");
            if (!file.getParentFile().mkdirs()) {
                EvLog.e("创建目标文件所在目录失败！");
                return false;
            }
        }
        // 创建目标文件
        try {
            if (file.createNewFile()) {
                EvLog.i("创建单个文件" + destFileName + "成功！");
                return true;
            } else {
                EvLog.e("创建单个文件" + destFileName + "失败！");
                return false;
            }
        } catch (IOException e) {
//            LogAnalyzeManagerUtil.reportError(e);
            e.printStackTrace();
            EvLog.e("创建单个文件" + destFileName + "失败！" + e.getMessage());
            return false;
        }
    }

    /**
     * 移动文件
     *
     * @param srcFileName 源文件完整路径
     * @param destDirName 目的目录完整路径
     * @return 文件移动成功返回true，否则返回false
     */
    public static boolean moveFile(String srcFileName, String destDirName) {

        File srcFile = new File(srcFileName);
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        String destPath = destDirName + File.separator + srcFile.getName();
        EvLog.d("dest path:" + destPath);
        return srcFile.renameTo(new File(destPath));
    }

    public static boolean renameFileWithPath(String sourcePath, String destPath) {
        if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(destPath)) {
            return false;
        }
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return false;
        }

        @SuppressWarnings("unused")
        String c = sourceFile.getParent();
        File refile = new File(destPath);
        if (sourceFile.renameTo(refile)) {
            EvLog.i("FileUtil", "rename success");
            sourceFile.deleteOnExit();
        } else {
            EvLog.e("FileUtil", "rename failed!");
            return false;
        }
        return true;
    }

    private static final String RM_SH = "/mnt/sdcard/kmbox/rm.sh";
    /**
     * @param path       待删除文件所在目录
     * @param suffixName 指定后缀名 例如存储盘下“.tmp”临时文件
     * @return true成功， false失败
     */
    public static boolean deleteSuffixFile(String path, String suffixName) {
        String rmCommond = "rm " + path + suffixName;
        EvLog.d("rmCommond:" + rmCommond);
        try {
            reCreateRmSh(rmCommond);
        } catch (IOException e) {
            e.printStackTrace();
            EvLog.e("reCreateRmSh failed:" + e);
            return false;
        }

        try {
            Runtime.getRuntime().exec("/system/bin/sh " + RM_SH);
        } catch (IOException e) {
            EvLog.e("implement rm failed:" + e);
            return false;
        }
        return true;
    }

    // 重写rm命令
    private static void reCreateRmSh(String commond) throws IOException {
        // 删除再重新创建
        File f = new File(RM_SH);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        // 修改为777权限
        try {
            Runtime.getRuntime().exec("chmod 777 " + RM_SH);
        } catch (IOException e) {
            EvLog.e("reCreateRmSh failed!");
        }

        FileWriter writer = new FileWriter(RM_SH, true);
        writer.write(commond);
        writer.close();
    }

    public static long getFileSizes(File f) throws Exception {
        long size = 0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            size = fis.available();
        } else {
            f.createNewFile();
            System.out.println("文件不存在");
        }
        return size;
    }

    /**
     * [获取文件夹大小]
     * @param f
     * @return
     * @throws Exception
     */
    public static long getFileSize(File f) throws Exception {
        long size = 0;
        if (!f.isDirectory()) {
            return f.length();
        }

        File[] flists = f.listFiles();
//        if (flists == null) {
//            return size;
//        }
        for (File flist : flists) {
            if (flist.isDirectory()) {
                size = size + getFileSize(flist);
            } else {
                size = size + flist.length();
            }
        }
        return size;
    }

    public static boolean isEmptyDir(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        if (!isDirectory(path)) {
            return false;
        }
        File file = new File(path);
        File[] files = file.listFiles();
        if (files.length == 0 || files == null) {
            return true;
        }
        return false;
    }

//    /**
//     * [功能说明]单位为M
//     */
//    public static void cleanSdCard(long needFreeSize) {
//        long freeSize = 1024 * 1024 * needFreeSize;
//        long leftSize = 0;
//        boolean isDebug = KmSharedPreferences.getInstance().getBoolean(
//                KeyName.KEY_DEBUG_MODE_SWITCH, false);
//
//        String path = Environment.getExternalStorageDirectory().getPath();
//
//        //删除无用路径
//        FileUtil.deleteAllFiles(path + "/360Download");
//        FileUtil.deleteAllFiles(path + "/wandoujia/app/");
//
//        leftSize = FileUtil.getAvailableSize(path);
//        if (leftSize < freeSize) {
//            EvLog.e("-------left size is ---------" + leftSize);
//            FileUtil.deleteAllFiles(path + "/LOST.DIR");
//            FileUtil.deleteAllFiles(path + "/download");
//        } else {
//            return;
//        }
//
//        leftSize = FileUtil.getAvailableSize(path);
//        if (leftSize < freeSize) {
//            EvLog.e("-------left size is ---------" + leftSize);
//            FileUtil.deleteAllFiles(path + "/android");
//        } else {
//            return;
//        }
//
//        // 小于设定空间自动清除文件,debug 模式不能清log
//        if (!isDebug) {
//            leftSize = FileUtil.getAvailableSize(path);
//            if (leftSize < freeSize ) {
//                EvLog.e("-------left size is ---------" + leftSize);
//                FileUtil.deleteAllFiles(path + "/kmbox/log");
//            } else {
//                return;
//            }
//        }
//
//        leftSize = FileUtil.getAvailableSize(path);
//        if (leftSize < freeSize) {
//            EvLog.e("-------left size is ---------" + leftSize);
//            FileUtil.deleteAllFiles(path + "/kmbox/records");
//        } else {
//            return;
//        }
//
//        leftSize = FileUtil.getAvailableSize(path);
//        if (leftSize < freeSize){
//            EvLog.e("-------left size is ---------" + leftSize);
//            FileUtil.deleteAllFiles(path + "/kmbox/template/download");
//        } else {
//            return;
//        }
//    }

//    /**
//     * 修改文件权限为777
//     * @param path
//     */
//    public static void modifyPermission(String path){
//        try {
//            Runtime.getRuntime().exec(
//                    "chmod 777 " + path);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            LogAnalyzeManagerUtil.reportError(e);
//            EvLog.e("Modify permissions failure");
//        }
//    }
}
