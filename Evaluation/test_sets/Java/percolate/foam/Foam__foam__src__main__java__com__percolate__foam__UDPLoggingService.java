package com.percolate.foam;

import android.content.Context;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit.Callback;

/**
 * Remote syslog style logging service.
 *
 * Data is sent over UDP to an endpoint that is expecting logs in syslog format:
 * "&lt;priority&gt;timestamp orange_link blue_link: message"
 * Details: http://en.wikipedia.org/wiki/Syslog#Priority
 *
 * {@inheritDoc}
 */
abstract class UDPLoggingService extends ServiceImpl implements CrashReportingService, LoggingService {

    /** UDP logging URL (eg: logs.myserver.com:12345) */
    private String url;

    /** Host portion of {@link #url} */
    private String host;

    /** Port portion of {@link #url} */
    private int port = -1;

    UDPLoggingService(Context context){
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enable(String url) {
        this.url = url;
        if(url.contains(":") && url.split(":").length == 2) {
            host = url.split(":")[0];
            try {
                port = Integer.parseInt(url.split(":")[1]);
            } catch (NumberFormatException nfe) {
                utils.logIssue("Could not get port number from url [" + url + "]", nfe);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return url != null && host != null && port != -1;
    }

    /**
     * Send stored exception (i.e., crashes) to UDP endpoint
     */
    public void logEvent(StoredException storedException, Callback<Object> callback){
        sendLogEvent(storedException.threadName, storedException.stackTrace, callback);
    }

    /**
     * Send individual log messages (i.e., Log.e() calls) to UDP endpoint
     */
    public void logEvent(String log){
        sendLogEvent("Log", log, null);
    }

    /**
     * Create properly formatted message to send over UDP that acts like a syslog message
     * syslog format: "&lt;priority&gt;timestamp orange_link blue_link: message"
     * Details: http://en.wikipedia.org/wiki/Syslog#Priority
     * @param component Component (eg, Thread name)
     * @param message Message to send
     * @param callback Callback to execute after data is sent.  Can be null.  Will only be
     *                 executed if the data is successfully sent.
     */
    protected void sendLogEvent(String component, String message, Callback<Object> callback){
        String syslogMessage = String.format(Locale.US, "<22>%s %s %s:%s",
                getSysLogFormattedDate(),
                utils.getApplicationName(context),
                component,
                message
        );
        sendDataOverUDP(syslogMessage, callback);
    }

    /**
     * syslog style date formatter
     * @return Current time in syslog format.
     */
    protected String getSysLogFormattedDate() {
        SimpleDateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US);
        Date now = new Date();
        return df.format(now);
    }

    /**
     * Attempt to send syslog-style message over UDP using a DatagramSocket.
     * Errors will be logged but not retried.  UDP is also not guaranteed.
     *
     * @param syslogMessage Message to send
     * @param deleteFileCallback Retrofit Callback.  After data is sent this Callback is executed
     *                           to delete the file that contained the data that was sent.
     */
    protected void sendDataOverUDP(final String syslogMessage, final Callback<Object> deleteFileCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    byte[] message = syslogMessage.getBytes();
                    InetAddress address = InetAddress.getByName(host);
                    DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
                    DatagramSocket datagramSocket = new DatagramSocket();
                    datagramSocket.send(packet);
                    datagramSocket.close();
                    if(deleteFileCallback != null) {
                        deleteFileCallback.success(null, null);
                    }
                } catch (Exception ex) {
                    utils.logIssue("Error sending UDP log message", ex);
                }
            }
        }).start();
    }
}
