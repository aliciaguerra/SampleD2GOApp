## Handle Campaigns ##

When the sdk finds a campaign, it sends the response through a broadcast receiver action. To handle them you need to do the following:

**1.** Create the BroadcastReceiver in your **AndroidManifest.xml**

```xml
<receiver
	android:name="YOUR RECEIVER"
	android:enabled="true">
	<intent-filter>
		<action android:name="com.d2go.sdk.beaconimpression.your-app-name-without-spaces" />
		<action android:name="com.d2go.sdk.geofenceimpression.your-app-name-without-spaces" />
	</intent-filter>
</receiver>
```

**2.** Catch the campaigns in your code:

```java
public class YOUR-RECEIVER extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	String campaign = intent.getStringExtra("campaign");
	... 
	}
}
```

>**Note**: The campaign string comes as a **JSON** object with the following structure

```json
{
	"id": 123428614,
	"campaign_content_touch_id": 185893592,
	"campaign_content_near_id": 185893592,
	"campaign_content_far_id": 185893592,
	"campaign_content": {
		"name": "Test Campaign",
		"description": "Test Campaign",
		"notification_message": "This is a notification message",
		"layout": "MISC",
		"sub_layout": "b_video_with_image",
		"check_video": "D2GO",
		"check_image": "D2GO",
		"header_title": "",
		"video_description_text": "This is my video description text right here",
		"product_name": "",
		"product_description": "",
		"product_price": null,
		"interaction_method": "BUTTONS",
		"campaign_content_actions": {},
		"campaign_content_buttons": {
			"id": 33,
			"campaign_content_id": 147750423,
			"is_primary": true,
			"video_id": null,
			"action": "OPEN_URL",
			"data": "http://www.sahuarolabs.com/",
			"color": "",
			"label": "Open URL",
			"created": "2016-10-20T04:44:41+11:00",
			"modified": "2016-10-20T04:44:41+11:00"
		},
		"media_video": {
			"id": 35673071,
			"filename": "24225.753828878946.c72a10d9a653b2b1ccf39689a775b7ea.mov",
			"encoded_file": "https://cmsdev.deliverables.digital2go.com/uploads/MediaLibrary/24225/35673071/452609.24225.599864954578.mp4",
			"video_thumb": "https://cmsdev.deliverables.digital2go.com/uploads/MediaLibrary/24225/35673071/452609.24225.599854854578.jpg",
			"video_sthumb": "https://cmsdev.deliverables.digital2go.com/uploads/MediaLibrary/24225/35673071/thumbnail-452609.24225.59994954578.jpg",
			"duration": 22732,
			"path": "uploads/MediaLibrary/24225/3567471/",
			"mimetype": "video/quicktime",
			"filesize": 14375773,
			"title": null,
			"description": null,
			"width": 1280,
			"height": 720,
			"media_status": "COMPLETE"
		},
		"media_image": {
			"id": 194785093,
			"filename": "24225.991990563004.80f6876f1e51eecf6f884a8801abe819_cropped_Q4fNnB.jpg",
			"path": "uploads/MediaLibrary/24225/7654671/",
			"mimetype": "image/jpeg",
			"filesize": 474305,
			"title": null,
			"description": null,
			"width": 1280,
			"height": 1706,
			"media_status": "EDITED"
			},
			"id": 185893592
		},
		"media_cdn": "https://cmsdev.deliverables.digital2go.com",
		"impression_id": 1742945,
		"personas": {
		"age": "18-24",
		"gender": "male"
	}
}
```