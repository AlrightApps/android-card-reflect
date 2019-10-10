# CardReflectView
#### A card shaped view with a blurry, mirror-like reflection or shadow

This is an attempt at an effect referenced on Reddit [a couple](https://www.reddit.com/r/androiddev/comments/daqy6t/how_to_set_shadow_colour_to_a_cardview_like_this/) of [times](https://www.reddit.com/r/androiddev/comments/cikw81/how_would_you_implement_adaptive_cardview/) I liked.

![Effect sample](github_image.gif "Logo Title Text 1")

We draw a rounded rectangle with a reflection like shadow underneath. The reflection is blurred using renderscript, then a blur mask paint filter is applied to give the edges some fuzziness.

Usage:

```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:card_view="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_horizontal"
        android:paddingLeft="16dp"
        android:paddingRight="16dp"
        android:paddingTop="16dp"
        android:paddingBottom="16dp">

    <alright.apps.reflectivecard.CardReflectView
            android:id="@+id/card_image"
            android:layout_width="match_parent"
            android:layout_height="300dp"
            card_view:reflect_size="44dp"
            card_view:reflect_elevation="0dp"
            card_view:reflect_corner_radius="16dp"
            card_view:reflect_image_side_padding="16dp"/>

</FrameLayout>
```

Set the image in code like this:
```
view.card_image.setCardImage(R.drawable.myImage)
```

- `reflect_size` is the distance between the top and bottom of the reflection
- `reflect_elevation` is the distance between the bottom of the card and the top of the elevation
- `reflect_corner_radius` is the corner radius of the card and the reflection
- `reflect_image_side_padding` is the padding on at the start and end of the card. This allows the reflection sides to be "fuzzy"

The project is hosted on Jitpack:
```
allprojects {
 repositories {
    ...
    maven { url 'https://jitpack.io' }
 }
}
```
`implementation 'com.github.AlrightApps:android-card-reflect:0.1'`


#### Problems / desired features:
 - Right now each view takes about **40-50** ms to render on my Gen 1 Razer phone. This is obviously a little too long, so any optimization suggestions are welcome! (this is my first time making a custom view!)
 - The `setCardImage` function uses a drawable resource. Passing in a dynamic bitmap here should be easy, I just haven't implemented it yet.
 - More attribute properties could be added. In particular blurriness, or the shape of the view (it doesn't really need to be a roundrect).
 - There is no sanitization of the attributes, the `reflect_size` + `reflect_elevation` for example should never exceed the height of the view
