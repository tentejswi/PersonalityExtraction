/**
 * 
 */
package com.personalityextractor.entity.resolver;

import java.util.ArrayList;

import com.personalityextractor.entity.extractor.IEntityExtractor;

/**
 * @author semanticvoid
 *
 */
public class BaseEntityResolver implements IEntityExtractor {
	
	protected IEntityExtractor extractor;
	
	public BaseEntityResolver(IEntityExtractor extractor) {
		this.extractor = extractor;
	}

	/* (non-Javadoc)
	 * @see com.personalityextractor.entity.extractor.IEntityExtractor#extract(java.lang.String)
	 */
	@Override
	public ArrayList<String> extract(String line) {
		// TODO Auto-generated method stub
		return null;
	}

}
