package com.ivanovsky.passnotes.data.keepass;

import com.annimon.stream.Stream;
import com.ivanovsky.passnotes.data.safedb.GroupRepository;
import com.ivanovsky.passnotes.data.safedb.dao.GroupDao;
import com.ivanovsky.passnotes.data.safedb.model.Group;

import java.util.List;

import io.reactivex.Single;

public class KeepassGroupRepository implements GroupRepository {

	private final GroupDao dao;
	private final Object lock;

	KeepassGroupRepository(GroupDao dao) {
		this.dao = dao;
		this.lock = new Object();
	}

	@Override
	public Single<List<Group>> getAllNotepads() {
		return Single.fromCallable(dao::getAll);
	}

	@Override
	public boolean isTitleFree(String title) {
		boolean result;

		synchronized (lock) {
			result = !Stream.of(dao.getAll())
					.anyMatch(notepad -> title.equals(notepad.getTitle()));
		}

		return result;
	}

	@Override
	public void insert(Group group) {
		synchronized (lock) {
			String uid = dao.insert(group);
			group.setUid(uid);
		}
	}
}