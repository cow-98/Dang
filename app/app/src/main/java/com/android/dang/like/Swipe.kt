package com.android.dang.like

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class Swipe(
    private val adapter: LikeAdapter,
    private val hostView: View,
    private val anchorView: View? = null,
    private val onSnackbarChanged: (Snackbar?) -> Unit = {}
) : ItemTouchHelper.SimpleCallback(0, RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.layoutPosition
        val data = adapter.data(position)

        adapter.removeData(position)

        val snackbar = Snackbar.make(hostView, "삭제되었습니다.", Snackbar.LENGTH_SHORT)
            .setAction("되돌리기") {
                adapter.insertData(position, data)
            }

        anchorView?.let { snackbar.setAnchorView(it) }
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onShown(transientBottomBar: Snackbar?) {
                onSnackbarChanged(transientBottomBar)
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                onSnackbarChanged(null)
            }
        })
        snackbar.show()
    }
}
