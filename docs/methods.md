## Methods ##

The following are the usable methods exposed by the sdk, here are described how to use them and it’s needs to make it work.

- **D2GOSDK.init(Context context, String token)**

	To make use of the sdk properly, you need to start with the init. *Context* stands for the application context and the *token* attribute stands for value provided by FCM.

	**sample** 

	```java
	public class Activity extends AppCompatActivity {
	private String firebaseToken;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity);
			firebaseToken = FirebaseInstanceId.getInstance().getToken();
			... 
		}
		@Override
		protected void onResume() {
			super.onResume();
			D2GOSDK.init(getApplicationContext(), firebaseToken);
			...
		}
	}
	```

* **D2GOSDK.getTiles(Context context)**

	This method retrieves the tiles from the platform, it returns a JSONArray if tiles found, null otherwise.
	
	The JSONArray structure comes like this:

	```json
	[
		{
			"image": "https://d2go-media.s3.us-west-2.amazonaws.com/screens/f925d076-32d3-11e6-818b-848f69aa5c09/logo%20%281%29.png",
			"title": "This is the first slide",
			"content": "This is some example content returned from the screens",
			"link": "https://www.google.com",
			"screen_order": 1,
			"created": "2017-12-15T04:09:05+11:00",
			"modified": "2017-12-15T04:39:48+11:00"
		},
		{
			"image": "https://d2go-media.s3.us-west-2.amazonaws.com/screens/f925d076-32d3-11e6-818b-848f69aa5c09/devices.png",
			"title": "This is the second slide",
			"content": "This is some example content returned from the screens",
			"link": "https://www.facebook.com",
			"screen_order": 2,
			"created": "2017-12-15T04:09:05+11:00",
			"modified": "2017-12-15T04:39:48+11:00"
		},
		{
			"image": "https://d2go-media.s3.us-west-2.amazonaws.com/screens/f925d076-32d3-11e6-818b-848f69aa5c09/cakelogo3.jpg",
			"title": "This is the third slide",
			"content": "This is some example content returned from the screens",
			"link": "https://www.yahoo.com",
			"screen_order": 3,
			"created": "2017-12-15T04:09:05+11:00",
			"modified": "2017-12-15T04:39:48+11:00"
		}
	]
	```

- **D2GOSDK.updateDemographics(JSONArray demographics, Context context)**

	In order to start the monitoring services to look for campaigns you need to update the demographics stored in memory, if not you won’t be able to start those services and the SDK will return an **IllegalStateException**
	The JSONArray object should have the following structure:

	```json
	[
		{
			"age":23,
			"gender":"m",
			"city":"Boston"
		}
	]
	```

* **D2GOSDK.startBeaconService() & D2GOSDK.startLocationService()**

	This methods runs the services to monitor campaigns around, in order to begin this services you need to D2GOSDK.init(...) and D2GOSDK.updateDemographics(...) before, otherwise you will get the following exceptions:

	- **IllegalStateException**: if not demographics found when attempting to start the service
	- **LoginException**: if not connected to the platform
	- **SDKException**: if not initialized


- **D2GOSDK.isConnected(Context context)**

	Retrieves the actual state of the SDK:
	* **True** if already initizlied
	* **False** if not initialized or disconnected

* **D2GOSDK.registerInteraction(JSONObject campaign, Context context)**

	To keep track of the user interactions between campaigns you need to send like the following:

	```json
	{
		"campaign_id": 123428614,
		"impression_id": 1742860,
		"device_id": "1AB422B56B3B49AF8389268A9ACD7246",
		"action": "OPEN_URL",
		"timestamp": "2016-08-22 00:00:01"
	}
	```

- **D2GOSDK.changeDeviceId(Context context)**

	Updates the current device id to the platform, just by calling the method.
	It returns **SDKException** if the sdk is not connected to the platform.

* **D2GOSDK.stopLocationService() & D2GOSDK.stopBeaconService()**

	Disables and Shutdow the monitor services for each side (beacons or geofences).

- **D2GOSDK.stop()**

	Stops the SDK functions, the monitor services and disconnects the SDK from the API.
