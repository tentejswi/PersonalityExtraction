/**
 * 
 */
package com.personalityextractor.entity.extractor;

import java.util.List;

/**
 * @author akishore
 *
 */
public interface IEntityExtractor {

	public List<String> extract(String line);

}
