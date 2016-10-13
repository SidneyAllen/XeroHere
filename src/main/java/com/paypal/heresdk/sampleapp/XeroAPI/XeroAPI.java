package com.paypal.heresdk.sampleapp.XeroAPI;

/**
 * Created by steven.cooper on 12/10/2016.
 */

import android.content.Context;
import android.util.Base64;
import android.util.Log;


import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.services.RSASha1SignatureService;
import com.github.scribejava.core.services.SignatureService;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by Bec Martin on 24/07/16.
 */
public class XeroAPI extends DefaultApi10a {


    Context context;

    public XeroAPI(Context context) {
        this.context = context;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return null;
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return null;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return null;
    }


    @Override
    public SignatureService getSignatureService() {
        return new RSASha1SignatureService(getPrivateKey());
    }

    //This is an alternative that is currently not usable due to Android ASN1 encoding/decoding issues
    public PrivateKey get() {

        try{
            InputStream is = context.getAssets().open("xero_privatekey.pcks8");
            byte[] fileBytes=new byte[is.available()];
            is.read( fileBytes);
            is.close();


            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(fileBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }catch (Exception e){
            Log.e("API Exception", e.getLocalizedMessage());
        }

        return null;

    }

    private static PrivateKey getPrivateKey()
    {
        /*You can copy and past the key from your pcks8 file here
        keep the new line formatting and remove the header and footer
        you should replace this with your own private key and this has been replaced
        for my demo app - this is not ideal as it's not a super awesome idea
        to have this as a string unless you trust the phone
        this could be extracted from the apk on a maliciously rooted file*/
        String str = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALfP0HccVobkSLHE\n" +
                "jpzSEzQeWZLXbCMUhqWVbXcgHxADp1k4zoCOHJyMBD489nSop/2DSccw0XVxmPf4\n" +
                "xwLsPDo65LmV4ZpyZNS3ufUegjyKw5iMeY8EMMnr+TUiQG+ca7vUOM9Zwkesu3kn\n" +
                "cn7u38El5ELEMF89gwhtgz4+UCGXAgMBAAECgYEAm8PGw/lGtf7kP4jy1aAMWAFU\n" +
                "JOTbJYsyY0WSwv18dUSzXx+Nl1FWm67ntZpRvCBkY6gLMHqFNcIzQAgZFbf9fGaj\n" +
                "lBR0tEgLL4NzVG0ARD+kZAeWbqN2GMSCD4IDM8tEnE1TqGStuBjFwrvqiX0JEO90\n" +
                "WqF4Ntwj/Um1i6yNVvkCQQDnCbeFTouoLXSjT9gHs1W9OC7nJ/ukCDNSgxRYLfkV\n" +
                "sIXV0rufOLUQ+BLpB57zN3YRqV8EePhB1tpaMQbG6GqjAkEAy6veGwZ30JgApUBn\n" +
                "CKda7UYReq2kFx3z+Nx7my0Oauy4J1KJ3EKg2FVSPWp8Yc+7jdLhLXYHPJlAURqE\n" +
                "kKywfQJAGg1Qcmm++cHWayvvsj6YpnOVZl9t0rtR/jXlr2PPuJBlJw1SNswQLs+q\n" +
                "YjiHxek1GRy+KCTz6YiuqojouLkwfQJBAKTQvfmMsGSivNL9XOOJ2nDsJLTr0z/J\n" +
                "w2Z9taELQwGP8i4QAvO1D0hBMz3z8BeO7S2FmycYvh5xvbRkCyawOFkCQGzxNg/r\n" +
                "vKn4+Ck2NPKMCM7aQz2ZaPMlCCDnEPv4QjlU6/5+7jztNIwgQCTRH/E6JQzNqm5z\n" +
                "WJWoE1j/ssAPPdw=";

        try
        {
            KeyFactory fac = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(str.getBytes(), Base64.DEFAULT));
            return fac.generatePrivate(privKeySpec);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
