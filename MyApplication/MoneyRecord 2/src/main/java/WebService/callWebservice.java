package WebService;

import android.os.AsyncTask;
import android.util.Log;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chen on 2016/9/13.
 */
public class callWebservice {
    String tag="MoneyRecord";
    //final String WEB_SERVICE_URL = "http://192.168.1.102:12500/WebService1.asmx?wsdl";
    final String WEB_SERVICE_URL = "http://3106289a.nat123.net:12500/WebService1.asmx?wsdl";
    final String Namespace = "http://tempuri.org/";//命名空间
    /**
     * 调用WebService
     *
     * @return WebService的返回值
     *
     */
    public String CallWebService(String MethodName, Map<String, String> Params) {
        // 1、指定webservice的命名空间和调用的方法名
        Log.e(tag,"指定webservice的命名空间和调用的方法名");
        SoapObject request = new SoapObject(Namespace, MethodName);
        // 2、设置调用方法的参数值，如果没有参数，可以省略，
        Log.e(tag,"设置调用方法的参数值，如果没有参数，可以省略，");
        if (Params != null) {
            //Log.e(tag,Params.get(0).toString()+"/"+Params.get(1).toString());
            Iterator iter = Params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                request.addProperty((String) entry.getKey(),
                        (String) entry.getValue());
            }
        }
        // 3、生成调用Webservice方法的SOAP请求信息。该信息由SoapSerializationEnvelope对象描述
        Log.e(tag,"生成调用Webservice方法的SOAP请求信息。该信息由SoapSerializationEnvelope对象描述");
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // c#写的应用程序必须加上这句
        envelope.dotNet = true;
        HttpTransportSE ht = new HttpTransportSE(WEB_SERVICE_URL);
        // 使用call方法调用WebService方法
        Log.e(tag,"使用call方法调用WebService方法");
        try {
            ht.call(null, envelope);
            Log.e(tag,"呼叫成功");
        } catch (HttpResponseException e) {
            Log.e(tag,"HttpResponseException: ",e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(tag,"IOException: ",e);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.e(tag,"XmlPullParserException: ",e);
            e.printStackTrace();
        }
        try {
            final SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            if (result != null) {
                Log.e(tag, "----收到的回复----"+result.toString());
                return result.toString();
            }

        } catch (SoapFault e) {
            Log.e(tag, "SoapFault: "+e);
            e.printStackTrace();
        }
        return null;
    }
}
