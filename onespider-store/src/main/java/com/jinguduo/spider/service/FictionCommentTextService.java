package com.jinguduo.spider.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.text.FictionCommentText;
import com.jinguduo.spider.store.FictionCommentCsvWriter;

@Component
public class FictionCommentTextService {
	@Autowired
	private FictionCommentCsvWriter csvFileWriter;

	public void save(List<FictionCommentText> commentTexts) throws IOException {
		if (commentTexts == null || commentTexts.isEmpty()) {
			return;
		}
		csvFileWriter.write(commentTexts);
	}
}
