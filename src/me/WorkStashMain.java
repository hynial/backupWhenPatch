package me;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorkStashMain {
    private static String OS;

    static {
        OS = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        System.out.println(String.format("ExecuteUnderOS: %s", OS));
    }
    private static final String SEP = File.separator;
    public static final String DEFAULT_PROJECT_DIR = "/Users/hynial/IdeaProjects/gm/svn/xmjhx/";
    private static final String defaultStashPath = "./stash/";
    private static final String defaultStashNode = "未填写功能描述";

    public static void main(String[] args) {
        String defaultConfigPath = "./workStashList.txt";
        String pathListFileString = (args != null && args.length > 0) ? args[0] : defaultConfigPath;
        if (pathListFileString.equals(".")) {
            pathListFileString = defaultConfigPath;
        }

        String stashTargetDir = (args != null && args.length > 1) ? args[1] : defaultStashPath;
        if (stashTargetDir.equals(".")) {
            stashTargetDir = defaultStashPath;
        }
        stashTargetDir = stashFolderName(stashTargetDir);

        String codeProjectPath = System.getProperty("codeProjectPath");
        if (codeProjectPath == null || codeProjectPath.trim().equals("")) {
            codeProjectPath = DEFAULT_PROJECT_DIR;
        }

        String featureDesc = System.getProperty("note");
        if (featureDesc == null || featureDesc.trim().equals("")) {
            featureDesc = defaultStashNode;
        }

        if (featureDesc.equals(defaultStashNode)) {
            System.out.println("pleaseWriteNote:");
            Scanner keyboard = new Scanner(System.in);
            String inputNode = keyboard.nextLine();
            if (!isEmpty(inputNode)) {
                featureDesc = inputNode;
            }
        }

        System.out.println(String.format("stashWorkListFile:\t%s", pathListFileString));
        System.out.println(String.format("stashTargetDir:\t%s", stashTargetDir));
        System.out.println(String.format("codeProjectPath:\t%s", codeProjectPath));

        System.out.println("开始Stash工作路径");
        Map<String, String> unDeletes = readTxtFile4Stash(pathListFileString, stashTargetDir, codeProjectPath);
        List<String> totalStashes = new ArrayList<>();
        List<String> targetStashes = new ArrayList<>();
        for (Map.Entry<String, String> en : unDeletes.entrySet()) {
            boolean y = copyFile(en.getKey(), en.getValue());
            if (y) {
                totalStashes.add(en.getKey());
                targetStashes.add(en.getValue());
            }
        }
        generateStashDescriptor(totalStashes, targetStashes, stashTargetDir, featureDesc);

        System.out.println("工作包Stash生成成功，共涉及" + totalStashes.size() + "个文件");
        System.out.println("工作包Stash路径：" + stashTargetDir);
    }

    public static String stashFolderName(String upgradeTargetDir) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        upgradeTargetDir = upgradeTargetDir + df.format(new Date()) + SEP;
        return upgradeTargetDir;
    }

    public static Map<String, String> readTxtFile4Stash(String filePath, String upgradeTargetDir, String codeProjectPath) {
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
                    if (modifyPath.length() == 0) continue;
                    String[] parts = lineTxt.split("\\s+");
                    modifyPath = parts[parts.length - 1];
                    if (modifyPath.endsWith(".") || modifyPath.endsWith(":")) continue;
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

    public static void generateStashDescriptor(List<String> stashList, List<String> targetStashes, String stashTargetDir, String stashNote) {
        String newPath = stashTargetDir + "stashDescription.txt";

        try {
            FileOutputStream fs = new FileOutputStream(newPath);
            if (isEmpty(stashNote)) {
                stashNote = "---------------------";
            }
            fs.write((stashNote + (isWindows() ? "\r\n\r\n" : "\n\n")).getBytes(Charset.forName("UTF-8")));
            for (String s : stashList) {
                fs.write((s + (isWindows() ? "\r\n" : "\n")).getBytes(Charset.forName("UTF-8")));
            }
            fs.write(((isWindows() ? "\r\n" : "\n")).getBytes(Charset.forName("UTF-8")));
            fs.write(("--------------------" + (isWindows() ? "\r\n\r\n" : "\n\n")).getBytes(Charset.forName("UTF-8")));
            for (String sTarget : targetStashes) {
                String p = new File(sTarget).getAbsolutePath();
                p = p.replaceAll("\\./", "");
                fs.write((p + (isWindows() ? "\r\n" : "\n")).getBytes(Charset.forName("UTF-8")));
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
