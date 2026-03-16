package com.android.dang.shelter.vm

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.dang.retrofit.Constants
import com.android.dang.retrofit.DangClient
import com.android.dang.retrofit.abandonedDog.AbandonedDogRes
import com.android.dang.retrofit.abandonedDog.AbandonedShelter
import com.android.dang.retrofit.kind.Items
import com.android.dang.retrofit.sido.Sido
import com.android.dang.retrofit.sido.SidoRes
import com.google.firebase.firestore.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ShelterViewModel : ViewModel() {
    private lateinit var geocoder: Geocoder

    val sido: LiveData<Items<Sido>>
        get() = _sido
    private val _sido = MutableLiveData<Items<Sido>>()

    val sigungu: LiveData<Items<Sido>>
        get() = _sigungu
    private val _sigungu = MutableLiveData<Items<Sido>>()

    private val orgCode: LiveData<String>
        get() = _orgCode
    private val _orgCode = MutableLiveData("")

    private val uprCode: LiveData<String>
        get() = _uprCode
    private val _uprCode = MutableLiveData("")

    val abandonedDogsList: LiveData<List<AbandonedShelter>>
        get() = _abandonedDogsList
    private val _abandonedDogsList = MutableLiveData<List<AbandonedShelter>>(emptyList())

    fun getSidoList() {
        Log.d(Constants.TestTAG, "getSidoList requested")
        DangClient.api.getSidoList().enqueue(object : Callback<SidoRes> {
            override fun onResponse(call: Call<SidoRes>, response: Response<SidoRes>) {
                if (!response.isSuccessful) {
                    Log.e(Constants.TestTAG, "getSidoList failed: ${response.code()}")
                    return
                }

                val items = response.body()?.response?.body?.items
                if (items == null) {
                    Log.e(Constants.TestTAG, "getSidoList returned an empty body")
                    return
                }

                Log.d(Constants.TestTAG, "getSidoList success: ${items.item.size}")
                _sido.value = items
            }

            override fun onFailure(call: Call<SidoRes>, t: Throwable) {
                Log.e(Constants.TestTAG, "getSidoList onFailure: ${t.localizedMessage}", t)
            }
        })
    }

    fun getSigunguList(code: String) {
        Log.d(Constants.TestTAG, "getSigunguList requested: $code")
        DangClient.api.getSigunguList(code = code).enqueue(object : Callback<SidoRes> {
            override fun onResponse(call: Call<SidoRes>, response: Response<SidoRes>) {
                if (!response.isSuccessful) {
                    Log.e(Constants.TestTAG, "getSigunguList failed: ${response.code()}")
                    return
                }

                val sigunguList = response.body()?.response?.body?.items
                if (sigunguList == null) {
                    Log.e(Constants.TestTAG, "getSigunguList returned an empty body")
                    _sigungu.value = Items(emptyList())
                    return
                }

                Log.d(Constants.TestTAG, "getSigunguList success: ${sigunguList.item.size}")
                _sigungu.value = sigunguList
            }

            override fun onFailure(call: Call<SidoRes>, t: Throwable) {
                Log.e(Constants.TestTAG, "getSigunguList onFailure: ${t.localizedMessage}", t)
            }
        })
    }

    fun getAbandonedDogs() {
        Log.d(Constants.TestTAG, "getAbandonedDogs requested: org=${orgCode.value}, upr=${uprCode.value}")
        DangClient.api.abandonedDogShelter(
            uprCode = uprCode.value,
            orgCode = orgCode.value,
            upkind = 417000,
            numOfRows = 10
        ).enqueue(object : Callback<AbandonedDogRes?> {
            override fun onResponse(
                call: Call<AbandonedDogRes?>,
                response: Response<AbandonedDogRes?>
            ) {
                if (!response.isSuccessful) {
                    Log.e(Constants.TestTAG, "getAbandonedDogs failed: ${response.code()}")
                    return
                }

                val dogs = response.body()
                    ?.response
                    ?.body
                    ?.items
                    ?.item
                    .orEmpty()
                    .mapNotNull { dog ->
                        dog?.copy(pos = findGeoPoint(dog.careAddr.orEmpty()))
                    }

                _abandonedDogsList.value = dogs
                Log.d(Constants.TestTAG, "getAbandonedDogs success: ${dogs.size}")
            }

            override fun onFailure(call: Call<AbandonedDogRes?>, t: Throwable) {
                Log.e(Constants.TestTAG, "getAbandonedDogs onFailure: ${t.localizedMessage}", t)
            }
        })
    }

    fun setOrgCode(orgCode: String) {
        Log.d(Constants.TestTAG, "setOrgCode: $orgCode")
        _orgCode.value = orgCode
    }

    fun setUprCode(uprCode: String) {
        Log.d(Constants.TestTAG, "setUprCode: $uprCode")
        _uprCode.value = uprCode
    }

    fun getShelterInfo(desertionNo: String): AbandonedShelter? {
        return abandonedDogsList.value?.find {
            it.desertionNo == desertionNo
        }
    }

    fun findGeoPoint(address: String): GeoPoint? {
        var location: GeoPoint? = null
        try {
            val listAddress: List<Address> = geocoder.getFromLocationName(address, 1).orEmpty()
            if (listAddress.isNotEmpty()) {
                val addr = listAddress[0]
                val lat = addr.latitude
                val lng = addr.longitude
                Log.d(Constants.TestTAG, "findGeoPoint: $lat / $lng")
                location = GeoPoint(lat, lng)
            }
        } catch (e: IOException) {
            Log.e(Constants.TestTAG, "findGeoPoint failed: ${e.localizedMessage}", e)
        }
        return location
    }

    fun setGeoCoder(geocoder: Geocoder) {
        this.geocoder = geocoder
    }
}
