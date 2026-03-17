package com.android.dang.dictionary

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.android.dang.R
import com.android.dang.databinding.FragmentDictionaryDetailBinding
import com.bumptech.glide.Glide

class DictionaryDetailFragment : Fragment(R.layout.fragment_dictionary_detail) {
    private var _binding: FragmentDictionaryDetailBinding? = null
    private val binding: FragmentDictionaryDetailBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDictionaryDetailBinding.bind(view)
        bindUi()
    }

    private fun bindUi() = with(binding) {
        val args = requireArguments()
        val displayName = args.getString(ARG_DISPLAY_NAME).orEmpty()
        val englishName = args.getString(ARG_ENGLISH_NAME).orEmpty()
        val imageUrl = args.getString(ARG_IMAGE_URL)
        val info = args.getString(ARG_INFO).orEmpty()
        val description = args.getString(ARG_DESCRIPTION).orEmpty()
        val history = args.getString(ARG_HISTORY).orEmpty()

        descriptionTitle.text = "\uACAC\uC885 \uC18C\uAC1C"
        historyTitle.text = "\uC5ED\uC0AC"
        breedName.text = displayName
        breedNameEnglish.text = englishName
        breedNameEnglish.isVisible = englishName.isNotBlank() && englishName != displayName

        Glide.with(this@DictionaryDetailFragment)
            .load(imageUrl)
            .placeholder(R.drawable.icon_dog1)
            .error(R.drawable.icon_dog1)
            .into(breedImage)

        breedSummary.text = info
        breedSummary.isVisible = info.isNotBlank()

        descriptionTitle.isVisible = description.isNotBlank()
        breedDescription.isVisible = description.isNotBlank()
        breedDescription.text = description

        historyTitle.isVisible = history.isNotBlank()
        breedHistory.isVisible = history.isNotBlank()
        breedHistory.text = history
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_DISPLAY_NAME = "display_name"
        private const val ARG_ENGLISH_NAME = "english_name"
        private const val ARG_IMAGE_URL = "image_url"
        private const val ARG_INFO = "info"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_HISTORY = "history"

        fun newInstance(
            displayName: String,
            englishName: String,
            imageUrl: String?,
            info: String,
            description: String,
            history: String
        ): DictionaryDetailFragment {
            return DictionaryDetailFragment().apply {
                arguments = bundleOf(
                    ARG_DISPLAY_NAME to displayName,
                    ARG_ENGLISH_NAME to englishName,
                    ARG_IMAGE_URL to imageUrl,
                    ARG_INFO to info,
                    ARG_DESCRIPTION to description,
                    ARG_HISTORY to history
                )
            }
        }
    }
}
