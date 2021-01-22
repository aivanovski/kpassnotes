package com.ivanovsky.passnotes.presentation.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ivanovsky.passnotes.R
import com.ivanovsky.passnotes.data.entity.Group
import com.ivanovsky.passnotes.data.entity.Note
import com.ivanovsky.passnotes.data.entity.Template
import com.ivanovsky.passnotes.presentation.Screen
import com.ivanovsky.passnotes.presentation.core.BaseFragment
import com.ivanovsky.passnotes.presentation.core.ScreenDisplayingMode
import com.ivanovsky.passnotes.presentation.core.ScreenState
import com.ivanovsky.passnotes.presentation.core.dialog.ConfirmationDialog
import com.ivanovsky.passnotes.presentation.core_mvvm.event.SingleLiveEvent
import com.ivanovsky.passnotes.presentation.group.GroupActivity
import com.ivanovsky.passnotes.presentation.groups.dialog.ChooseOptionDialog
import com.ivanovsky.passnotes.presentation.note.NoteActivity
import com.ivanovsky.passnotes.presentation.note_editor.NoteEditorActivity
import java.util.*

class GroupsFragment : BaseFragment(), GroupsContract.View {

    override var presenter: GroupsContract.Presenter? = null

    private lateinit var adapter: GroupsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val itemsData = MutableLiveData<List<GroupsAdapter.ListItem>>()
    private val showNoteScreenEvent = SingleLiveEvent<Note>()
    private val showNoteListScreenEvent = SingleLiveEvent<Group>()
    private val showNewGroupScreenEvent = SingleLiveEvent<UUID>()
    private val showNewNoteScreenEvent = SingleLiveEvent<Pair<UUID, Template?>>()
    private val showNewEntryDialogEvent = SingleLiveEvent<List<Template>>()
    private val showGroupActionsDialogEvent = SingleLiveEvent<Group>()
    private val showNoteActionsDialogEvent = SingleLiveEvent<Note>()
    private val showRemoveConfirmationDialogEvent = SingleLiveEvent<Pair<Group?, Note?>>()
    private val showEditNoteScreenEvent = SingleLiveEvent<Note>()
    private val showEditGroupScreenEvent = SingleLiveEvent<Group>()

    override fun onStart() {
        super.onStart()
        presenter?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.destroy()
    }

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.groups_fragment, container, false)

        val context = view.context

        recyclerView = view.findViewById(R.id.recycler_view)
        fab = view.findViewById(R.id.fab)

        val layoutManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = layoutManager

        adapter = GroupsAdapter(context)
        adapter.onListItemClickListener = { position ->
            presenter?.onListItemClicked(position)
        }
        adapter.onListItemLongClickListener = { position ->
            presenter?.onListItemLongClicked(position)
        }

        recyclerView.adapter = adapter

        fab.setOnClickListener { presenter?.onAddButtonClicked() }

        presenter?.globalSnackbarMessageAction?.observe(viewLifecycleOwner, Screen.GROUPS,
            Observer { message -> showSnackbar(message) })

        itemsData.observe(viewLifecycleOwner,
            Observer { items -> setItemsInternal(items) })
        showNoteListScreenEvent.observe(viewLifecycleOwner,
            Observer { group ->  showNoteListScreenInternal(group) })
        showNoteScreenEvent.observe(viewLifecycleOwner,
            Observer { note -> showNoteScreenInternal(note)})
        showNewGroupScreenEvent.observe(viewLifecycleOwner,
            Observer { parentGroupUid -> showNewGroupScreenInternal(parentGroupUid) })
        showNewNoteScreenEvent.observe(viewLifecycleOwner,
            Observer { (groupUid, template) -> showNewNoteScreenInternal(groupUid, template) })
        showNewEntryDialogEvent.observe(viewLifecycleOwner,
            Observer { templates -> showNewEntryDialogInternal(templates) })
        showGroupActionsDialogEvent.observe(viewLifecycleOwner,
            Observer { group -> showGroupActionsDialogInternal(group) })
        showNoteActionsDialogEvent.observe(viewLifecycleOwner,
            Observer { note -> showNoteActionsDialogInternal(note) })
        showRemoveConfirmationDialogEvent.observe(viewLifecycleOwner,
            Observer { (group, note) -> showRemoveConfirmationDialogInternal(group, note)})
        showEditNoteScreenEvent.observe(viewLifecycleOwner,
            Observer { note -> showEditNoteScreenInternal(note) })
        showEditGroupScreenEvent.observe(viewLifecycleOwner,
            Observer { group -> showEditGroupScreenInternal(group) })

        return view
    }

    override fun getContentContainerId(): Int {
        return R.id.recycler_view
    }

    override fun onScreenStateChanged(screenState: ScreenState) {
        when (screenState.displayingMode) {
            ScreenDisplayingMode.EMPTY,
            ScreenDisplayingMode.DISPLAYING_DATA_WITH_ERROR_PANEL,
            ScreenDisplayingMode.DISPLAYING_DATA -> fab.show()

            ScreenDisplayingMode.LOADING, ScreenDisplayingMode.ERROR -> fab.hide()
        }
    }

    override fun setItems(items: List<GroupsAdapter.ListItem>) {
        itemsData.value = items
    }

    private fun setItemsInternal(items: List<GroupsAdapter.ListItem>) {
        adapter.setItems(items)
        adapter.notifyDataSetChanged()
    }

    override fun showNoteListScreen(group: Group) {
        showNoteListScreenEvent.call(group)
    }

    private fun showNoteListScreenInternal(group: Group) {
        val context = this.context ?: return

        startActivity(GroupsActivity.intentFroGroup(context, group))
    }

    override fun showNoteScreen(note: Note) {
        showNoteScreenEvent.call(note)
    }

    private fun showNoteScreenInternal(note: Note) {
        val context = this.context ?: return

        startActivity(NoteActivity.createStartIntent(context, note))
    }

    override fun showNewGroupScreen(parentGroupUid: UUID) {
        showNewGroupScreenEvent.call(parentGroupUid)
    }

    private fun showNewGroupScreenInternal(parentGroupUid: UUID) {
        val context = this.context ?: return

        startActivity(GroupActivity.createChildGroup(context, parentGroupUid))
    }

    override fun showNewNoteScreen(parentGroupUid: UUID, template: Template?) {
        showNewNoteScreenEvent.call(Pair(parentGroupUid, template))
    }

    private fun showNewNoteScreenInternal(parentGroupUid: UUID, template: Template?) {
        val context = this.context ?: return

        startActivity(NoteEditorActivity.intentForNewNote(context, parentGroupUid, template))
    }

    override fun showNewEntryDialog(templates: List<Template>) {
        showNewEntryDialogEvent.call(templates)
    }

    private fun showNewEntryDialogInternal(templates: List<Template>) {
        val templateEntries = templates.map { template -> template.title }

        val entries = listOf(getString(R.string.new_item_entry_standard_note)) +
                templateEntries +
                listOf(getString(R.string.new_item_entry_new_group))

        val dialog = ChooseOptionDialog.newInstance(getString(R.string.create), entries)

        dialog.onItemClickListener = { itemIdx ->
            when (itemIdx) {
                0 -> presenter?.onCreateNewNoteClicked()
                entries.size - 1 -> presenter?.onCreateNewGroupClicked()
                else -> presenter?.onCreateNewNoteFromTemplateClicked(templates[itemIdx - 1])
            }
        }

        dialog.show(childFragmentManager, ChooseOptionDialog.TAG)
    }

    override fun showGroupActionsDialog(group: Group) {
        showGroupActionsDialogEvent.call(group)
    }

    private fun showGroupActionsDialogInternal(group: Group) {
        val entries = resources.getStringArray(R.array.item_actions_entries)

        val dialog = ChooseOptionDialog.newInstance(null, entries.toList())
        dialog.onItemClickListener = { itemIdx ->
            when (itemIdx) {
                0 -> presenter?.onEditGroupClicked(group)
                1 -> presenter?.onRemoveGroupClicked(group)
            }
        }
        dialog.show(childFragmentManager, ChooseOptionDialog.TAG)
    }

    override fun showNoteActionsDialog(note: Note) {
        showNoteActionsDialogEvent.call(note)
    }

    private fun showNoteActionsDialogInternal(note: Note) {
        val entries = resources.getStringArray(R.array.item_actions_entries)

        val dialog = ChooseOptionDialog.newInstance(null, entries.toList())
        dialog.onItemClickListener = { itemIdx ->
            when (itemIdx) {
                0 -> presenter?.onEditNoteClicked(note)
                1 -> presenter?.onRemoveNoteClicked(note)
            }
        }
        dialog.show(childFragmentManager, ChooseOptionDialog.TAG)
    }

    override fun showRemoveConfirmationDialog(group: Group?, note: Note?) {
        showRemoveConfirmationDialogEvent.call(Pair(group, note))
    }

    private fun showRemoveConfirmationDialogInternal(group: Group?, note: Note?) {
        val message = if (note != null) {
            getString(R.string.note_remove_confirmation_message, note.title)
        } else {
            getString(R.string.group_remove_confirmation_message)
        }

        val dialog = ConfirmationDialog.newInstance(
            message,
            getString(R.string.yes),
            getString(R.string.no)
        )
        dialog.onConfirmationLister = {
            presenter?.onRemoveConfirmed(group, note)
        }
        dialog.show(childFragmentManager, ConfirmationDialog.TAG)
    }

    override fun showEditNoteScreen(note: Note) {
        showEditNoteScreenEvent.call(note)
    }

    private fun showEditNoteScreenInternal(note: Note) {
        val noteUid = note.uid ?: return

        val intent = NoteEditorActivity.intentForEditNote(requireContext(), noteUid, note.title)
        startActivity(intent)
    }

    override fun showEditGroupScreen(group: Group) {
        showEditGroupScreenEvent.call(group)
    }

    private fun showEditGroupScreenInternal(group: Group) {
    }
}