package me;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 从 svn 上的变更文件路径 =》备份部署目录下的相应文件
 * svn =》 class =》backup
 * <p>
 * java -jar config-path target-backup-dir -DlocProjectName=xmjhx -DdeployProjectName=jzww -DwebappsPath=/xxx/webapps/
 */
public class BackMain {
    private static String OS;

    static {
        OS = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        System.out.println(String.format("ExecuteUnderOS: %s", OS));
    }
    //    private static String webappsPath = "D:\\jzww_tomcat\\webapps\\";
    private static String webappsPath = "/Users/hynial/IdeaProjects/gm/backupWhenPatch/webapps/";
    private static String locProjectName = "xmjhx";
    private static String deployProjectName = "jzww";
    private static String deployProjectPath;

    public static void main(String[] args) {
        String defaultConfigPath = "./backPathList.txt"; // /Users/hynial/IdeaProjects/gm/backupWhenPatch/pathList.txt
        boolean isProd = false;
        String pathListFileString = (args != null && args.length > 0) ? args[0] : defaultConfigPath;
        if (pathListFileString.equals(".") || pathListFileString.equals("_")) {
            isProd = pathListFileString.equals("_");
            pathListFileString = defaultConfigPath;
        }
        String backupTargetDir = (args != null && args.length > 1) ? args[1] : "./";
        if (backupTargetDir.equals(".")) {
            backupTargetDir = "./";
        }

        String inputWebappsPath = System.getProperty("webappsPath");
        if (isEmpty(inputWebappsPath)) {
            if (isWindows()) {
                webappsPath = "D:\\jzww_tomcat\\webapps\\";
                if (isProd) {
                    webappsPath = "D:\\data\\apache-tomcat-8.5\\webapps\\";
                }
            }
        } else {
            webappsPath = inputWebappsPath.trim();
        }

        String inputLocProjectName = System.getProperty("locProjectName");
        if (!isEmpty(inputLocProjectName)) {
            locProjectName = inputLocProjectName.trim();
        }
        String inputDeployProjectName = System.getProperty("deployProjectName");
        if (!isEmpty(inputDeployProjectName)) {
            deployProjectName = inputDeployProjectName.trim();
        }
        deployProjectPath = webappsPath + deployProjectName + File.separator;

        backupTargetDir = backupFolderName(backupTargetDir);
        System.out.println(String.format("pathListFile:\t%s", pathListFileString));
        System.out.println(String.format("backupTargetDir:\t%s", backupTargetDir));
        System.out.println(String.format("locProjectName:\t%s", locProjectName));
        System.out.println(String.format("deployProjectName:\t%s", deployProjectName));
        System.out.println(String.format("webappsPath:\t%s", webappsPath));

        Map<String, String> backupTaskMap = readTxtFile4Backup(pathListFileString, backupTargetDir);
        List<String> totalBacks = new ArrayList<>();
        for (Map.Entry<String, String> en : backupTaskMap.entrySet()) {
            boolean y = copyFile(en.getKey(), en.getValue());
            if (y) {
                totalBacks.add(en.getKey());
            }
        }
        generateBackupDescriptor(totalBacks, backupTargetDir);
        System.out.println("备份包生成成功，共涉及" + backupTaskMap.size() + "个文件");
        String backPackPath = new File(backupTargetDir).getAbsolutePath();
        backPackPath = backPackPath.replaceAll("\\./", "/").replaceAll("//", "/");
        if (isWindows()) {
            File b = new File(backupTargetDir);
            String tmp = b.getParentFile().getAbsolutePath();
            backPackPath = tmp.substring(0, tmp.length() - 1) + b.getName();
        }

        System.out.println("备份包路径：" + backPackPath);
    }

    public static String backupFolderName(String backupTargetDir) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HHmmss");    //设置日期格式
        backupTargetDir = backupTargetDir + df.format(new Date()) + File.separator;
        return backupTargetDir;
    }

    public static List<String> toFullPath(String linePath) {
        File tmp = new File(linePath);
        List<String> results = new ArrayList<>();

        if (tmp.exists()) {
            results.add(linePath);
        } else {
            // svn To class # /xmjhx/src/com/zhiwei/credit/util/RemoveStrUtilJHX.java
            String modifyPath = linePath.split("/" + locProjectName + "/")[1];
            if (modifyPath.startsWith("src/")) {
                modifyPath = "WEB-INF/classes" + modifyPath.substring(3);
                if (modifyPath.endsWith("java")) {
                    modifyPath = modifyPath.substring(0, modifyPath.length() - 4) + "class";
                    // 不能只考虑Action.class里面有匿名的内部类，任何一个class文件，可能都存在一个匿名类
                    String temp = modifyPath.substring(0, modifyPath.length() - 6) + "$";//+"1.class";
                    int k = 1;
                    while (true) {
                        String tempF = deployProjectPath + temp + k + ".class";
                        File tempFile = new File(tempF);
                        if (!tempFile.exists()) {
                            break;
                        }
                        results.add(tempF);
                        k++;
                    }
                }
            }
            results.add(deployProjectPath + modifyPath);
        }
        return results;
    }

    public static Map<String, String> readTxtFile4Backup(String filePath, String backupTargetDir) {
        Map<String, String> resultMap = new HashMap<String, String>();
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (lineTxt == null || lineTxt.trim().equals("")) continue;
                    if (lineTxt.startsWith("#")) continue;

                    List<String> deployM = toFullPath(lineTxt.trim());
                    for (String backPath : deployM) {
                        System.out.println("Processing:" + backPath);
                        if (!backPath.contains("webapps")) {
                            System.out.println(String.format("Warn! backupPathWithoutDirectory: %s, %s", "webapps", backPath));
                        }

                        File backFile = new File(backPath);
                        if (!backFile.exists()) continue;
                        if (backFile.isDirectory()) continue;
                        String subPath = backPath.split("webapps")[1];
                        if (subPath.startsWith("/") || subPath.startsWith("\\")) subPath = subPath.substring(1);
                        String target = backupTargetDir + subPath;
                        resultMap.put(backPath, target);
                    }
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return resultMap;
    }

    public static boolean copyFile(String oldPath, String newPath) {
        File toFile = new File(newPath);
        File parentFile = toFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            int byteRead = 0;
            File oldFile = new File(oldPath);
            if (oldFile.exists() && oldFile.isFile()) { // 文件存在时
                System.out.println(oldPath);
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
                fs.close();
                return true;
            } else {
                System.out.println("未找到文件：" + oldPath);
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }

        return false;
    }

    public static void generateBackupDescriptor(List<String> backupList, String backupTargetDir) {
        String newPath = backupTargetDir + "backupList.txt";

        try {
            FileOutputStream fs = new FileOutputStream(newPath);
            for (String backup : backupList) {
                fs.write((backup + (isWindows() ? "\r\n" : "\n")).getBytes(Charset.forName("UTF-8")));
            }
            fs.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isUnix() {
        return OS.contains("nux");
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
}
