package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.ExtensionSuportedGroups;
import ru.lb.design.server.ssl.SignatureAlgorithm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SignatureAlgorithms extends AExtension {

    private short signatureAlgorithmLen;
    private List<SignatureAlgorithm> signatureAlgorithms;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        signatureAlgorithmLen = buffer.getShort();

        if(signatureAlgorithmLen > 0){
            signatureAlgorithms = new ArrayList<>();
            for (int i = 0; i < signatureAlgorithmLen/2; i++) {
                short tempShort = buffer.getShort();
                signatureAlgorithms.add(
                Arrays.stream(SignatureAlgorithm.values()).filter((q1) -> (q1.getType() == tempShort)).findFirst().orElse(null)
                );
            }
            signatureAlgorithms = signatureAlgorithms.stream().filter((q1) -> q1 != null).collect(Collectors.toList());
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

    public void setSignatureAlgorithmLen(short signatureAlgorithmLen) {
        this.signatureAlgorithmLen = signatureAlgorithmLen;
    }

    public void setSignatureAlgorithms(List<SignatureAlgorithm> signatureAlgorithms) {
        this.signatureAlgorithms = signatureAlgorithms;
    }
}
