package com.android.dang.shelter.view

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.dang.R
import com.android.dang.databinding.FragmentShelterBinding
import com.android.dang.retrofit.Constants
import com.android.dang.retrofit.abandonedDog.AbandonedShelter
import com.android.dang.retrofit.kind.Items
import com.android.dang.retrofit.sido.Sido
import com.android.dang.shelter.vm.ShelterViewModel
import com.google.firebase.firestore.GeoPoint
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.mapwidget.InfoWindowOptions
import com.kakao.vectormap.mapwidget.component.GuiImage
import com.kakao.vectormap.mapwidget.component.GuiLayout
import com.kakao.vectormap.mapwidget.component.GuiText
import com.kakao.vectormap.mapwidget.component.Orientation

class ShelterFragment : Fragment() {
    private lateinit var binding: FragmentShelterBinding
    private lateinit var viewModel: ShelterViewModel
    private var kakaoMap: KakaoMap? = null
    private val duration = 500

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShelterBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ShelterViewModel::class.java]

        viewModel.run {
            setGeoCoder(Geocoder(requireContext()))
            sido.observe(viewLifecycleOwner, sidoObserver)
            sigungu.observe(viewLifecycleOwner, sigunguObserver)
            abandonedDogsList.observe(viewLifecycleOwner, abandonedDogObserver)
        }

        Log.d(Constants.TestTAG, "Starting Kakao MapView")
        binding.mapView.start(object : KakaoMapReadyCallback() {
            override fun getPosition(): LatLng = LatLng.from(37.393865, 127.115795)

            override fun onMapReady(kakaoMap: KakaoMap) {
                this@ShelterFragment.kakaoMap = kakaoMap
                Log.d(Constants.TestTAG, "KakaoMap is ready")
                renderDogsOnMap(viewModel.abandonedDogsList.value.orEmpty())
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(Constants.TestTAG, "Requesting initial shelter region list")
        viewModel.getSidoList()

        binding.selectLocationMain.setOnClickListener {
            viewModel.sido.value?.item?.let { list ->
                val builder = SidoDialog(list, onClickSido)
                builder.show(childFragmentManager, "")
            }
        }

        binding.selectLocationDetail.setOnClickListener {
            viewModel.sigungu.value?.let { list ->
                if (list.item.isEmpty()) {
                    return@setOnClickListener
                }
                val builder = SidoDialog(list.item, onClickSigungu)
                builder.show(childFragmentManager, "")
            }
        }
    }

    private fun setLabel(geoPoint: GeoPoint, dog: AbandonedShelter) {
        val pos = LatLng.from(geoPoint.latitude, geoPoint.longitude)
        val styles = kakaoMap?.labelManager
            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.icon_pink_marker)))
        val options = LabelOptions.from(pos)
            .setStyles(styles)
            .setClickable(true)

        val layer = kakaoMap?.labelManager?.layer
        val label: Label? = layer?.addLabel(options)
        label?.tag = dog.desertionNo

        Log.d(Constants.TestTAG, "setPin: ${label?.labelId}")
        kakaoMap?.setOnLabelClickListener { _, _, clickedLabel ->
            viewModel.getShelterInfo(clickedLabel.tag as String)?.let {
                setShelterInfo(it)
                showInfoWindow(it)
            }
        }
        kakaoMap?.moveCamera(
            CameraUpdateFactory.newCenterPosition(pos, 13),
            CameraAnimation.from(duration)
        )
    }

    private val onClickSido = { sido: Sido ->
        Log.d(Constants.TestTAG, "onClickSido: $sido")
        viewModel.setUprCode(sido.orgCd)
        viewModel.getSigunguList(sido.orgCd)
        binding.selectLocationMain.text = sido.orgdownNm
    }

    private val onClickSigungu = { sigungu: Sido ->
        viewModel.setOrgCode(sigungu.orgCd)
        binding.selectLocationDetail.text = sigungu.orgdownNm
        viewModel.getAbandonedDogs()
    }

    private val sidoObserver = Observer<Items<Sido>> { sidoList ->
        val defaultSido = sidoList.item.firstOrNull() ?: return@Observer
        binding.selectLocationMain.text = defaultSido.orgdownNm
        viewModel.setUprCode(defaultSido.orgCd)
        viewModel.getSigunguList(defaultSido.orgCd)
    }

    private val sigunguObserver = Observer<Items<Sido>> { sigunguList ->
        removeAllMarkers()
        val defaultSigungu = sigunguList.item.firstOrNull()
        if (defaultSigungu != null) {
            binding.selectLocationDetail.text = defaultSigungu.orgdownNm
            viewModel.setOrgCode(defaultSigungu.orgCd)
            viewModel.getAbandonedDogs()
            return@Observer
        }
        binding.selectLocationDetail.text = ""
    }

    private val abandonedDogObserver = Observer<List<AbandonedShelter>> { dogs ->
        Log.d(Constants.TestTAG, "abandonedDogs updated: ${dogs.size}")
        renderDogsOnMap(dogs)
    }

    private fun renderDogsOnMap(dogs: List<AbandonedShelter>) {
        if (kakaoMap == null) {
            Log.d(Constants.TestTAG, "Skipping marker render because KakaoMap is not ready yet")
            return
        }

        removeAllMarkers()
        dogs.forEach { dog ->
            dog.pos?.let { setLabel(it, dog) }
        }
        Log.d(Constants.TestTAG, "Rendered shelter markers: ${dogs.size}")
    }

    private fun setShelterInfo(dog: AbandonedShelter) = with(binding) {
        shelterName.text = dog.careNm
        shelterLocation.text = dog.careAddr
        shelterPhone.text = dog.careTel
    }

    private fun showInfoWindow(dog: AbandonedShelter) {
        val dogPos = dog.pos ?: return
        kakaoMap?.mapWidgetManager?.infoWindowLayer?.removeAll()

        val pos = LatLng.from(dogPos.latitude, dogPos.longitude)
        val body = GuiLayout(Orientation.Horizontal)
        body.setPadding(15, 15, 15, 15)

        val bgImage = GuiImage(R.drawable.icon_window_body, true)
        bgImage.setFixedArea(5, 5, 5, 5)
        body.setBackground(bgImage)

        val text = GuiText(dog.careNm)
        text.setTextSize(25)
        body.addView(text)

        val options = InfoWindowOptions.from(pos)
        options.setBody(body)
        options.setBodyOffset(0f, -100f)

        kakaoMap?.mapWidgetManager?.infoWindowLayer?.addInfoWindow(options)
    }

    private fun removeAllMarkers() {
        kakaoMap?.labelManager?.layer?.removeAll()
        kakaoMap?.mapWidgetManager?.infoWindowLayer?.removeAll()
        with(binding) {
            shelterName.text = ""
            shelterPhone.text = ""
            shelterLocation.text = ""
        }
    }
}
