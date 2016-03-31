package com.alisonkaique.lutepel.com.alisonkaique.lutepel.connector;

import android.os.AsyncTask;

import com.alisonkaique.lutepel.SoapSerEnv;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Alison Kaique on 25/01/2016.
 */
public class SoapGetFil extends AsyncTask<String, Void, String> {

    protected static final String NAMESPACE = "http://www.wststlutepel.com.br";
    protected static String URL = "http://192.168.0.228:8093/ws/WSLUGRF.apw";
    protected static final String METHOD_NAME = "GETFIL";
    protected static final String SOAP_ACTION = "http://www.wststlutepel.com.br/GETFIL";

    @Override
    protected String doInBackground(String... params) {

        String response = ""; //Response JOB

        if (params.length > 0)
            try {
                //SOAP
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                //Propriedades //Parametros
                //Empresa
                PropertyInfo company = new PropertyInfo();
                company.setName("CEMP");
                company.setValue(params[0]);
                company.setType(company.STRING_CLASS);
                request.addProperty(company);

                SoapSerEnv envelope = new SoapSerEnv(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.implicitTypes = true;
                envelope.setAddAdornments(false);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

                try {
                    androidHttpTransport.call(SOAP_ACTION, envelope);

                    SoapPrimitive result = (SoapPrimitive) envelope.getResponse();

                    response = result.toString();

                    //Desserializar a String JSON
                    //JSONObject jsonObject = new JSONObject(response.toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                    response = "ERROR IO: " + e1.getMessage();
                } catch (XmlPullParserException e1) {
                    e1.printStackTrace();
                    response = "ERROR XML: " + e1.getMessage();

                } catch (Exception e) {
                    e.printStackTrace();
                    response = "ERROR EX: " + e.getMessage();
                }

            } catch (Exception e) {
                e.printStackTrace();
                response = "CATCH " + e.getMessage();
            }

        return response;
    }
}
