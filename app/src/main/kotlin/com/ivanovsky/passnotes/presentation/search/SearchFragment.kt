package com.ivanovsky.passnotes.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.github.terrakok.cicerone.Router
import com.ivanovsky.passnotes.R
import com.ivanovsky.passnotes.data.entity.Note
import com.ivanovsky.passnotes.databinding.SearchFragmentBinding
import com.ivanovsky.passnotes.extensions.setItemVisibility
import com.ivanovsky.passnotes.injection.GlobalInjector
import com.ivanovsky.passnotes.injection.GlobalInjector.inject
import com.ivanovsky.passnotes.presentation.ApplicationLaunchMode
import com.ivanovsky.passnotes.presentation.Screens
import com.ivanovsky.passnotes.presentation.autofill.AutofillDialogFactory
import com.ivanovsky.passnotes.presentation.core.BaseFragment
import com.ivanovsky.passnotes.presentation.core.dialog.ConfirmationDialog
import com.ivanovsky.passnotes.presentation.core.extensions.finishActivity
import com.ivanovsky.passnotes.presentation.core.extensions.getMandatoryArgument
import com.ivanovsky.passnotes.presentation.core.extensions.hideKeyboard
import com.ivanovsky.passnotes.presentation.core.extensions.sendAutofillResult
import com.ivanovsky.passnotes.presentation.core.extensions.setupActionBar
import com.ivanovsky.passnotes.presentation.core.extensions.showKeyboard
import com.ivanovsky.passnotes.presentation.core.extensions.withArguments
import com.ivanovsky.passnotes.presentation.unlock.UnlockScreenArgs

class SearchFragment : BaseFragment() {

    private var menu: Menu? = null
    private lateinit var binding: SearchFragmentBinding
    private val viewModel: SearchViewModel by lazy {
        ViewModelProvider(
            this,
            SearchViewModel.Factory(
                args = getMandatoryArgument(ARGUMENTS)
            )
        )
            .get(SearchViewModel::class.java)
    }
    private val router: Router by inject()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        setupActionBar {
            title = getString(R.string.search)
            setHomeAsUpIndicator(null)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu

        inflater.inflate(R.menu.search, menu)

        viewModel.isMoreMenuVisible.value?.let {
            menu.setItemVisibility(R.id.menu_more, it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchFragmentBinding.inflate(inflater, container, false)
            .also {
                it.lifecycleOwner = viewLifecycleOwner
                it.viewModel = viewModel
            }
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                viewModel.onBackClicked()
                true
            }
            R.id.menu_lock -> {
                viewModel.onLockButtonClicked()
                true
            }
            R.id.menu_settings -> {
                viewModel.onSettingsButtonClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        navigationViewModel.setNavigationEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToData()
        subscribeToEvents()

        viewModel.onScreenCreated()
    }

    private fun subscribeToData() {
        viewModel.isMoreMenuVisible.observe(viewLifecycleOwner) {
            menu?.setItemVisibility(R.id.menu_more, it)
        }
    }

    private fun subscribeToEvents() {
        viewModel.isKeyboardVisibleEvent.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible) {
                binding.searchText.requestFocus()
                showKeyboard(binding.searchText)
            } else {
                hideKeyboard()
            }
        }
        viewModel.sendAutofillResponseEvent.observe(viewLifecycleOwner) { (note, structure) ->
            sendAutofillResult(note, structure)
            finishActivity()
        }
        viewModel.finishActivityEvent.observe(viewLifecycleOwner) {
            finishActivity()
        }
        viewModel.showAddAutofillDataDialog.observe(viewLifecycleOwner) {
            showAddAutofillDataDialog(it)
        }
        viewModel.lockScreenEvent.observe(viewLifecycleOwner) {
            router.backTo(
                Screens.UnlockScreen(
                    args = UnlockScreenArgs(ApplicationLaunchMode.NORMAL)
                )
            )
        }
    }

    private fun showAddAutofillDataDialog(note: Note) {
        val dialog = AutofillDialogFactory(requireContext()).createAddAutofillDataToNoteDialog(
            onConfirmed = { viewModel.onAddAutofillDataConfirmed(note) },
            onDenied = { viewModel.onAddAutofillDataDenied(note) }
        )
        dialog.show(childFragmentManager, ConfirmationDialog.TAG)
    }

    companion object {

        private const val ARGUMENTS = "arguments"

        fun newInstance(args: SearchScreenArgs): SearchFragment = SearchFragment()
            .withArguments {
                putParcelable(ARGUMENTS, args)
            }
    }
}