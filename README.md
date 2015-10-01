### Description

Phonegap plugin which allows the caller to customise a camera preview, including a custom button.

### Using the plugin

- Add the plugin ID and version to the config.xml.

```
<gap:plugin name="com.venkykowshik.plugins.camera" />
```
### Example

```js
navigator.customCamera.getPicture(filename, function success(fileUri) {
    alert("File location: " + fileUri);
}, function failure(error) {
    alert(error);
}, {
    quality: 80,
    targetWidth: 120,
    targetHeight: 120
});
```
