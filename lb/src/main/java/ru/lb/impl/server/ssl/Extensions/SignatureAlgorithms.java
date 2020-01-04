package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.ExtensionSuportedGroups;
import ru.lb.design.server.ssl.SignatureAlgorithm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignatureAlgorithms extends AExtension {

    private short signatureAlgorithmLen;
    private List<SignatureAlgorithm> signatureAlgorithms;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        signatureAlgorithmLen = buffer.getShort();

        if(signatureAlgorithmLen > 0){
            signatureAlgorithms = new ArrayList<>();
            for (int i = 0; i < signatureAlgorithmLen/2; i++) {
                signatureAlgorithms.add(
                Arrays.stream(SignatureAlgorithm.values()).filter((q1) -> (q1.getType() == buffer.getShort())).findFirst().get()
                );
            }
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.putShort(signatureAlgorithmLen);
        for (SignatureAlgorithm signatureAlgorithm: signatureAlgorithms) {
            byteBuffer.putShort(signatureAlgorithm.getType());
        }
    }

    public short getSignatureAlgorithmLen() {
        return signatureAlgorithmLen;
    }

    public List<SignatureAlgorithm> getSignatureAlgorithms() {
        return signatureAlgorithms;
    }
}
