package com.dropbox.djinni.test;

import javax.annotation.CheckForNull;

public class ClientInterfaceImpl extends ClientInterface {
    @Override
    public ClientReturnedRecord getRecord(long id, String utf8string, String misc) {
        if (!utf8string.equals("Non-ASCII /\0 非 ASCII 字符") && !utf8string.equals("Hello World!")) {
            throw new RuntimeException("Unexpected string. Check UTF-8?");
        }
        return new ClientReturnedRecord(id, utf8string, misc);
    }
    @Override
    public double identifierCheck(byte[] data, int r, long jret)
    {
        return 0.0;
    }
    @Override
    public String returnStr() {
        return "test";
    }

    @Override
    public String methTakingInterface(@CheckForNull ClientInterface i) {
        if (i != null) { return i.returnStr(); } else { return ""; }
    }

    @Override
    public String methTakingOptionalInterface(@CheckForNull ClientInterface i) {
        if (i != null) { return i.returnStr(); } else { return ""; }
    }
}
