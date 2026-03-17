package com.android.dang.search

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.dang.R
import com.android.dang.databinding.FragmentSearchBinding
import com.android.dang.home.retrofit.HomeData
import com.android.dang.retrofit.Constants
import com.android.dang.search.adapter.SearchAdapter
import com.android.dang.search.adapter.SearchAdapter.Companion.typeOne
import com.android.dang.search.retrofit.SearchRetrofitClient
import com.android.dang.home.retrofit.RetrofitClient.apiService
import com.android.dang.home.retrofit.Util
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.search.searchViewModel.RecentViewModel
import com.android.dang.search.searchViewModel.SearchViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding: FragmentSearchBinding
        get() = _binding!!
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var recentViewModel: RecentViewModel

    private var searchItem = mutableListOf<SearchDogData>()
    private var hashMap = HashMap<String, String>()
    private var autoWordList = mutableListOf<String>()
    private lateinit var searchAdapter: SearchAdapter
    private val year = LocalDate.now().year
    private var dogKind = ""
    private lateinit var passData: DogData

    companion object {
        private const val DEFAULT_AGE_LABEL = "\uB098\uC774"
        private const val DEFAULT_GENDER_LABEL = "\uC131\uBCC4"
        private const val DEFAULT_SIZE_LABEL = "\uD06C\uAE30"
        private const val RECENT_SEARCH_LABEL = "\uCD5C\uADFC \uAC80\uC0C9\uC5B4"
        private const val SEARCH_HINT = "\uAC15\uC544\uC9C0 \uD488\uC885\uBA85\uC744 \uC785\uB825\uD574 \uC8FC\uC138\uC694"
        private const val RECENT_CLEAR_ALL_LABEL = "\uC804\uCCB4 \uC0AD\uC81C"
        private const val SEARCH_GUIDE_MESSAGE = "\uAC80\uC0C9\uD560 \uD488\uC885\uBA85\uC744 \uC785\uB825\uD558\uAC70\uB098 \uBAA9\uB85D\uC5D0\uC11C \uC120\uD0DD\uD574 \uC8FC\uC138\uC694."
        private const val MALE_LABEL = "\uC218\uCEF7"
        private const val FEMALE_LABEL = "\uC554\uCEF7"
        private const val NEUTERED_LABEL = "\uC911\uC131\uD654"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)


        initView()
        viewModel()
        setupSearchUi()

        context?.let { recentViewModel.setRecentList(recentViewModel.getListFromPreferences(it)) }

        binding.searchEdit.setOnClickListener {
            typeOne = 1
            showRecentSection()
        }

        binding.recentClearAll.setOnClickListener {
            recentViewModel.clearAll()
        }

        binding.searchBtn.setOnClickListener {
            if (performSearch()) {
                hideKeyboard()
            }
        }

        binding.searchAge.setOnClickListener {
            ageDialog()
        }

        binding.searchGender.setOnClickListener {
            genderDialog()
        }

        binding.searchSize.setOnClickListener {
            sizeDialog()
        }
        searchAdapter.itemClick = object : SearchAdapter.ItemClick {
            override fun onSearchClick(item: SearchDogData) {
                passData.pass(item)
            }

            override fun onImageViewClick(position: Int) {
                recentViewModel.recentRemove(position)
            }

            override fun onTextViewClick(position: Int) {
                val edit = recentViewModel.editText(position)
                binding.searchEdit.setText(edit)
                binding.searchEdit.setSelection(edit.length)
            }
        }

        val autoCompleteTextView = binding.searchEdit
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            autoWordList
        )
        autoCompleteTextView.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSearchUi() {
        binding.recent.text = RECENT_SEARCH_LABEL
        binding.recentClearAll.text = RECENT_CLEAR_ALL_LABEL
        binding.searchEdit.hint = SEARCH_HINT
        binding.searchEdit.inputType = InputType.TYPE_CLASS_TEXT
        binding.searchEdit.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.searchEdit.setSingleLine()
        resetFilterLabels()
        binding.searchEdit.setOnEditorActionListener { _, actionId, event ->
            val isEnterKey = event?.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                isEnterKey
            ) {
                if (performSearch()) {
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }
    }

    private fun showRecentSection() {
        binding.recentHeader.visibility = View.VISIBLE
        binding.searchTag.visibility = View.GONE
        searchAdapter.recentData(recentViewModel.recentList.value.orEmpty())
        updateRecentControls(recentViewModel.recentList.value.orEmpty())
    }

    private fun showFilterSection() {
        binding.recentHeader.visibility = View.GONE
        binding.recentClearAll.visibility = View.GONE
        binding.searchTag.visibility = View.VISIBLE
    }

    private fun updateRecentControls(list: List<String>) {
        val isRecentMode = typeOne == 1
        binding.recentHeader.visibility = if (isRecentMode) View.VISIBLE else View.GONE
        binding.recentClearAll.visibility = if (isRecentMode && list.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun performSearch(): Boolean {
        dogKind = binding.searchEdit.text.toString().trim()
        val selectedKindNumber = hashMap[dogKind]
        if (dogKind.isEmpty() || selectedKindNumber.isNullOrEmpty()) {
            toast(SEARCH_GUIDE_MESSAGE)
            return false
        }

        typeOne = 0
        showFilterSection()
        resetFilterLabels()

        searchViewModel.clearSearches()
        searchItem.clear()
        searchData(selectedKindNumber)
        return true
    }

    private fun resetFilterLabels() {
        binding.textAge.text = DEFAULT_AGE_LABEL
        binding.textGender.text = DEFAULT_GENDER_LABEL
        binding.textSize.text = DEFAULT_SIZE_LABEL
    }

    private fun initView() {
        searchAdapter = SearchAdapter()
        binding.rcvSearchList.apply {
            adapter = searchAdapter
        }

        kindData()
    }

    private fun viewModel() {
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        searchViewModel.searchesList.observe(viewLifecycleOwner, Observer { list ->
            searchAdapter.searchesData(list.orEmpty())
        })

        recentViewModel = ViewModelProvider(this)[RecentViewModel::class.java]

        recentViewModel.recentList.observe(viewLifecycleOwner, Observer { list ->
            val recentKeywords = list.orEmpty()
            searchAdapter.recentData(recentKeywords)
            context?.let { recentViewModel.saveListToPreferences(it) }
            updateRecentControls(recentKeywords)
        })
    }

    private fun recentAdd(text: String) {
        recentViewModel.recentAdd(text)
    }

    @SuppressLint("ResourceType")
    private fun ageDialog() {
        val builder = AlertDialog.Builder(requireContext())

        val v1 = layoutInflater.inflate(R.layout.dialog_search_age, null)
        builder.setView(v1)

        val dialog = builder.create()
        val applyBtn = v1.findViewById<TextView>(R.id.apply_btn)
        val resetBtn = v1.findViewById<TextView>(R.id.reset_btn)
        val oneYear = v1.findViewById<TextView>(R.id.one_year)
        val threeYear = v1.findViewById<TextView>(R.id.three_year)
        val fiveYear = v1.findViewById<TextView>(R.id.five_year)
        val sixYear = v1.findViewById<TextView>(R.id.six_year)
        val minAge = v1.findViewById<EditText>(R.id.set_min_age)
        val maxAge = v1.findViewById<EditText>(R.id.set_max_age)

        oneYear.setOnClickListener {
            minAge.setText("0")
            maxAge.setText("1")
        }

        threeYear.setOnClickListener {
            minAge.setText("1")
            maxAge.setText("3")
        }

        fiveYear.setOnClickListener {
            minAge.setText("3")
            maxAge.setText("5")
        }

        sixYear.setOnClickListener {
            minAge.setText("6")
            maxAge.setText("50")
        }

        applyBtn.setOnClickListener {
            val min = minAge.text.toString().toInt()
            val max = maxAge.text.toString().toInt()

            val minYear = year - min
            val maxYear = year - max
            searchViewModel.ageFilter(maxYear, minYear)
            binding.textAge.text = "$min ~ $max \uC0B4"
            dialog.dismiss()
        }

        resetBtn.setOnClickListener {
            searchViewModel.resetAgeFilter()
            binding.textAge.text = DEFAULT_AGE_LABEL
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun genderDialog() {
        val builder = AlertDialog.Builder(requireContext())

        val v2 = layoutInflater.inflate(R.layout.dialog_search_gender, null)
        builder.setView(v2)

        val dialog = builder.create()
        val applyBtn = v2.findViewById<TextView>(R.id.apply_btn)
        val resetBtn = v2.findViewById<TextView>(R.id.reset_btn)
        var selectedGender: String? = null
        var selectedNeuter: String? = null
        var genderView = DEFAULT_GENDER_LABEL

        val male = v2.findViewById<ImageView>(R.id.set_male)
        val neutrality = v2.findViewById<ImageView>(R.id.set_neutrality)
        val female = v2.findViewById<ImageView>(R.id.set_female)

        male.setOnClickListener {
            selectedGender = "M"
            selectedNeuter = null
            genderView = MALE_LABEL
            male.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_male, null))
            neutrality.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_neutrality_gray, null))
            female.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_female_gray, null))
        }
        neutrality.setOnClickListener {
            selectedGender = null
            selectedNeuter = "Y"
            genderView = NEUTERED_LABEL
            male.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_male_gray, null))
            neutrality.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_neutrality, null))
            female.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_female_gray, null))
        }
        female.setOnClickListener {
            selectedGender = "F"
            selectedNeuter = null
            genderView = FEMALE_LABEL
            male.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_male_gray, null))
            neutrality.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_neutrality_gray, null))
            female.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.icon_female, null))
        }

        applyBtn.setOnClickListener {
            selectedGender?.let { searchViewModel.genderFilter(it) } ?: searchViewModel.resetGenderFilter()
            selectedNeuter?.let { searchViewModel.neutrality(it) } ?: searchViewModel.resetNeuterFilter()
            binding.textGender.text = genderView
            dialog.dismiss()
        }

        resetBtn.setOnClickListener {
            searchViewModel.resetGenderFilter()
            searchViewModel.resetNeuterFilter()
            binding.textGender.text = DEFAULT_GENDER_LABEL
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun sizeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val v3 = layoutInflater.inflate(R.layout.dialog_search_size, null)
        builder.setView(v3)

        val dialog = builder.create()
        val applyBtn = v3.findViewById<TextView>(R.id.apply_btn)
        val resetBtn = v3.findViewById<TextView>(R.id.reset_btn)
        val minSize = v3.findViewById<EditText>(R.id.set_min_weight)
        val maxSize = v3.findViewById<EditText>(R.id.set_max_weight)
        val smallSize = v3.findViewById<TextView>(R.id.size_small)
        val mediumSize = v3.findViewById<TextView>(R.id.size_medium)
        val largeSize = v3.findViewById<TextView>(R.id.size_large)

        smallSize.setOnClickListener {
            minSize.setText("0")
            maxSize.setText("6")
        }
        mediumSize.setOnClickListener {
            minSize.setText("7")
            maxSize.setText("14")
        }
        largeSize.setOnClickListener {
            minSize.setText("15")
            maxSize.setText("30")
        }
        applyBtn.setOnClickListener {
            val min = minSize.text.toString().toInt()
            val max = maxSize.text.toString().toInt()

            searchViewModel.sizeFilter(min, max)
            binding.textSize.text = "$min ~ $max kg"
            dialog.dismiss()
        }

        resetBtn.setOnClickListener {
            searchViewModel.resetSizeFilter()
            binding.textSize.text = DEFAULT_SIZE_LABEL
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun searchData(kind: String) {
        Log.d(Constants.TestTAG, "searchData requested: kind=$kind, keyword=$dogKind")
        SearchRetrofitClient.apiService.abandonedDogSearch(
            numOfRows = 30,
            kind = kind
        ).enqueue(object : Callback<HomeData?> {
            override fun onResponse(call: Call<HomeData?>, response: Response<HomeData?>) {
                Log.d(Constants.TestTAG, "searchData response: ${response.code()}")
                if (response.isSuccessful) {
                    val itemList = response.body()?.response?.body?.items?.item.orEmpty()
                    searchItem.clear()
                    itemList.forEach { item ->
                        searchItem.add(
                            SearchDogData(
                                item.popfile1,
                                item.kindFullNm ?: item.kindNm ?: item.kindCd,
                                item.age,
                                item.careAddr,
                                item.processState,
                                item.sexCd,
                                item.neuterYn,
                                item.weight,
                                item.specialMark,
                                item.noticeNo,
                                item.happenPlace,
                                item.colorCd,
                                item.careNm,
                                item.careTel,
                                false
                            )
                        )
                    }
                    Log.d(Constants.TestTAG, "searchData items: ${searchItem.size}")
                } else {
                    Log.e(Constants.TestTAG, "searchData error: ${response.errorBody()?.string()}")
                }
                searchViewModel.searches(searchItem)
                recentAdd(dogKind)
            }

            override fun onFailure(call: Call<HomeData?>, t: Throwable) {
                Log.e(Constants.TestTAG, "searchData failure: ${t.message}", t)
            }
        })
    }

    private fun kindData() {
        Log.d(Constants.TestTAG, "kindData requested")
        apiService.homeDang(
            serviceKey = Util.KEY,
            numOfRows = 1000,
            pageNo = 1,
            type = "json",
            upkind = 417000
        ).enqueue(object : Callback<HomeData?> {
            override fun onResponse(call: Call<HomeData?>, response: Response<HomeData?>) {
                Log.d(Constants.TestTAG, "kindData response: ${response.code()}")
                if (response.isSuccessful) {
                    hashMap.clear()
                    autoWordList.clear()
                    response.body()?.response?.body?.items?.item.orEmpty().forEach { item ->
                        val kindCode = item.kindCd ?: return@forEach
                        val kindName = item.kindNm ?: return@forEach
                        if (!hashMap.containsKey(kindName)) {
                            hashMap[kindName] = kindCode
                            autoWordList.add(kindName)
                        }
                        item.kindFullNm?.let { fullName ->
                            hashMap[fullName] = kindCode
                        }
                    }
                    Log.d(Constants.TestTAG, "kindData items: ${autoWordList.size}")
                } else {
                    Log.e(Constants.TestTAG, "kindData error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<HomeData?>, t: Throwable) {
                Log.e(Constants.TestTAG, "kindData failure: ${t.message}", t)
            }
        })
    }

    private fun hideKeyboard() {
        binding.searchEdit.clearFocus()
        requireContext().getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(binding.searchEdit.windowToken, 0)
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    interface DogData {
        fun pass(list: SearchDogData)
    }

    fun dogData(data: DogData) {
        passData = data
    }
}
