/**
 * 
 */
package com.personalityextractor.entity.resolver;

import java.util.List;

import com.personalityextractor.entity.Entity;
import com.personalityextractor.entity.WikipediaEntity;

/**
 * @author akishore
 *
 */
public interface IEntityResolver {

	public List<WikipediaEntity> resolve(List<String> entities);

}
