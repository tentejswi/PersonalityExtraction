/**
 * 
 */
package com.personalityextractor.entity.resolver;

import java.util.List;

import com.personalityextractor.entity.Entity;

/**
 * @author akishore
 *
 */
public interface IEntityResolver {

	public List<Entity> resolve(List<String> entities);

}
