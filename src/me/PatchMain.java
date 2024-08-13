package me;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PatchMain {

    private static final String SEP = File.separator;
    private static String defaultProjectPath = "/Users/hynial/IdeaProjects/gm/svn/xmjhx/";
    private static String defaultUpgradePath = "./upgrade/";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String defaultConfigPath = "./patchPathList.txt";
        String pathListFileString = (args != null && args.length > 0) ? args[0] : defaultConfigPath;
        if (pathListFileString.equals(".")) {
            pathListFileString = defaultConfigPath;
        }

        String upgradeTargetDir = (args != null && args.length > 1) ? args[1] : defaultUpgradePath;
        if (upgradeTargetDir.equals(".")) {
            upgradeTargetDir = defaultUpgradePath;
        }
        upgradeTargetDir = upgradeFolderName(upgradeTargetDir);

        String codeProjectPath = System.getProperty("codeProjectPath");
        if (codeProjectPath == null || codeProjectPath.trim().equals("")) {
            codeProjectPath = defaultProjectPath;
        }

        System.out.println(String.format("pathListFile:\t%s", pathListFileString));
        System.out.println(String.format("backupTargetDir:\t%s", upgradeTargetDir));
        System.out.println(String.format("codeProjectPath:\t%s", codeProjectPath));

        System.out.println("开始生成升级包");
        Map<String, String> unDeletes = readTxtFile4Upgrade(pathListFileString, upgradeTargetDir, codeProjectPath);
        List<String> totalUpgrades = new ArrayList<>();
        for (Map.Entry<String, String> en : unDeletes.entrySet()) {
            boolean y = copyFile(en.getKey(), en.getValue());
            if (y) {
                totalUpgrades.add(en.getKey());
            }
        }
        System.out.println("升级包生成成功，共涉及" + totalUpgrades.size() + "个文件");
        System.out.println("升级包路径：" + upgradeTargetDir);
    }

    public static String upgradeFolderName(String upgradeTargetDir) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        upgradeTargetDir = upgradeTargetDir + df.format(new Date()) + SEP;
        return upgradeTargetDir;
    }

    public static Map<String, String> readTxtFile4Upgrade(String filePath, String upgradeTargetDir, String codeProjectPath) {
        Map<String, String> resultMap = new HashMap<String, String>();
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String modifyPath = lineTxt.trim();
                    if (lineTxt.startsWith("M") || lineTxt.startsWith("?")) {
                        String[] parts = lineTxt.split("\\s+");
                        if (parts.length > 2) {
                            modifyPath = parts[2];
                        } else {
                            modifyPath = parts[1];
                        }
                    }
                    if (modifyPath.startsWith("src/")) {
                        modifyPath = "WebRoot/WEB-INF/classes" + modifyPath.substring(3);
                        if (modifyPath.endsWith("java")) {
                            modifyPath = modifyPath.substring(0, modifyPath.length() - 4) + "class";
                            String temp = modifyPath.substring(0, modifyPath.length() - 6) + "$";
                            int k = 1;
                            while (true) {
                                String tempF = temp + k + ".class";
                                File tempFile = new File(tempF);
                                if (!tempFile.exists()) {
                                    break;
                                }
                                resultMap.put(codeProjectPath + tempF, upgradeTargetDir + tempF);
                                k++;
                            }
                        }
                    }
                    resultMap.put(codeProjectPath + modifyPath, upgradeTargetDir + modifyPath);
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
}
