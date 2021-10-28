package parser;

import parser.Parser;
import util.Function;

public class ResponseParser extends Parser {
    String status;
    String functionName;

    public ResponseParser(byte[] responsePacketArray) {
        super(
                Parser.sliceByteArray(responsePacketArray, 0, 3),
                responsePacketArray[4],
                Parser.sliceByteArray(responsePacketArray, 5, 80),
                Parser.sliceByteArray(responsePacketArray, 86, 34)
                );
        this.status = getStatus(responsePacketArray[3]);
        this.functionName = Function.getFuncionString(responsePacketArray[85]);

        /*// 데이터 참고용 (추후 삭제 예정 부분)
        System.out.println("uniqueID: " + this.uniqueID);
        System.out.println("status: " + this.status);
        System.out.println("lastFlag: " + this.lastFlag);
        System.out.println("contentsLength: " + this.contentsLength);
        System.out.println("contents: " + this.contents);
        System.out.println("optionalInfo: " + this.optionalInfo);
        System.out.println("functionName: " + this.functionName);*/
    }

    public String getStatus(byte code) {
        switch (code) {
            case 20:
                return "success";
            default:
                return "no code";
        }
    }
}
