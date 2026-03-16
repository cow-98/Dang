package com.android.dang.home

import android.content.Context
import android.os.Bundle
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
    private var resItems: ArrayList<SearchDogData> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        Log.d("homefragment", "onCreate")

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        Log.d("homefragment", "onAttach")
    }

    override fun onResume() {
        super.onResume()
        Log.d("homefragment", "onResume")
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d("homefragment", "onCreateView")

        binding.bannerMoreBtn.setOnClickListener {
            val shelterFragment = ShelterFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_view, shelterFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        recyclerView = binding.homeRc
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = HomeAdapter(mContext)
        recyclerView.adapter = adapter

        adapter.clearItem()
        resItems.clear()
        homeResult()
        return binding.root
    }

    private fun homeResult() {
        Log.d("homeFragment", "homeResult start")

        apiService.homeDang(
            serviceKey = Util.KEY,
            numOfRows = 50,
            pageNo = 1,
            type = "json",
            upkind = 417000
        ).enqueue(object : Callback<HomeData?> {

            override fun onResponse(call: Call<HomeData?>, response: Response<HomeData?>) {
                Log.d("homeFragment", "response.code = ${response.code()}")
                Log.d("homeFragment", "response.isSuccessful = ${response.isSuccessful}")
                Log.d("homeFragment", "raw body = ${response.body()}")

                if (response.isSuccessful) {
                    val homeData = response.body()
                    Log.d("homeFragment", "header = ${homeData?.response?.header}")
                    Log.d("homeFragment", "body = ${homeData?.response?.body}")

                    val itemList = homeData?.response?.body?.items?.item
                    Log.d("homeFragment", "itemList null? = ${itemList == null}")
                    Log.d("homeFragment", "itemList size = ${itemList?.size}")

                    val likeItems = PrefManager.getLikeItem(mContext)
                    Log.d("homeFragment", "likeItems.size = ${likeItems.size}")

                    resItems.clear()

                    itemList?.forEach { item ->
                        Log.d("homeFragment", "item = $item")

                        val popfile = item.popfile1
                        val kindCd = item.kindFullNm ?: item.kindNm ?: item.kindCd
                        val age = item.age
                        val careAddr = item.careAddr
                        val processState = item.processState
                        val sexCd = item.sexCd
                        val neuterYn = item.neuterYn
                        val weight = item.weight
                        val specialMark = item.specialMark
                        val noticeNo = item.noticeNo
                        val happenPlace = item.happenPlace
                        val colorCd = item.colorCd
                        val careNm = item.careNm
                        val careTel = item.careTel

                        val isLike = likeItems.find { it.popfile == item.popfile1 } != null

                        resItems.add(
                            SearchDogData(
                                popfile,
                                kindCd,
                                age,
                                careAddr,
                                processState,
                                sexCd,
                                neuterYn,
                                weight,
                                specialMark,
                                noticeNo,
                                happenPlace,
                                colorCd,
                                careNm,
                                careTel,
                                isLike
                            )
                        )
                    }

                    Log.d("homeFragment", "resItems.size = ${resItems.size}")
                    adapter.addItem(resItems)
                } else {
                    Log.e("homeFragment", "error code = ${response.code()}")
                    Log.e("homeFragment", "error body = ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<HomeData?>, t: Throwable) {
                Log.e("homeFragment", "API Error: ${t.message}", t)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}