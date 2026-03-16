package com.android.dang.detailFragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.android.dang.R
import com.android.dang.databinding.FragmentDogDetailBinding
import com.android.dang.search.searchItemModel.SearchDogData
import com.bumptech.glide.Glide

class DogDetailFragment : Fragment(R.layout.fragment_dog_detail) {

    private lateinit var detailData : SearchDogData
    private var _binding: FragmentDogDetailBinding? = null
    private val binding: FragmentDogDetailBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDogDetailBinding.bind(view)
        initView()

    }

    private fun initView(){
        Glide.with(this)
            .load(detailData.popfile)
            .into(binding.dogImg)
        val text2 = detailData.kindCd
        val result2 = text2?.replace("[개] ", "")
        binding.dogName.text = result2
        binding.dogId.text = detailData.noticeNo
        var text1 = "# 접수 일시 - ${detailData.age}\n"
        text1 += "# 발견 장소 - ${detailData.happenPlace}\n\n"
        text1 += when (detailData.sexCd) {
            "M" -> "# 성별 - 수컷\n"
            "F" -> "# 성별 - 암컷\n"
            else -> "# 성별 - 미상\n"
        }
        text1 += "# 나이 - ${detailData.age}\n"
        text1 += "# 색상 - ${detailData.colorCd}\n"
        text1 += "# 체중 - ${detailData.weight}\n\n"
        text1 += "# 특징 - ${detailData.specialMark}\n\n"

        text1 += "보호 센터명 - ${detailData.careNm}\n"
        text1 += "보호소 전화 번호 - ${detailData.careTel}\n"
        binding.dogInfo.text = text1
    }

    fun receiveData(data: SearchDogData){
        detailData = data
    }

}