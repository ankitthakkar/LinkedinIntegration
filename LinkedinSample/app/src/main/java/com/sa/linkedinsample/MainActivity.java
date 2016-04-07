package com.sa.linkedinsample;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.linkedin.platform.APIHelper;
import com.linkedin.platform.DeepLinkHelper;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.errors.LIDeepLinkError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.listeners.DeepLinkListener;
import com.linkedin.platform.utils.Scope;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String PACKAGE = "com.sa.linkedinsample";

    private static final String host = "api.linkedin.com";
    private static final String topCardUrl = "https://" + host + "/v1/people/~:(first-name,last-name,email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";
    private static final String shareUrl = "https://" + host + "/v1/people/~/shares";



    private Button btnLogin, btnShare,btnConnections,btnOpenProfile,btnUserProfile;
    private TextView txtFirstName,txtLastName;
    private ImageView imgProfilePic;


  /*  private static final String HOST = "api.linkedin.com";
    private static final String FETCH_BASIC_INFO = "https://" + host + "/v1/people/~:(id,first-name,last-name,headline,location,industry)";
    private static final String FETCH_CONTACT = "https://" + host + "/v1/people/~:(num-connections,email-address,phone-numbers,main-address)";
    private static final String FETCH_PROFILE_PIC = "https://" + host + "/v1/people/~:(picture-urls::(original))";
    private static final String SHARE_URL = "https://" + host + "/v1/people/~/shares";*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        txtFirstName = (TextView) findViewById(R.id.txt_first_name_value);

        txtLastName = (TextView) findViewById(R.id.txt_last_name_value);

        btnShare = (Button) findViewById(R.id.btn_share);
        btnShare.setOnClickListener(this);

        btnConnections = (Button) findViewById(R.id.btn_fetch_connections);
        btnConnections.setOnClickListener(this);

        btnOpenProfile = (Button) findViewById(R.id.btn_open_profile);
        btnOpenProfile.setOnClickListener(this);

        btnUserProfile = (Button) findViewById(R.id.btn_open_user_profile);
        btnUserProfile.setOnClickListener(this);

        imgProfilePic=(ImageView)findViewById(R.id.imageView);



        generateHashkey();
    }


    // This Method is used to generate "Android Package Name" hash key

    public void generateHashkey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    PACKAGE,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.e(TAG, "Package Name: " + info.packageName);
                Log.e(TAG, "Base 64 Name: " + Base64.encodeToString(md.digest(),
                        Base64.NO_WRAP));


            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, e.getMessage(), e);
        }
    }

    public void loginLinkedin() {
        LISessionManager.getInstance(getApplicationContext()).init(this,
                buildScope(), new AuthListener() {
                    @Override
                    public void onAuthSuccess() {

                        //Toast.makeText(getApplicationContext(), "success" + LISessionManager.getInstance(getApplicationContext()).getSession().getAccessToken().toString(), Toast.LENGTH_LONG).show();

                        btnLogin.setText("Logout");

                        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
                        apiHelper.getRequest(MainActivity.this, topCardUrl, new ApiListener() {
                            @Override
                            public void onApiSuccess(ApiResponse s) {
                                //((TextView) findViewById(R.id.response)).setText(s.toString());
                                //((TextView) findViewById(R.id.response)).setText(error.toString());
                                Toast.makeText(getApplicationContext(), "Profile fetched successfully " + s.toString(),
                                        Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Profile json" + s.getResponseDataAsJson());
                                Log.e(TAG, "Profile String" + s.getResponseDataAsString());

                               try {
                                   Log.e(TAG, "Profile emailAddress" + s.getResponseDataAsJson().get("emailAddress").toString());
                                   Log.e(TAG, "Profile formattedName" + s.getResponseDataAsJson().get("formattedName").toString());

                                   txtFirstName.setText(s.getResponseDataAsJson().get("emailAddress").toString());
                                   txtLastName.setText(s.getResponseDataAsJson().get("formattedName").toString());
                                   Picasso.with(MainActivity.this).load(s.getResponseDataAsJson().getString("pictureUrl"))
                                           .into(imgProfilePic);

                               }catch (Exception e){

                               }




                            }

                            @Override
                            public void onApiError(LIApiError error) {
                                //((TextView) findViewById(R.id.response)).setText(error.toString());
                                Toast.makeText(getApplicationContext(), "Profile failed " + error.toString(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onAuthError(LIAuthError error) {

                        Toast.makeText(getApplicationContext(), "failed " + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }, true);

       /* String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name)";

        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // Success!
                Toast.makeText(getApplicationContext(), "failed " + apiResponse.toString(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!

                Toast.makeText(getApplicationContext(), "failed " + liApiError.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });*/
    }


    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS, Scope.W_SHARE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this,
                requestCode, resultCode, data);


        /*Intent intent = new Intent(MainActivity.this,HomeActivity.class);
        startActivity(intent);*/

        DeepLinkHelper deepLinkHelper = DeepLinkHelper.getInstance();
        deepLinkHelper.onActivityResult(this, requestCode, resultCode, data);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_login:


                if (isLogin()) {
                    logOut();
                } else {
                    loginLinkedin();
                }


                break;
            case R.id.btn_share:
                shareMessage();
                break;
            case R.id.btn_fetch_connections:
                retrieveConnection();
                break;

            case R.id.btn_open_profile:
                openUserProfile();
               // openOtherUserProfile();
                break;


            case R.id.btn_open_user_profile:
                openOtherUserProfile();
                break;


            default:

        }

    }


    public void shareMessage() {
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.postRequest(MainActivity.this, shareUrl, buildShareMessage("Hello World", "Hello Title", "Hello Descriptions", "http://ankitthakkar90.blogspot.in/", "http://1.bp.blogspot.com/-qffW4zPyThI/VkCSLongZbI/AAAAAAAAC88/oGxWnHRwzBk/s320/10333099_1408666882743423_2079696723_n.png"), new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // ((TextView) findViewById(R.id.response)).setText(apiResponse.toString());
                Toast.makeText(getApplicationContext(), "Share success:  " + apiResponse.toString(),
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "share success" + apiResponse.toString());
            }

            @Override
            public void onApiError(LIApiError error) {
                //   ((TextView) findViewById(R.id.response)).setText(error.toString());
                Toast.makeText(getApplicationContext(), "Share failed " + error.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    public void retrieveConnection(){
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(MainActivity.this, "https://api.linkedin.com/v1/people/~/connections?modified=new", new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse response) {
                Log.e(TAG, "share success" + response.toString());
            }

            @Override
            public void onApiError(LIApiError error) {
                // Lconnection errorare success" + error.toString());
                Log.e(TAG, "share success" + error.toString());
            }
        });
    }

    public void openUserProfile(){
        DeepLinkHelper deepLinkHelper = DeepLinkHelper.getInstance();
        deepLinkHelper.openCurrentProfile(MainActivity.this, new DeepLinkListener() {
            @Override
            public void onDeepLinkSuccess() {
                Log.e(TAG, "openUserProfile success");
            }

            @Override
            public void onDeepLinkError(LIDeepLinkError error) {

                Log.e(TAG, "openUserProfile error" + error.toString());
            }
        });
    }

    public void openOtherUserProfile(){
        DeepLinkHelper deepLinkHelper = DeepLinkHelper.getInstance();
        deepLinkHelper.openOtherProfile(MainActivity.this, "72818350", new DeepLinkListener() {
            @Override
            public void onDeepLinkSuccess() {
                Log.e(TAG, "openOtherUserProfile success");
            }

            @Override
            public void onDeepLinkError(LIDeepLinkError error) {
                Log.e(TAG, "openOtherUserProfile error"+ error.toString());

            }
        });

    }
    private void logOut(){
        LISessionManager.getInstance(getApplicationContext()).clearSession();
        btnLogin.setText("Login");
    }


    public String buildShareMessage(String comment,String title,String descriptions,String linkUrl,String imageUrl  ){
        String shareJsonText = "{ \n" +
                "   \"comment\":\"" + comment + "\"," +
                "   \"visibility\":{ " +
                "      \"code\":\"anyone\"" +
                "   }," +
                "   \"content\":{ " +
                "      \"title\":\""+title+"\"," +
                "      \"description\":\""+descriptions+"\"," +
                "      \"submitted-url\":\""+linkUrl+"\"," +
                "      \"submitted-image-url\":\""+imageUrl+"\"" +
                "   }" +
                "}";
        return shareJsonText;
    }

    private boolean isLogin(){
        LISessionManager sessionManager = LISessionManager.getInstance(getApplicationContext());
        LISession session = sessionManager.getSession();
        boolean accessTokenValid = session.isValid();
        return accessTokenValid;
    }

    private void RetrieveConnections(){
    /*    final Set<ProfileField> connectionFields = EnumSet.of(ProfileField.ID, ProfileField.MAIN_ADDRESS,
                ProfileField.PHONE_NUMBERS, ProfileField.LOCATION,
                ProfileField.LOCATION_COUNTRY, ProfileField.LOCATION_NAME,
                ProfileField.FIRST_NAME, ProfileField.LAST_NAME, ProfileField.HEADLINE,
                ProfileField.INDUSTRY, ProfileField.CURRENT_STATUS,
                ProfileField.CURRENT_STATUS_TIMESTAMP, ProfileField.API_STANDARD_PROFILE_REQUEST,
                ProfileField.EDUCATIONS, ProfileField.PUBLIC_PROFILE_URL, ProfileField.POSITIONS,
                ProfileField.LOCATION, ProfileField.PICTURE_URL);
        Connections connections = client.getConnectionsForCurrentUser(connectionFields);*/
    }


}
