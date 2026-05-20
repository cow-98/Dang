package com.android.dang.detailFragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.dang.R
import com.android.dang.databinding.FragmentDogDetailBinding
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.util.PrefManager
import com.bumptech.glide.Glide

class DogDetailFragment : Fragment(R.layout.fragment_dog_detail) {

    private lateinit var detailData: SearchDogData
    private var _binding: FragmentDogDetailBinding? = null
    private val binding: FragmentDogDetailBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDogDetailBinding.bind(view)
        if (!::detailData.isInitialized) {
            return
        }
        initView()
        bindActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        syncLikeState()

        Glide.with(this)
            .load(detailData.popfile)
            .into(binding.dogImg)

        binding.dogName.text = formatDogName(detailData.kindCd)
        binding.dogId.text = detailData.noticeNo.orEmpty()
        binding.dogInfo.text = buildDogInfo(detailData)
        binding.dogInfoScroll.post {
            binding.dogInfoScroll.scrollTo(0, 0)
        }
        updateLikeIcon()
    }

    private fun bindActions() {
        binding.btnLike.setOnClickListener {
            toggleLike()
        }

        binding.btnShelterCall.setOnClickListener {
            showCallDialog()
        }
    }

    private fun buildDogInfo(data: SearchDogData): String {
        val infoLines = mutableListOf<String>()
        infoLines += "# 발견 장소 - ${valueOrPlaceholder(data.happenPlace)}"
        infoLines += "# 상태 - ${valueOrPlaceholder(data.processState)}"
        infoLines += "# 성별 - ${formatSex(data.sexCd)}"
        infoLines += "# 중성화 - ${formatNeuter(data.neuterYn)}"
        infoLines += "# 나이 - ${valueOrPlaceholder(data.age)}"
        infoLines += "# 색상 - ${valueOrPlaceholder(data.colorCd)}"
        infoLines += "# 체중 - ${valueOrPlaceholder(data.weight)}"
        infoLines += ""
        infoLines += "# 특징 - ${valueOrPlaceholder(data.specialMark)}"
        infoLines += ""
        infoLines += "보호 센터명 - ${valueOrPlaceholder(data.careNm)}"
        infoLines += "보호소 전화 번호 - ${valueOrPlaceholder(data.careTel)}"
        infoLines += "보호소 주소 - ${valueOrPlaceholder(data.careAddr)}"
        return infoLines.joinToString("\n")
    }

    private fun formatDogName(kind: String?): String {
        return kind.orEmpty()
            .replace(Regex("^\\[[^]]*]\\s*"), "")
            .trim()
    }

    private fun formatSex(value: String?): String {
        return when (value) {
            "M" -> "수컷"
            "F" -> "암컷"
            else -> "미상"
        }
    }

    private fun formatNeuter(value: String?): String {
        return when (value) {
            "Y" -> "예"
            "N" -> "아니오"
            else -> "미상"
        }
    }

    private fun valueOrPlaceholder(value: String?): String {
        val text = value.orEmpty().trim()
        return text.ifBlank { "정보 없음" }
    }

    private fun syncLikeState() {
        detailData.isLiked = PrefManager.getLikeItem(requireContext()).any { it.popfile == detailData.popfile }
    }

    private fun updateLikeIcon() {
        binding.btnLike.setImageResource(
            if (detailData.isLiked) R.drawable.icon_heart_filled else R.drawable.icon_heart_empty
        )
    }

    private fun toggleLike() {
        detailData.isLiked = !detailData.isLiked
        if (detailData.isLiked) {
            PrefManager.addItem(requireContext(), detailData)
        } else {
            detailData.popfile?.let { PrefManager.deleteItem(requireContext(), it) }
        }
        updateLikeIcon()
    }

    private fun showCallDialog() {
        val phone = detailData.careTel.orEmpty().trim()
        if (phone.isBlank()) {
            Toast.makeText(requireContext(), "보호소 연락처 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("보호소에 연락할까요?")
            .setMessage("${valueOrPlaceholder(detailData.careNm)}\n$phone")
            .setNegativeButton("아니오", null)
            .setPositiveButton("예") { _, _ ->
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${Uri.encode(phone)}")
                }
                startActivity(dialIntent)
            }
            .show()
    }

    fun receiveData(data: SearchDogData) {
        detailData = data
    }
}
