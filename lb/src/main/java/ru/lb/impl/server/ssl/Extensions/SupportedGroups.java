package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.ExtensionSuportedGroups;
import ru.lb.design.server.ssl.ExtensionType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SupportedGroups extends AExtension {

    private short supportedGroupsListLen;
    private List<ExtensionSuportedGroups> supportedGroups;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        supportedGroupsListLen = buffer.getShort();

        if(supportedGroupsListLen > 0){
            supportedGroups = new ArrayList<>();
            for (int i = 0; i < supportedGroupsListLen/2; i++) {
                short tempShort = buffer.getShort();
                supportedGroups.add(
                Arrays.stream(ExtensionSuportedGroups.values()).filter((q1) -> (q1.getType() == tempShort)).findFirst().orElse(null)
                );
            }
            supportedGroups = supportedGroups.stream().filter((q1) -> q1 != null).collect(Collectors.toList());
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.putShort(supportedGroupsListLen);
        for (ExtensionSuportedGroups supportedGroup: supportedGroups) {
            byteBuffer.putShort(supportedGroup.getType());
        }
    }

    public short getSupportedGroupsListLen() {
        return supportedGroupsListLen;
    }

    public List<ExtensionSuportedGroups> getSupportedGroups() {
        return supportedGroups;
    }

    public void setSupportedGroupsListLen(short supportedGroupsListLen) {
        this.supportedGroupsListLen = supportedGroupsListLen;
    }

    public void setSupportedGroups(List<ExtensionSuportedGroups> supportedGroups) {
        this.supportedGroups = supportedGroups;
    }
}
