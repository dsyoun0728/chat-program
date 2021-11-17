package util;

import java.lang.reflect.Field;

public class Function {
    public static final int Login = 0;
    public static final int Logout = 1;
    public static final int SendText = 4;
    public static final int SendWhisper = 5;
    public static final int SendFile = 6;
    public static final int SendFiles = 7;
    public static final int ShowFileList = 16;
    public static final int DownloadFile = 17;
    public static final int DownloadFiles = 18;
    public static final int DeleteFile = 19;
    public static final int DeleteFiles = 20;
    public static int returnFunctionNum = 127;

    public static final String _0 = "Login";
    public static final String _1 = "Logout";
    public static final String _4 = "SendText";
    public static final String _5 = "SendWhisper";
    public static final String _6 = "SendFile";
    public static final String _7 = "SendFiles";
    public static final String _16 = "ShowFileList";
    public static final String _17 = "DownloadFile";
    public static final String _18 = "DownloadFiles";
    public static final String _19 = "DeleteFile";
    public static final String _20 = "DeleteFiles";
    public static String returnFunctionStr = "Not Defined util.Function";

    public static int getFunctionNum(String fieldName) {
        Function function = new Function();
        try {
            Field field = function.getClass().getDeclaredField(fieldName);
            returnFunctionNum = (int) field.get(field);
        } catch (NoSuchFieldException e) {
            System.out.println("util.Function Class getByte NoSuchFieldException" + "\n\n\n");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.out.println("util.Function Class getByte IllegalAccessException" + "\n\n\n");
            e.printStackTrace();
        }
        return returnFunctionNum;
    }

    public static String getFuncionString(int byteNum) {
        Function function = new Function();
        try {
            Field field = function.getClass().getDeclaredField("_" + byteNum);
            returnFunctionStr = (String) field.get(field);
        } catch (NoSuchFieldException e) {
            System.out.println("util.Function Class getByte NoSuchFieldException" + "\n\n\n");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.out.println("util.Function Class getByte IllegalAccessException" + "\n\n\n");
            e.printStackTrace();
        }
        return returnFunctionStr;
    }
}
