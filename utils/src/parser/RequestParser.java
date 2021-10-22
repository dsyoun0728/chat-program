package parser;

import parser.Parser;
import util.Function;

public class RequestParser extends Parser {
    String functionName;

    public RequestParser(byte[] responsePacketArray) {
        super(
                Parser.sliceByteArray(responsePacketArray, 0, 3),
                responsePacketArray[4],
                Parser.sliceByteArray(responsePacketArray, 5, 80),
                Parser.sliceByteArray(responsePacketArray, 85, 35)
        );
        this.functionName = Function.getFuncionString(responsePacketArray[3]);

        // 데이터 참고용 (추후 삭제 예정 부분)
        System.out.println("uniqueID: " + this.uniqueID);
        System.out.println("functionName: " + this.functionName);
        System.out.println("lastFlag: " + this.lastFlag);
        System.out.println("contentsLength: " + this.contentsLength);
        System.out.println("contents: " + this.contents);
        System.out.println("optionalInfo: " + this.optionalInfo);
    }
}
