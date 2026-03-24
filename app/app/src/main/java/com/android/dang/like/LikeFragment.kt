package com.android.dang.like

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.R
import com.android.dang.databinding.FragmentLikeBinding
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.util.PrefManager
import com.google.android.material.snackbar.Snackbar

class LikeFragment : Fragment() {
    private var _binding: FragmentLikeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var adapter: LikeAdapter
    private lateinit var recyclerView: RecyclerView
    private var currentSnackbar: Snackbar? = null
    private var navBarView: View? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLikeBinding.inflate(inflater, container, false)

        val likeItems = PrefManager.getLikeItem(mContext)
        recyclerView = binding.likeRc
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = LikeAdapter(mContext)
        adapter.items = likeItems
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : LikeAdapter.OnItemClickListener {
            override fun onItemClick(item: SearchDogData, position: Int) = Unit
        })

        navBarView = requireActivity().findViewById(R.id.nav_bar)
        ItemTouchHelper(
            Swipe(
                adapter = adapter,
                hostView = binding.root,
                anchorView = navBarView,
                onSnackbarChanged = { snackbar ->
                    currentSnackbar = snackbar
                }
            )
        ).attachToRecyclerView(binding.likeRc)

        setupSnackbarDismissHandlers()
        return binding.root
    }

    private fun setupSnackbarDismissHandlers() {
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dismissDeleteSnackbar()
            }
            false
        }

        binding.likeRc.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_DOWN) {
                    dismissDeleteSnackbar()
                }
                return false
            }
        })

        binding.notify.setOnClickListener {
            dismissDeleteSnackbar()
        }

        navBarView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dismissDeleteSnackbar()
            }
            false
        }
    }

    private fun dismissDeleteSnackbar() {
        currentSnackbar?.dismiss()
    }

    override fun onDestroyView() {
        currentSnackbar?.dismiss()
        currentSnackbar = null
        navBarView?.setOnTouchListener(null)
        navBarView = null
        super.onDestroyView()
        _binding = null
    }
}
