package com.ivanovsky.passnotes.data.repository.encdb.dao;

import com.ivanovsky.passnotes.data.entity.Note;
import com.ivanovsky.passnotes.data.entity.OperationResult;

import java.util.List;
import java.util.UUID;

public interface NoteDao {

	OperationResult<List<Note>> getNotesByGroupUid(UUID groupUid);
	OperationResult<UUID> insert(Note note);
}
