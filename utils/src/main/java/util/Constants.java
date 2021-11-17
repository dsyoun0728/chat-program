package util;

public class Constants {
    public static final int PACKET_TOTAL_SIZE = 500;
    public static final int PACKET_LAST_FLAG_SIZE = 1;
    public static final int PACKET_UUID_SIZE = 16;
    public static final int PACKET_FUNCTION_SIZE = 4;
    public static final int PACKET_CONTENTS_LENGTH_SIZE = 4;
    public static final int PACKET_CONTENTS_SIZE = 400;
    public static final int PACKET_OPTIONAL_INFO_SIZE = 75;
    public static final int PACKET_TOTAL_PACKET_NUMBER_SIZE = 4;
    public static final int PACKET_RESPONSE_CODE_SIZE = 1;

    public static final int RESPONSE_SUCCESS = 20;

    public static String getStatus(byte b) {
        return (b == (byte) RESPONSE_SUCCESS) ? "RESPONSE_SUCCESS" : "RESPONSE_FAIL";
    }
}
