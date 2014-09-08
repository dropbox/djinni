package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class ClientInterfaceTest extends TestCase {

    private ClientInterface jClientInterface;

    @Override
    protected void setUp() {
        jClientInterface = new ClientInterfaceImpl();
    }

    public void testClientReturn() {
        TestHelpers.checkClientInterfaceAscii(jClientInterface);
    }

    public void testClientReturnUTF8() {
        TestHelpers.checkClientInterfaceNonascii(jClientInterface);
    }
}
