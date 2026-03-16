package com.android.dang.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.R
import com.android.dang.databinding.FragmentHomeBinding
import com.android.dang.home.homeAdapter.HomeAdapter
import com.android.dang.home.retrofit.HomeData
import com.android.dang.home.retrofit.RetrofitClient.apiService
import com.android.dang.home.retrofit.Util
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.shelter.view.ShelterFragment
import com.android.dang.util.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private val resItems = ArrayList<SearchDogData>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeAdapter
    private var passData: DogData? = null
    private var isHomeLoaded = false
    private var currentHomeCount = INITIAL_HOME_COUNT
    private var homeCall: Call<HomeData?>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.bannerMoreBtn.setOnClickListener {
            val shelterFragment = ShelterFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_view, shelterFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.homeRefresh.setOnRefreshListener {
            requestHomeItems(FULL_HOME_COUNT, true)
        }

        recyclerView = binding.homeRc
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        adapter = HomeAdapter(mContext)
        recyclerView.adapter = adapter
        adapter.itemClick = object : HomeAdapter.ItemClick {
            override fun onClick(position: Int) {
                val selectedItem = resItems.getOrNull(position) ?: return
                passData?.pass(selectedItem)
            }
        }

        if (isHomeLoaded && resItems.isNotEmpty()) {
            syncLikedState()
            adapter.addItem(resItems)
        } else {
            adapter.clearItem()
            resItems.clear()
            requestHomeItems(currentHomeCount, false)
        }
        return binding.root
    }

    private fun requestHomeItems(numOfRows: Int, fromUserRefresh: Boolean) {
        if (fromUserRefresh) {
            _binding?.homeRefresh?.isRefreshing = true
        }

        homeCall?.cancel()
        homeCall = apiService.homeDang(
            serviceKey = Util.KEY,
            numOfRows = numOfRows,
            pageNo = 1,
            type = "json",
            upkind = 417000
        )

        homeCall?.enqueue(object : Callback<HomeData?> {
            override fun onResponse(call: Call<HomeData?>, response: Response<HomeData?>) {
                _binding?.homeRefresh?.isRefreshing = false
                if (!response.isSuccessful) {
                    Log.e("homeFragment", "Home API error: ${response.code()}")
                    return
                }

                val itemList = response.body()?.response?.body?.items?.item.orEmpty()
                val likePopfiles = PrefManager.getLikeItem(mContext)
                    .mapNotNull { it.popfile }
                    .toHashSet()

                resItems.clear()
                resItems.addAll(
                    itemList.map { item ->
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
                            likePopfiles.contains(item.popfile1)
                        )
                    }
                )
                currentHomeCount = numOfRows
                isHomeLoaded = true

                if (_binding != null && ::adapter.isInitialized) {
                    adapter.addItem(resItems)
                }
            }

            override fun onFailure(call: Call<HomeData?>, t: Throwable) {
                _binding?.homeRefresh?.isRefreshing = false
                if (call.isCanceled) {
                    return
                }
                Log.e("homeFragment", "Home API failure: ${t.message}", t)
            }
        })
    }

    private fun syncLikedState() {
        val likePopfiles = PrefManager.getLikeItem(mContext)
            .mapNotNull { it.popfile }
            .toHashSet()
        resItems.forEach { item ->
            item.isLiked = likePopfiles.contains(item.popfile)
        }
    }

    interface DogData {
        fun pass(list: SearchDogData)
    }

    fun dogData(data: DogData) {
        passData = data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val INITIAL_HOME_COUNT = 20
        private const val FULL_HOME_COUNT = 50
    }
}
