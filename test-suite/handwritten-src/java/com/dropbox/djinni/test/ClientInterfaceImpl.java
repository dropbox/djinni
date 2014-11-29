package com.dropbox.djinni.test;

import static junit.framework.Assert.assertTrue;

public class ClientInterfaceImpl extends ClientInterface {
    @Override
    public ClientReturnedRecord getRecord(long id, String utf8string) {
        if (!utf8string.equals("Non-ASCII / 非 ASCII 字符") && !utf8string.equals("Hello World!")) {
            throw new RuntimeException("Unexpected string. Check UTF-8?");
        }
        return new ClientReturnedRecord(id, utf8string);
    }
}
