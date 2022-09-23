# Team 1

# 🧑‍💻Member

[김현수](https://github.com/KimHance)
[권혁준](https://github.com/DavidKwon7)
[이현섭](https://github.com/leehandsub)

# 🔧 Tech

- MVVM
- Room
- Hilt
- Kakao Map
- DataBinding

# 🗂️ Structure

~~~
├─base
├─data
│  ├─database
│  ├─entity
│  └─repository
├─di
├─feature
│  ├─history
│  │  ├─domain
│  │  │  └─usecase
│  │  └─presentation
│  ├─map
│  │  └─presentation
│  ├─presentation
│  └─set
└─shared
~~~

- data
    - RoomDatabase 빌더 및 Dao
    - repository
        - DB로부터 날짜별 히스토리 가져옴
        - 히스토리 DB에 저장
- di
    - Repository bind
    - Database 및 Dao provide
- feature
    - history
        - doamin
            - UseCase 작성 및 Repository Interface 작성
        - 히스토리 리스트 어댑터 , 바인딩 어댑터 , 히스토리 다이얼로그 작성
    - map
        - BallonAdapter를 통해 마커에 주소 표기
    - set
        - 세팅 시간 저장을 위한 다이얼로그
- shared
    - SharedPreference로 세팅 시간 관리

# 💻 Work

## Room

```kotlin
@Database(
    entities = [History::class],
    version = 1,
    exportSchema = false
)
abstract class MapDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao

    companion object {
        fun getInstance(context: Context): MapDatabase = Room
            .databaseBuilder(context, MapDatabase::class.java, "map.db")
            .build()
    }
}

@Dao
interface MapDao {

    @Query("SELECT * FROM history WHERE date =:date")
    suspend fun getHistoryFromDate(date: String): List<History>

    @Insert
    suspend fun saveHistory(history: History)
}

@Parcelize
@Entity
data class History(
    val time: String,

    val date: String,
    val latitude: Double,
    val longitude: Double,
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
```

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMapDao(appDatabase: MapDatabase) = appDatabase.mapDao()

    @Provides
    @Singleton
    fun provideMapDatabase(
        @ApplicationContextcontext: Context
    ): MapDatabase = MapDatabase.getInstance(context)

}
```

Room 데이터 베이스는 DI를 통해 싱글톤으로 생성하였습니다.

```kotlin
private val _historyFromDate = MutableStateFlow<List<History>>(emptyList())
val historyFromDate = _historyFromDate.asStateFlow()

fun getHistoryFromDate(date: String) {
    viewModelScope.launch {
        _historyFromDate.update {
            getHistoryUseCase(date)
        }
    }
}

fun saveHistory(date: Long, latitude: Double, longitude: Double) {
    viewModelScope.launch {
        saveHistoryUseCase(date, latitude, longitude)
    }
}
```

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        mainViewModel.historyFromDate.collect { historyList ->
            updateMarkerList(historyList.toMapItem())
        }
    }
}
```

뷰에 마커를 띄우기 위해 뷰모델에 히스토리를 담았습니다. 날짜별로 DB에 저장된 히스토리는 StateFlow를 사용하여 collect하여 맵의 마커를 선택된 날짜에 띄우고
있습니다.

```kotlin
private fun collectFlow() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.historyFromDate.collect { historyList ->
                historyAdapter.submitList(historyList.toList())
            }
        }
    }
}
```

히스토리 다이얼로그에서도 뷰모델의 날짜별 히스토리를 collect하여 날짜별로 히스토리 리스트를 띄워주고 있습니다.

```kotlin
fun List<History>.toMapItem(): List<MapPOIItem> {
    return this.map {
        val position = MapPoint.mapPointWithGeoCoord(it.latitude, it.longitude)
        MapPOIItem().apply {
            tag = it.id
            itemName = " "
            mapPoint = position
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
        }
    }
}
```

카카오맵 Api 마커를 찍기위해 히스토리 클래스를 MapPOIItem으로 매퍼를 작성하였습니다.

## 현위치 찾기 마커 찍기

```kotlin
 private fun startTracking() {
    binding.mapView.currentLocationTrackingMode =
        MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    val position = getCurrentLocation()
    if (position != Pair(0.0, 0.0)) {
        val uNowPosition = MapPoint.mapPointWithGeoCoord(position.first, position.second)
        val marker = MapPOIItem().apply {
            itemName = getString(R.string.currentPosition)
            mapPoint = uNowPosition
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
        }
        mainViewModel.saveHistory(getCurrentTime(), position.first, position.second)
        updateHistory()
        Toast.makeText(
            this,
            "lat: ${position.first}, long: ${position.second}",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        Toast.makeText(
            this,
            getString(R.string.failed_to_get_location),
            Toast.LENGTH_SHORT
        ).show()
    }
}
```

location Manager를 사용하여 현재 위치를 가져온 후에 경도 위도 값을 구하였습니다.

## 위도 경도 기반으로 현재 위치 가져오기

```kotlin
private fun getDetailAddress(uLatitude: Double, uLongitude: Double): String {
    val geocoder = Geocoder(context)
    val convertAddress = geocoder
        .getFromLocation(uLatitude, uLongitude, MAX_RESULT)

    if (convertAddress.isEmpty()) {
        return context.getString(R.string.no_detail_location)
    } else {
        return convertAddress.get(ADDRESS).getAddressLine(ADDRESS).toString()
    }
}
}
```

Geocoder.getFromLocation으로 위도, 경도 기반으로 현재 주소를 가져올 수 있었습니다.

## 시간 세팅

```kotlin
btnSetTimePositive.setOnClickListener {
    LocationHistoryApp.prefs.setTime =
        (editSetText.text.toString().toInt() * 60000L).toString()
    dismiss()
}
```

전역으로 있는 SharedPreferences에 세팅되는 시간을 저장

## SharedPreferences 구현

```kotlin
@HiltAndroidApp
class LocationHistoryApp : Application() {
    companion object {
        lateinit var prefs: SharedPreferences
    }

    override fun onCreate() {
        prefs = SharedPreferences
        prefs.init(this)
        super.onCreate()
    }
}
```

```kotlin
object SharedPreferences {
    private const val PREFS_NAME = "prefs_name"
    private const val TIME_KEY = "time_key"
    ...

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var setTime: String?
        get() = prefs.getString(TIME_KEY, "10000")
        set(value) = prefs.edit().putString(TIME_KEY, value).apply()

    ...
}
```

## MainActivity

```kotlin
mainViewModel.apply {
    setTime.observe(this@MainActivity) { time ->
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            time.toString().toLong(),
            10F,
            gpsLocationListener
        )
    }
}
```

livedata를 통해 observe하며 변화된 값을 감지합니다. requestLocationUpdates을 통해 일정시간마다 변화된 좌표를 호출합니다.

# 📱 Result (until now)
![Screenshot_20220923-174610_android-wanted-LocationHistoryApp](https://user-images.githubusercontent.com/86879099/191925248-d6460d32-6351-4f3b-81a3-eead4143ea2f.jpg)
- 스플래시

![Screenshot_20220923-174630_android-wanted-LocationHistoryApp](https://user-images.githubusercontent.com/86879099/191925264-1fe534df-2c3c-467f-a7a6-3fcbd6f8527b.jpg)
- 마커

![Screenshot_20220923-174636_android-wanted-LocationHistoryApp](https://user-images.githubusercontent.com/86879099/191925335-a91c4b21-7017-4fb5-850d-e8d073ee107c.jpg)![Screenshot_20220923-174718_android-wanted-LocationHistoryApp](https://user-images.githubusercontent.com/86879099/191925411-898593ad-914f-45c7-bafa-6629f60be8f4.jpg)
- 현재 위치

![Screenshot_20220923-174642_android-wanted-LocationHistoryApp](https://user-images.githubusercontent.com/86879099/191925322-b255ad89-234e-4dce-988c-9d7402259365.jpg)
![Screenshot_20220923-174649_android-wanted-LocationHistoryApp](https://user-images.githubusercontent.com/86879099/191925352-a24869fe-d9c8-4b0d-91d1-0c0cf397d03b.jpg)

- 날짜별 히스토리

![Screenshot_20220923-174709_android-wanted-LocationHistoryApp](https://user-images.githubusercontent.com/86879099/191925986-ce55da74-0b63-4c6e-9c74-c65602c6572d.jpg)

- 시간 설정
