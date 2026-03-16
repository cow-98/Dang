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
import com.android.dang.databinding.FragmentDictionaryBinding
import com.android.dang.dictionary.data.BreedsSpinnerData
import com.android.dang.dictionary.retrofit.NetWorkClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DictionaryFragment : Fragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding: FragmentDictionaryBinding
        get() = _binding!!
    private var mBreedList = arrayListOf<BreedsSpinnerData>()
    private val dictionaryListAdapter by lazy {
        DictionaryListAdapter()
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

                mBreedList.clear()
                mBreedList.add(BreedsSpinnerData(0, "\uC804\uCCB4"))
                mBreedList.addAll(
                    breedsDatas.map { breedItem ->
                        BreedsSpinnerData(breedItem.id, breedItem.name)
                    }
                )

                binding.dictionarySpinner.adapter =
                    BreedsSpinnerAdapter(requireContext(), mBreedList)
                binding.dictionarySpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            selectedView: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (position == 0) {
                                (binding.dictionaryRecyclerView.adapter as DictionaryListAdapter)
                                    .addItems(breedsDatas, true)
                                return
                            }

                            val breedId = mBreedList[position].id
                            breedsDatas.find { it.id == breedId }?.let {
                                (binding.dictionaryRecyclerView.adapter as DictionaryListAdapter)
                                    .addItems(arrayListOf(it), true)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                    }
            } catch (e: HttpException) {
                Log.e("dictionaryFragment", "Dictionary API error: ${e.code()}", e)
            } catch (e: Exception) {
                Log.e("dictionaryFragment", "Dictionary load failed: ${e.message}", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
