package com.android.dang.dictionary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.dang.R
import com.android.dang.databinding.FragmentDictionaryBinding
import com.android.dang.dictionary.data.BreedsData
import com.android.dang.dictionary.data.BreedsSpinnerData
import com.android.dang.dictionary.retrofit.NetWorkClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DictionaryFragment : Fragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding: FragmentDictionaryBinding
        get() = _binding!!
    private val breedOptions = arrayListOf<BreedsSpinnerData>()
    private val dictionaryListAdapter by lazy {
        DictionaryListAdapter(::openBreedDetail)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dictionaryRecyclerView.adapter = dictionaryListAdapter
        binding.dictionaryRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val breedsDatas = NetWorkClient.dogNetWork.getBreeds(
                    NetWorkClient.API_AUTHKEY,
                    hashMapOf(
                        "limit" to 200,
                        "page" to 0
                    )
                )

                if (_binding == null || !isAdded) {
                    return@launch
                }

                dictionaryListAdapter.addItems(breedsDatas, true)
                bindSpinner(breedsDatas)
            } catch (e: HttpException) {
                Log.e("dictionaryFragment", "Dictionary API error: ${e.code()}", e)
            } catch (e: Exception) {
                Log.e("dictionaryFragment", "Dictionary load failed: ${e.message}", e)
            }
        }
    }

    private fun bindSpinner(breedsDatas: BreedsData) {
        breedOptions.clear()
        breedOptions.add(BreedsSpinnerData(0, "\uC804\uCCB4"))
        breedOptions.addAll(
            breedsDatas.map { breedItem ->
                BreedsSpinnerData(
                    breedItem.id,
                    BreedNameLocalizer.localize(requireContext(), breedItem.name)
                )
            }
        )

        binding.dictionarySpinner.adapter =
            BreedsSpinnerAdapter(requireContext(), breedOptions)
        binding.dictionarySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    selectedView: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        dictionaryListAdapter.addItems(breedsDatas, true)
                        return
                    }

                    val breedId = breedOptions[position].id
                    breedsDatas.find { it.id == breedId }?.let {
                        dictionaryListAdapter.addItems(listOf(it), true)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun openBreedDetail(item: BreedsData.BreedsDataItem) {
        if (!isAdded) {
            return
        }

        val displayName = BreedNameLocalizer.localize(requireContext(), item.name)
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_view,
                DictionaryDetailFragment.newInstance(
                    displayName = displayName,
                    englishName = item.name.orEmpty(),
                    imageUrl = DictionaryBreedUi.imageUrl(item),
                    info = DictionaryBreedUi.detailInfo(requireContext(), item),
                    description = DictionaryBreedUi.description(requireContext(), item),
                    history = DictionaryBreedUi.history(requireContext(), item)
                )
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
