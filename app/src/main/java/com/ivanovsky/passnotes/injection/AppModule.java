package com.ivanovsky.passnotes.injection;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.ivanovsky.passnotes.data.ObserverBus;
import com.ivanovsky.passnotes.data.repository.GroupRepository;
import com.ivanovsky.passnotes.data.repository.NoteRepository;
import com.ivanovsky.passnotes.data.repository.db.AppDatabase;
import com.ivanovsky.passnotes.data.repository.file.FileResolver;
import com.ivanovsky.passnotes.data.repository.keepass.KeepassDatabaseRepository;
import com.ivanovsky.passnotes.data.repository.EncryptedDatabaseRepository;
import com.ivanovsky.passnotes.data.repository.UsedFileRepository;
import com.ivanovsky.passnotes.data.repository.SettingsRepository;
import com.ivanovsky.passnotes.domain.interactor.groups.GroupsInteractor;
import com.ivanovsky.passnotes.domain.interactor.newdb.NewDatabaseInteractor;
import com.ivanovsky.passnotes.domain.interactor.unlock.UnlockInteractor;
import com.ivanovsky.passnotes.domain.interactor.ErrorInteractor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

	private final Context context;
	private final AppDatabase db;
	private final UsedFileRepository usedFileRepository;

	public AppModule(Context context) {
		this.context = context;
		this.db = Room.databaseBuilder(context.getApplicationContext(),
				AppDatabase.class, AppDatabase.FILE_NAME).build();
		this.usedFileRepository = new UsedFileRepository(db);
	}

	@Provides
	@Singleton
	SettingsRepository provideSettingsManager() {
		return new SettingsRepository(context);
	}

	@Provides
	@Singleton
	AppDatabase provideAppDatabase() {
		return db;
	}

	@Provides
	@Singleton
	UsedFileRepository provideUsedFileRepository() {
		return usedFileRepository;
	}

	@Provides
	@Singleton
	EncryptedDatabaseRepository provideEncryptedDBProvider(FileResolver fileResolver) {
		return new KeepassDatabaseRepository(context, fileResolver);
	}

	@Provides
	@Singleton
	ObserverBus provideObserverBus() {
		return new ObserverBus();
	}

	@Provides
	@Singleton
	FileResolver provideFileResolver() {
		return new FileResolver();
	}

	@Provides
	@Singleton
	UnlockInteractor provideUnlockInteractor(EncryptedDatabaseRepository dbRepository,
											 UsedFileRepository usedFileRepository) {
		return new UnlockInteractor(usedFileRepository, dbRepository);
	}

	@Provides
	@Singleton
	ErrorInteractor provideErrorInteractor() {
		return new ErrorInteractor(context);
	}

	@Provides
	@Singleton
	NewDatabaseInteractor provideNewDatabaseInteractor(EncryptedDatabaseRepository dbRepository,
													   UsedFileRepository usedFileRepository,
													   ObserverBus observerBus) {
		return new NewDatabaseInteractor(context, dbRepository, usedFileRepository, observerBus);
	}
}
