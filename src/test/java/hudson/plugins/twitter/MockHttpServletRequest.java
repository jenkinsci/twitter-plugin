package hudson.plugins.twitter;

import javax.servlet.ServletRequest;

import com.mockobjects.ReturnValue;

public class MockHttpServletRequest extends com.mockobjects.servlet.MockHttpServletRequest
        implements ServletRequest {

    private final ReturnValue myLocalAddr = new ReturnValue("localAddr");
    private final ReturnValue myLocalName = new ReturnValue("localName");
    private final ReturnValue myLocalPort = new ReturnValue("localPort");
    private final ReturnValue myRemotePort = new ReturnValue("remotePort");

    public String getLocalAddr() {
        return (String) myLocalAddr.getValue();
    }

    public void setupGetLocalAddr(String localAddr) {
        myLocalAddr.setValue(localAddr);
    }

    public String getLocalName() {
        return (String) myLocalName.getValue();
    }

    public void setupGetLocalName(String localName) {
        myLocalName.setValue(localName);
    }

    public int getLocalPort() {
        return myLocalPort.getIntValue();
    }

    public void setupGetLocalPort(int localPort) {
        myLocalPort.setValue(localPort);
    }

    public int getRemotePort() {
        return myRemotePort.getIntValue();
    }

    public void setupGetRemotePort(int remotePort) {
        myRemotePort.setValue(remotePort);
    }

}
