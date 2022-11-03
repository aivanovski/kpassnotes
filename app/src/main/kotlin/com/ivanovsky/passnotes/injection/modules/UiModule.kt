package com.ivanovsky.passnotes.injection.modules

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import com.ivanovsky.passnotes.domain.DatabaseLockInteractor
import com.ivanovsky.passnotes.domain.interactor.autofill.AutofillInteractor
import com.ivanovsky.passnotes.domain.interactor.debugmenu.DebugMenuInteractor
import com.ivanovsky.passnotes.domain.interactor.filepicker.FilePickerInteractor
import com.ivanovsky.passnotes.domain.interactor.group_editor.GroupEditorInteractor
import com.ivanovsky.passnotes.domain.interactor.groups.GroupsInteractor
import com.ivanovsky.passnotes.domain.interactor.main.MainInteractor
import com.ivanovsky.passnotes.domain.interactor.newdb.NewDatabaseInteractor
import com.ivanovsky.passnotes.domain.interactor.note.NoteInteractor
import com.ivanovsky.passnotes.domain.interactor.note_editor.NoteEditorInteractor
import com.ivanovsky.passnotes.domain.interactor.password_generator.PasswordGeneratorInteractor
import com.ivanovsky.passnotes.domain.interactor.search.SearchInteractor
import com.ivanovsky.passnotes.domain.interactor.server_login.ServerLoginInteractor
import com.ivanovsky.passnotes.domain.interactor.service.LockServiceInteractor
import com.ivanovsky.passnotes.domain.interactor.settings.app.AppSettingsInteractor
import com.ivanovsky.passnotes.domain.interactor.settings.database.DatabaseSettingsInteractor
import com.ivanovsky.passnotes.domain.interactor.settings.main.MainSettingsInteractor
import com.ivanovsky.passnotes.domain.interactor.storagelist.StorageListInteractor
import com.ivanovsky.passnotes.domain.interactor.unlock.UnlockInteractor
import com.ivanovsky.passnotes.presentation.about.AboutViewModel
import com.ivanovsky.passnotes.presentation.autofill.AutofillViewFactory
import com.ivanovsky.passnotes.presentation.core.factory.DatabaseStatusCellModelFactory
import com.ivanovsky.passnotes.presentation.debugmenu.DebugMenuViewModel
import com.ivanovsky.passnotes.presentation.dialogs.sort_and_view.SortAndViewDialogArgs
import com.ivanovsky.passnotes.presentation.dialogs.sort_and_view.SortAndViewDialogViewModel
import com.ivanovsky.passnotes.presentation.filepicker.FilePickerArgs
import com.ivanovsky.passnotes.presentation.filepicker.FilePickerViewModel
import com.ivanovsky.passnotes.presentation.filepicker.factory.FilePickerCellModelFactory
import com.ivanovsky.passnotes.presentation.filepicker.factory.FilePickerCellViewModelFactory
import com.ivanovsky.passnotes.presentation.group_editor.GroupEditorArgs
import com.ivanovsky.passnotes.presentation.group_editor.GroupEditorViewModel
import com.ivanovsky.passnotes.presentation.groups.GroupsScreenArgs
import com.ivanovsky.passnotes.presentation.groups.GroupsViewModel
import com.ivanovsky.passnotes.presentation.groups.factory.GroupsCellModelFactory
import com.ivanovsky.passnotes.presentation.groups.factory.GroupsCellViewModelFactory
import com.ivanovsky.passnotes.presentation.main.MainScreenArgs
import com.ivanovsky.passnotes.presentation.main.MainViewModel
import com.ivanovsky.passnotes.presentation.main.navigation.NavigationMenuViewModel
import com.ivanovsky.passnotes.presentation.newdb.NewDatabaseViewModel
import com.ivanovsky.passnotes.presentation.note.NoteScreenArgs
import com.ivanovsky.passnotes.presentation.note.NoteViewModel
import com.ivanovsky.passnotes.presentation.note.factory.NoteCellModelFactory
import com.ivanovsky.passnotes.presentation.note.factory.NoteCellViewModelFactory
import com.ivanovsky.passnotes.presentation.note_editor.NoteEditorArgs
import com.ivanovsky.passnotes.presentation.note_editor.NoteEditorViewModel
import com.ivanovsky.passnotes.presentation.note_editor.factory.NoteEditorCellModelFactory
import com.ivanovsky.passnotes.presentation.note_editor.factory.NoteEditorCellViewModelFactory
import com.ivanovsky.passnotes.presentation.password_generator.PasswordGeneratorViewModel
import com.ivanovsky.passnotes.presentation.search.SearchScreenArgs
import com.ivanovsky.passnotes.presentation.search.SearchViewModel
import com.ivanovsky.passnotes.presentation.search.factory.SearchCellModelFactory
import com.ivanovsky.passnotes.presentation.search.factory.SearchCellViewModelFactory
import com.ivanovsky.passnotes.presentation.server_login.ServerLoginArgs
import com.ivanovsky.passnotes.presentation.server_login.ServerLoginViewModel
import com.ivanovsky.passnotes.presentation.settings.SettingsRouter
import com.ivanovsky.passnotes.presentation.settings.app.AppSettingsViewModel
import com.ivanovsky.passnotes.presentation.settings.database.DatabaseSettingsViewModel
import com.ivanovsky.passnotes.presentation.settings.database.change_password.ChangePasswordDialogViewModel
import com.ivanovsky.passnotes.presentation.settings.main.MainSettingsViewModel
import com.ivanovsky.passnotes.presentation.storagelist.StorageListArgs
import com.ivanovsky.passnotes.presentation.storagelist.StorageListViewModel
import com.ivanovsky.passnotes.presentation.storagelist.factory.StorageListCellModelFactory
import com.ivanovsky.passnotes.presentation.storagelist.factory.StorageListCellViewModelFactory
import com.ivanovsky.passnotes.presentation.unlock.UnlockScreenArgs
import com.ivanovsky.passnotes.presentation.unlock.UnlockViewModel
import com.ivanovsky.passnotes.presentation.unlock.cells.factory.UnlockCellModelFactory
import com.ivanovsky.passnotes.presentation.unlock.cells.factory.UnlockCellViewModelFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object UiModule {

    fun build() =
        module {
            // Interactors
            single { DatabaseLockInteractor(get(), get(), get()) }
            single { FilePickerInteractor(get(), get()) }
            single {
                UnlockInteractor(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get()
                )
            }
            single { StorageListInteractor(get(), get(), get()) }
            single { NewDatabaseInteractor(get(), get(), get(), get()) }
            single { GroupEditorInteractor(get(), get(), get(), get(), get()) }
            single { DebugMenuInteractor(get(), get(), get(), get(), get()) }
            single { NoteInteractor(get(), get(), get(), get(), get(), get(), get()) }
            single {
                GroupsInteractor(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get()
                )
            }
            single { NoteEditorInteractor(get(), get(), get(), get()) }
            single { ServerLoginInteractor(get(), get(), get()) }
            single { SearchInteractor(get(), get(), get(), get(), get(), get(), get(), get()) }
            single { MainSettingsInteractor(get()) }
            single { DatabaseSettingsInteractor(get(), get()) }
            single { AppSettingsInteractor(get(), get(), get(), get()) }
            single { AutofillInteractor(get(), get()) }
            single { MainInteractor(get()) }
            single { LockServiceInteractor(get(), get(), get(), get(), get()) }
            single { PasswordGeneratorInteractor(get()) }

            // Autofill
            single { AutofillViewFactory(get(), get()) }

            // Cell factories
            single { DatabaseStatusCellModelFactory(get()) }

            single { GroupsCellModelFactory(get()) }
            single { GroupsCellViewModelFactory(get(), get()) }

            single { NoteEditorCellModelFactory(get()) }
            single { NoteEditorCellViewModelFactory(get()) }

            single { UnlockCellModelFactory(get()) }
            single { UnlockCellViewModelFactory() }

            single { SearchCellModelFactory() }
            single { SearchCellViewModelFactory(get(), get()) }

            single { NoteCellModelFactory() }
            single { NoteCellViewModelFactory() }

            single { FilePickerCellModelFactory() }
            single { FilePickerCellViewModelFactory() }

            single { StorageListCellModelFactory(get()) }
            single { StorageListCellViewModelFactory() }

            // Cicerone
            single { Cicerone.create() }
            single { provideCiceroneRouter(get()) }
            single { provideCiceroneNavigatorHolder(get()) }
            single { SettingsRouter(get()) }

            // ViewModels
            factory { (args: StorageListArgs) ->
                StorageListViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            factory { (args: FilePickerArgs) ->
                FilePickerViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            factory { (args: GroupEditorArgs) ->
                GroupEditorViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            factory { (args: NoteScreenArgs) ->
                NoteViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            factory { (args: GroupsScreenArgs) ->
                GroupsViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            viewModel { (args: NoteEditorArgs) ->
                NoteEditorViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            viewModel { (args: ServerLoginArgs) ->
                ServerLoginViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            factory { (args: SearchScreenArgs) ->
                SearchViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            factory { (args: UnlockScreenArgs) ->
                UnlockViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    args
                )
            }
            viewModel { AboutViewModel(get(), get()) }
            viewModel { MainSettingsViewModel(get(), get()) }
            viewModel { AppSettingsViewModel(get(), get(), get(), get(), get(), get()) }
            viewModel { DatabaseSettingsViewModel(get(), get(), get(), get()) }
            viewModel { ChangePasswordDialogViewModel(get(), get(), get()) }
            viewModel { PasswordGeneratorViewModel(get(), get(), get(), get(), get()) }
            viewModel { DebugMenuViewModel(get(), get(), get(), get(), get()) }
            viewModel { NewDatabaseViewModel(get(), get(), get(), get(), get()) }
            factory { (args: SortAndViewDialogArgs) -> SortAndViewDialogViewModel(get(), args) }
            factory { NavigationMenuViewModel(get()) }
            factory { (args: MainScreenArgs) -> MainViewModel(get(), get(), args) }
        }

    private fun provideCiceroneRouter(cicerone: Cicerone<Router>) =
        cicerone.router

    private fun provideCiceroneNavigatorHolder(cicerone: Cicerone<Router>) =
        cicerone.getNavigatorHolder()
}