package com.ivanovsky.passnotes.ui.notepads;

import android.content.Context;

import com.ivanovsky.passnotes.App;
import com.ivanovsky.passnotes.data.DbDescriptor;
import com.ivanovsky.passnotes.data.keepass.KeepassDatabaseKey;
import com.ivanovsky.passnotes.data.safedb.EncryptedDatabaseKey;
import com.ivanovsky.passnotes.data.safedb.EncryptedDatabaseProvider;
import com.ivanovsky.passnotes.data.safedb.NotepadRepository;
import com.ivanovsky.passnotes.data.safedb.model.Notepad;
import com.ivanovsky.passnotes.event.NotepadDataSetChangedEvent;
import com.ivanovsky.passnotes.ui.core.FragmentState;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotepadsPresenter implements NotepadsContract.Presenter {

	@Inject
	EncryptedDatabaseProvider safeDbProvider;

	private final DbDescriptor dbDescriptor;
	private final Context context;
	private final NotepadsContract.View view;
	private final CompositeDisposable disposables;

	NotepadsPresenter(Context context, NotepadsContract.View view, DbDescriptor dbDescriptor) {
		App.getDaggerComponent().inject(this);
		this.context = context;
		this.view = view;
		this.dbDescriptor = dbDescriptor;
		this.disposables = new CompositeDisposable();
	}

	@Override
	public void start() {
		view.setState(FragmentState.LOADING);
		EventBus.getDefault().register(this);
		loadData();
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
		disposables.clear();
	}

	@Override
	public void loadData() {
		String openedDBPath = safeDbProvider.getOpenedDatabasePath();
		if (openedDBPath != null && openedDBPath.equals(dbDescriptor.getFile().getPath())) {
			NotepadRepository repository = safeDbProvider.getDatabase().getNotepadRepository();

			Disposable disposable = repository.getAllNotepads()
					.subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(this::onNotepadsLoaded);
			disposables.add(disposable);

		} else {
			KeepassDatabaseKey key = new KeepassDatabaseKey(dbDescriptor.getPassword());

			Disposable disposable = safeDbProvider.openAsync(key, dbDescriptor.getFile())
					.subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(db -> onDbOpened());
			disposables.add(disposable);
		}
	}

	private void onDbOpened() {
		NotepadRepository repository = safeDbProvider.getDatabase().getNotepadRepository();

		Disposable disposable = repository.getAllNotepads()
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onNotepadsLoaded);
		disposables.add(disposable);
	}

	private void onNotepadsLoaded(List<Notepad> notepads) {
		if (notepads.size() != 0) {
			view.showNotepads(notepads);
		} else {
			view.showNoItems();
		}
	}

	public void onEvent(NotepadDataSetChangedEvent event) {
		loadData();
	}
}
