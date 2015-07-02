package com.dropbox.djinni.test;

public class ClientInterfaceImpl extends ClientInterface {
    @Override
    public ClientReturnedRecord getRecord(long id, String utf8string, String misc) {
        if (!utf8string.equals("Non-ASCII / 非 ASCII 字符") && !utf8string.equals("Hello World!")) {
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
}
