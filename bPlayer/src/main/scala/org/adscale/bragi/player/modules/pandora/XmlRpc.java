package org.adscale.bragi.player.modules.pandora;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlRpc {

    private static final Logger logger = Logger.getLogger(XmlRpc.class.getName());

    private HttpPost postMethod;

    private CloseableHttpClient client;


    public XmlRpc(String url){
        XmlRpcClientConfigImpl xmlRpcClientConfig = new XmlRpcClientConfigImpl();
        try {
            URI uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        client = HttpClients.createDefault();
        postMethod = new HttpPost();
    }


    /*
     * This method is extracted from the parent class with slight modifications for sending a request with a
     * predetermined body content.
     */
    public Object callWithBody(String url, String body) throws Exception {
        // logger.info("url = " + url);
        // logger.info("body = " + body);
        postMethod.setURI(URI.create(url));

        try {
            // set POST body
            HttpEntity entity = new StringEntity(body);
            postMethod.setEntity(entity);

            // Log.d(Tag.LOG, "ros HTTP POST");
            // execute HTTP POST request
            HttpResponse response = client.execute(postMethod);
            // Log.d(Tag.LOG, "ros HTTP POSTed");

            // check status code
            int statusCode = response.getStatusLine().getStatusCode();
            // Log.d(Tag.LOG, "ros status code:" + statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("HTTP status code: " + statusCode + " != " + HttpStatus.SC_OK);
            }

            // parse response stuff
            //
            // setup pull parser
            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
            entity = response.getEntity();
            Reader reader = new InputStreamReader(new BufferedInputStream(entity.getContent()), "UTF-8");
            // for testing purposes only
            // reader = new StringReader("<?xml
            // version='1.0'?><methodResponse><params><param><value>\n\n\n</value></param></params></methodResponse>");
            pullParser.setInput(reader);

            // lets start pulling...
            pullParser.nextTag();
            pullParser.require(XmlPullParser.START_TAG, null, Tag.METHOD_RESPONSE);

            pullParser.nextTag(); // either Tag.PARAMS (<params>) or Tag.FAULT (<fault>)
            String tag = pullParser.getName();
            if (tag.equals(Tag.PARAMS)) {
                // normal response
                pullParser.nextTag(); // Tag.PARAM (<param>)
                pullParser.require(XmlPullParser.START_TAG, null, Tag.PARAM);
                pullParser.nextTag(); // Tag.VALUE (<value>)
                // no parser.require() here since its called in XMLRPCSerializer.deserialize() below

                // deserialize result
//                Object obj = iXMLRPCSerializer.deserialize(pullParser);
                entity.consumeContent();
                return null;
            }
            else if (tag.equals(Tag.FAULT)) {
                // fault response
                pullParser.nextTag(); // Tag.VALUE (<value>)
                // no parser.require() here since its called in XMLRPCSerializer.deserialize() below

                // deserialize fault result
//                Map<String, Object> map = (Map<String, Object>) iXMLRPCSerializer.deserialize(pullParser);
//                String faultString = (String) map.get(Tag.FAULT_STRING);
//                int faultCode = (Integer) map.get(Tag.FAULT_CODE);
                entity.consumeContent();
                throw new RuntimeException("fault occured");
            }
            else {
                entity.consumeContent();
                throw new Exception("Bad tag <" + tag + "> in XMLRPC response - neither <params> nor <fault>");
            }
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught.", e);
            ;
            // wrap any other Exception(s) around XMLRPCException
            throw new Exception(e);
        }
    }


    public void addHeader(String header, String value) {
        postMethod.addHeader(header, value);
    }


    public static String value(String v) {
        return "<value><string>" + v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</string></value>";
    }


    public static String value(boolean v) {
        return v ? "<value><boolean>1</boolean></value>" : "<value><boolean>0</boolean></value>";
    }


    public static String value(int v) {
        return "<value><int>" + String.valueOf(v) + "</int></value>";
    }


    public static String value(Number v) {
        return value(v.intValue());
    }


    public static String value(String[] list) {
        StringBuilder result = new StringBuilder("<value><array><data>");
        for (int i = 0; i < list.length; i++) {
            result.append(value(list[i]));
        }
        return result.append("</data></array></value>").toString();
    }


    public static String value(int[] list) {
        StringBuilder result = new StringBuilder("<value><array><data>");
        for (int i = 0; i < list.length; i++) {
            result.append(value(list[i]));
        }
        return result.append("</data></array></value>").toString();
    }


    public static String value(AbstractCollection<?> list) {
        StringBuilder result = new StringBuilder("<value><array><data>");
        Iterator<?> listIter = list.iterator();
        while (listIter.hasNext()) {
            result.append(valueGuess(listIter.next()));
        }
        return result.append("</data></array></value>").toString();
    }


    public static String valueGuess(Object v) {
        if (v instanceof Number) return value((Number) v);
        else if (v instanceof Boolean) return value((Boolean) v);
        else if (v instanceof String) return value((String) v);
        else if (v instanceof AbstractCollection<?>) return value((AbstractCollection<?>) v);
        else return value(v.toString());
    }


    public static String makeCall(String method, ArrayList<Object> args) {
        StringBuilder argsStr = new StringBuilder();
        Iterator<Object> argsIter = args.iterator();
        while (argsIter.hasNext()) {
            Object item = argsIter.next();
            argsStr.append("<param>").append(valueGuess(item)).append("</param>");
        }
        return "<?xml version=\"1.0\"?><methodCall><methodName>" + method + "</methodName><params>" + argsStr.toString() + "</params></methodCall>";
    }
}
