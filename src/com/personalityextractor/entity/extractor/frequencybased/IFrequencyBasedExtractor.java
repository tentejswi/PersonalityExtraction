package com.personalityextractor.entity.extractor.frequencybased;

import java.util.List;

import com.personalityextractor.commons.data.Tweet;

import cs224n.util.Counter;

public interface IFrequencyBasedExtractor {
	
	public Counter<String> extract(List<String> tweets);


}
