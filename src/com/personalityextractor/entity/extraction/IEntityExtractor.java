/**
 * 
 */
package com.personalityextractor.entity.extraction;

import java.util.ArrayList;

/**
 * @author akishore
 *
 */
public interface IEntityExtractor {

	public ArrayList<String> extract(String line);

}
