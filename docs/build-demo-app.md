# Building the Demo App

Once you register and retrieve the **D2GO keys**, to build properly the Demo App please follow the next:

**1.** Clone the repository (https://github.com/digital2go/digital2go-android-client.git)


**2.** Enter the keys provided by the Platform in the **srings.xml** file.

    ```xml
    <string name="kontakt_io_api_key">YOUR KONTAKTIO API KEY</string>
    <string name="aws_key">YOUR AWS KEY</string>
    <string name="aws_secret">YOUR AWS SECRET</string>
    <string name="d2go_app_name">YOUR D2GO APP USERNAME</string>
    <string name="d2go_app_pass">YOUR D2GO APP PASSWORD</string>
    ```

**3.** Build App

>**note**: Digital2Go Demo App uses Facebook SDK to handle the login. If you want to attach your own Facebook App you need to add the following in the **strings.xml** file. to know more about the facebook sdk [here](https://developers.facebook.com/docs/android/).

```xml
<string name="facebook_app_id">FACEBOOK APP ID</string>    
<string name="fb_login_protocol_scheme">FACEBOOK LOGIN PROTOCOL SCHEME</string>
``` 