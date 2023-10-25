# Easy Scanner
Anroid library handling Qr Scanning and barcode scanner in easy and fast way 

- Easy API for launching the QR scanner and receiving results by using the new Activity Result API.
- Modern design, edge-to-edge scanning view with multilingual user hint.
- Android Jetpack CameraX for communicating with the camera and showing the preview.
- ML Kit Vision API for best, fully on-device barcode recognition and decoding.


Qr Scanner                                                |  BarCode Scanner            
:-------------------------------------------------------: |:-------------------------:
<img src="Screenshot_1698219008.png" width="300">  |  <img src="Screenshot_1698219030.png" width="300"> 




# Download
--------
Gradle:

```gradle
repositories {
   maven { url 'https://jitpack.io' }
}

build.gradle (app)

dependencies {
	implementation 'com.github.BasemNasr:EasyScanner:v1.0.0'
}
```



# Easy Way To Using Library
```kotlin
private val scanQrCode = registerForActivityResult(ScanCustomCode(), ::onScanResult)
// QR SCANNER
scanQrCode.launch(
     ScannerConfig.build {
          setShowTextAction(true, getString(R.string.or_enter_code_number))
                setShowCloseButton(true)
         }
)

```
```kotlin
class PickerProfileFragment : Fragment(), OnCaptureMedia {
    private lateinit var easyPicker: FragmentEasyPicker
    var mProfileImagePath = ""
    .
    .
    .
    
    override fun onCreate(savedInstanceState: Bundle?) {
       .
       setUpImagePicker()
       btn.setOnClickListener {
            easyPicker.chooseImage()
       }
    }
    
    private fun setUpImagePicker() {
      easyPicker =  FragmentEasyPicker.Builder(this@PickerProfileFragment)
                .setRequestCode(MainActivity.PICK_PROFILE_IMAGE)
                .setListener(this@PickerProfileFragment).build()
    }
    
    override fun onCaptureMedia(request: Int, files: ArrayList<FileResource>?) {
        when (request) {
            PICK_PROFILE_IMAGE -> {
               // getting file path (file.path)
		files?.let {
                    mProfileImagePath = files[0]?.path ?: ""
                    Glide.with(requireActivity()).load(mProfileImagePath)
                        .into(requireView().findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
                }
            }
        }
    }
    
}

```
 
# Other Features
```kotlin
  
  // choose image
  easyPicker.chooseImage()
  
 // choose And Compress Image
  easyPicker.chooseAndCompressImage()
  
   // choose Video From Gallery
  easyPicker.chooseVideo()
  
  //choose file and getting file path
    easyPicker.chooseFile()
    
      
  //chooseMultipleImages
 private lateinit var multiImagesEasyPicker: EasyPicker
 multiImagesEasyPicker =
            EasyPicker.Builder(this@SecondFragment)
                .setRequestCode(MainActivity.PICK_IMAGES)
                .setListener(this@SecondFragment)
                .setMaxSelectionLimit(5)
                .build()
 multiImagesEasyPicker.chooseMultipleImages()




```




Pull requests are the best way to propose changes to the codebase (we use [Github Flow](https://guides.github.com/introduction/flow/index.html)). We actively welcome your pull requests:

1. Fork the repository and create your branch from `master`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code lints.
6. Issue that pull request!
7. Always add a `README` and/or `requirements.txt` to your added code.

## Report bugs using Github's [issues](https://github.com/BasemNasr/EasyMediaPicker/issues)
We use GitHub issues to track public bugs. Report a bug by opening a new issue it's that easy!

*Great Bug Reports* tend to have:

- A quick summary and/or background
- Steps to reproduce
    - Be specific!
    - Give sample code if you can.
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

People love thorough bug reports. I'm not even kidding.




