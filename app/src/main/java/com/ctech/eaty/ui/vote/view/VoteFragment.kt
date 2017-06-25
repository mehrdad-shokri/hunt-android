package com.ctech.eaty.ui.vote.view


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import com.ctech.eaty.R
import com.ctech.eaty.base.BaseFragment
import com.ctech.eaty.base.redux.Store
import com.ctech.eaty.di.Injectable
import com.ctech.eaty.entity.Vote
import com.ctech.eaty.ui.vote.action.VoteAction
import com.ctech.eaty.ui.vote.state.VoteState
import com.ctech.eaty.ui.vote.viewmodel.VoteViewModel
import com.ctech.eaty.util.GlideImageLoader
import com.ctech.eaty.widget.recyclerview.InfiniteScrollListener
import kotlinx.android.synthetic.main.fragment_votes.*
import vn.tiki.noadapter2.DiffCallback
import vn.tiki.noadapter2.OnlyAdapter
import javax.inject.Inject


class VoteFragment : BaseFragment<VoteState>(), Injectable {

    companion object {
        val POST_ID = "postId"

        fun newInstance(id: Int): Fragment {

            val args = Bundle()

            val fragment = VoteFragment()
            args.putInt(POST_ID, id)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var store: Store<VoteState>

    @Inject
    lateinit var viewModel: VoteViewModel

    @Inject
    lateinit var imageLoader: GlideImageLoader

    private val postId by lazy {
        arguments.getInt(POST_ID)
    }

    private val diffCallback = object : DiffCallback {

        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem is Vote && newItem is Vote) {
                return oldItem.id == newItem.id
            }
            return false
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

    }

    private val adapter: OnlyAdapter by lazy {
        OnlyAdapter.builder()
                .diffCallback(diffCallback)
                .viewHolderFactory { viewGroup, _ ->
                    VoteViewHolder.create(viewGroup, imageLoader)
                }
                .build()
    }

    private lateinit var contractor: FragmentContractor

    override fun store(): Store<VoteState> {
        return store
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is VoteActivity) {
            contractor = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_votes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        setupRecyclerView()
        setupViewModel()
    }

    override fun onStart() {
        super.onStart()
        store.dispatch(VoteAction.Load(postId))
        setupViewModel()
    }

    private fun renderContent(list: List<Vote>) {
        vLottie.cancelAnimation()
        vLottie.visibility = View.GONE
        vError.visibility = View.GONE
        adapter.setItems(list)
    }

    private fun setupErrorView() {
        vError.setOnRetryListener {
            store.dispatch(VoteAction.Load(postId))
        }
    }

    private fun renderLoadMoreError() {

    }


    private fun renderLoadError(error: Throwable) {
        vLottie.cancelAnimation()
        vLottie.visibility = View.GONE
        vError.visibility = View.VISIBLE
    }

    private fun renderLoadingMore() {

    }


    private fun renderLoading() {
        vLottie.playAnimation()
        vError.visibility = View.GONE
        vLottie.visibility = View.VISIBLE
    }

    private fun setupViewModel() {
        disposeOnStop(viewModel.loading().subscribe { renderLoading() })
        disposeOnStop(viewModel.loadingMore().subscribe { renderLoadingMore() })
        disposeOnStop(viewModel.loadError().subscribe { renderLoadError(it) })
        disposeOnStop(viewModel.loadMoreError().subscribe { renderLoadMoreError() })
        disposeOnStop(viewModel.content().subscribe { renderContent(it) })
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        rvVotes.adapter = adapter
        rvVotes.layoutManager = layoutManager
        rvVotes.addOnScrollListener(InfiniteScrollListener(layoutManager, 3, Runnable {
            store.dispatch(VoteAction.LoadMore(postId))
        }))
        rvVotes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val raiseTitleBar = dy > 0 || rvVotes.computeVerticalScrollOffset() != 0
                contractor.getTitleBar().isActivated = raiseTitleBar // animated via a StateListAnimator
            }
        })
    }

}